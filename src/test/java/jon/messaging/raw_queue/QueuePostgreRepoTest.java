package jon.messaging.raw_queue;

import jakarta.persistence.EntityManager;
import jon.messaging.raw_queue.shared.abstract_queue.QueuePostgreRepo;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;

import java.util.List;

import static org.mockito.Mockito.mock;

//TODO TERMINAR LO DE LA IA
//@Slf4j
//class QueuePostgreRepoTest {
//
//    private EntityManager entityManager;
//    private QueuePostgreRepo repo;
//
//    @BeforeEach
//    void setUp() {
//        entityManager = mock(EntityManager.class);
//        log = mock(Logger.class);
//        repo = new QueuePostgreRepo(entityManager, log);
//    }
//
//    @Test
//    void testDeleteOldMessages_logsAndDeletes() {
//        String tableName = "queue_table";
//        List<Long> ids = Arrays.asList(1L, 2L, 3L);
//
//        Query selectQuery = mock(Query.class);
//        Query deleteQuery = mock(Query.class);
//
//        when(entityManager.createNativeQuery(contains("SELECT"))).thenReturn(selectQuery);
//        when(entityManager.createNativeQuery(contains("DELETE"))).thenReturn(deleteQuery);
//
//        when(selectQuery.setParameter(eq("yesterday"), any())).thenReturn(selectQuery);
//        when(selectQuery.getResultList()).thenReturn(ids);
//
//        when(deleteQuery.setParameter("ids", ids)).thenReturn(deleteQuery);
//        when(deleteQuery.executeUpdate()).thenReturn(ids.size());
//
//        int deleted = repo.deleteOldMessages(tableName);
//
//        assertEquals(ids.size(), deleted);
//
//        verify(log).info(contains("Deleting {} old messages"), eq(ids.size()), eq(tableName), eq(ids));
//        verify(deleteQuery).executeUpdate();
//    }
//
//    @Test
//    void testDeleteOldMessages_noMessages() {
//        String tableName = "queue_table";
//        List<Long> ids = List.of();
//
//        Query selectQuery = mock(Query.class);
//
//        when(entityManager.createNativeQuery(contains("SELECT"))).thenReturn(selectQuery);
//        when(selectQuery.setParameter(eq("yesterday"), any())).thenReturn(selectQuery);
//        when(selectQuery.getResultList()).thenReturn(ids);
//
//        int deleted = repo.deleteOldMessages(tableName);
//
//        assertEquals(0, deleted);
//        verify(log).info(contains("No old messages"), eq(tableName));
//    }
//}
