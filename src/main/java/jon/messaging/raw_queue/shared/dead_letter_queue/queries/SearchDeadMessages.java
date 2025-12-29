package jon.messaging.raw_queue.shared.dead_letter_queue.queries;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.EntityManager;
import jon.messaging.raw_queue.shared.dead_letter_queue.infra.DLQRepo;
import jon.messaging.raw_queue.shared.dead_letter_queue.domain.DeadLetterQueue;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.Querydsl;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static jon.messaging.raw_queue.shared.dead_letter_queue.JsonUtils.adjustEscaping;

@RestController
@RequestMapping("/dead-letter-queue")
@RequiredArgsConstructor
class SearchDeadMessagesHttpController {
    private final SearchDeadMessages searchHandler;

    @PostMapping("/search")
    public ResponseEntity<Response> getMessages(Pageable pageable, @RequestBody SearchDeadMessages.Filters filters) {
        var page = searchHandler.getAll(pageable, filters);
        var pageFormatted = new PageImpl<>(mapToDlqDto(page.getContent()), pageable, page.getTotalElements());
        return ResponseEntity.ok(new Response(pageFormatted, "1.0.0", LocalDateTime.now().toString()));
    }

    private List<Response.Dlq> mapToDlqDto(final List<DeadLetterQueue> messages) {
        return messages.stream().map(msg -> {
            var data = adjustEscaping(msg.getData());
            return new Response.Dlq(msg.getMessageId(), data, msg.getArrivedAt(), msg.getFromQueue(), msg.getOrigin().name(), msg.getError());
        }).toList();
    }

    @Data
    @AllArgsConstructor
    static class Response {
        Page<Dlq> data;
        String version;
        String retrievedAt;

        @Data
        @AllArgsConstructor
        static class Dlq {
            UUID messageId;
            String data;
            LocalDateTime arrivedAt;
            String fromQueue;
            String origin;
            String error;
        }
    }
}

@RestController
@RequestMapping("/dead-letter-queue")
public class SearchDeadMessages {
    JPAQueryFactory queryFactory;

    public SearchDeadMessages(DLQRepo repo, EntityManager entityManager) {
        var queryFactory = new JPAQueryFactory(JPQLTemplates.DEFAULT, entityManager);
    }

    @GetMapping
    public Page<DeadLetterQueue> getAll(Pageable pageable, Filters filters) {
        var builder = buildFilters(filters);

        var jpaQuery = queryFactory.select(QDeadLetterQueue.deadLetterQueue)
                .from(QDeadLetterQueue.deadLetterQueue)
                .where(builder);

        if (pageable.getOffset() > 1000) {
            throw new ServiceApiException("Massa ocurrències compleixen la condició, si us plau acota més la consulta mitjançant els filtres");
        }

        Querydsl querydsl = new Querydsl(Objects.requireNonNull(entityManager), (new PathBuilderFactory()).create(DeadLetterQueue.class));
        querydsl.applyPagination(pageable, jpaQuery);

        return new PageImpl<>(jpaQuery.fetch(), pageable,  jpaQuery.fetchCount());
    }

    public Stream<DeadLetterQueue> streamAll(Filters filters) {
        final JPAQueryFactory queryFactory = new JPAQueryFactory(JPQLTemplates.DEFAULT, entityManager);

        var builder = buildFilters(filters);

        return queryFactory.select(QDeadLetterQueue.deadLetterQueue)
                .from(QDeadLetterQueue.deadLetterQueue)
                .where(builder).stream();
    }

    private BooleanBuilder buildFilters(Filters filters) {
        var builder = new BooleanBuilder();
        ofNullable(filters.getMessageId()).ifPresent(msgId -> builder.and(QDeadLetterQueue.deadLetterQueue.messageId.eq(msgId)));
        ofNullable(filters.getOrigin()).ifPresent(origin -> builder.and(QDeadLetterQueue.deadLetterQueue.origin.equalsIgnoreCase(origin.name())));
        buildArrivedAt(filters, builder);

        ofNullable(filters.getCip()).filter(StringUtils::hasText).ifPresent(cip -> {
            var is3LabNotPattern = "%<ORU_R01.PATIENT>%<PID>%<PID.3>%<CX.1>" + cip + "</CX.1>%";
            var ecapArgosPattern = "%{%cip%:%" + cip + "%,%";
            builder.and(
                    QDeadLetterQueue.deadLetterQueue.data.likeIgnoreCase(is3LabNotPattern)
                            .or(QDeadLetterQueue.deadLetterQueue.data.likeIgnoreCase(ecapArgosPattern))
            );
        });

        ofNullable(filters.getReportingCenter()).filter(StringUtils::hasText).ifPresent(reportingCenter -> {
            var is3LabNotPattern = "%<ORC.2>%<EI.3>" + reportingCenter + "</EI.3>%</ORC.2>%";
            var ecapArgosPattern = "%{%codi%:3,%valor%:%" + reportingCenter + "%}%";

            builder.and(
                    QDeadLetterQueue.deadLetterQueue.data.likeIgnoreCase(is3LabNotPattern)
                            .or(QDeadLetterQueue.deadLetterQueue.data.likeIgnoreCase(ecapArgosPattern))
            );
        });

        ofNullable(filters.getNotificationDate()).ifPresent(notificationDate -> {
            String ecapArgosDate = notificationDate.toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).replace("-", "/");
            String is3LabNotDate = notificationDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")); //HHmmss

            var is3LabNotPattern = "%<MSH.7>%<TS.1>" + is3LabNotDate + "%" + "</TS.1>%</MSH.7>%"; //Adding % in value to ignore hours, minutes and seconds
            var ecapArgosPattern = "%{%codi%:999999,%valor%:%" + ecapArgosDate + "%}%";

            builder.and(
                    QDeadLetterQueue.deadLetterQueue.data.likeIgnoreCase(is3LabNotPattern)
                            .or(QDeadLetterQueue.deadLetterQueue.data.likeIgnoreCase(ecapArgosPattern))
            );
        });

        ofNullable(filters.getAnyValue())
                .filter(StringUtils::hasText)
                .ifPresent(value -> builder.and(QDeadLetterQueue.deadLetterQueue.data.likeIgnoreCase( "%"+ value + "%" )));

        ofNullable(filters.getErrorDescription())
                .filter(StringUtils::hasText)
                .ifPresent(value -> builder.and(QDeadLetterQueue.deadLetterQueue.error.likeIgnoreCase( "%"+ value + "%" )));

        return builder;
    }

    private void buildArrivedAt(final Filters filters, final BooleanBuilder builder) {
        if(filters.getArrivedAtStart() != null) {
            if(filters.getArrivedAtEnd() != null) {
                builder.and(QDeadLetterQueue.deadLetterQueue.arrivedAt.after(filters.getArrivedAtStart()));
                builder.and(QDeadLetterQueue.deadLetterQueue.arrivedAt.before(filters.getArrivedAtEnd()));
            } else {
                builder.and(QDeadLetterQueue.deadLetterQueue.arrivedAt.after(filters.getArrivedAtStart()));
            }
        } else {
            if(filters.getArrivedAtEnd() != null) {
                builder.and(QDeadLetterQueue.deadLetterQueue.arrivedAt.before(filters.getArrivedAtEnd()));
            }
        }
    }

    public record Filters(
            UUID messageId,
            String origin,
            @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            LocalDateTime arrivedAtStart,
            @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            LocalDateTime arrivedAtEnd,
            String cip,
            String reportingCenter,
            @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            LocalDateTime notificationDate,
            String anyValue,
            String errorDescription
    ) { }
}
