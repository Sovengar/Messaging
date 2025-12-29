package jon.messaging.raw_queue.shared.dead_letter_queue.infra;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DLQScheduler {
    //private final ReportFailures reportFailures;

    //@Value("${...}")
    //private boolean notifyErrorCarregaCases;

    @Scheduled(cron = "0 0 6 * * *")
    public void reportFailures(){
      //  if (!notifyErrorCarregaCases) {
        //    return;
        //}

        //reportFailures.sendEmailIfFailedRowsPresent();
    }
}
