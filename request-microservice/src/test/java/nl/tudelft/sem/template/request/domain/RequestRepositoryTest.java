package nl.tudelft.sem.template.request.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@DataJpaTest
public class RequestRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    private RequestRepository requestRepository;

    @Test
    void checkSave() {
        Request request1 = new Request(1L, 2L, 3L, 4L, Status.PENDING, Positions.COACH);
        Request request2 = new Request(5L, 44L, 77L, 57L, Status.ACCEPTED, Positions.COACH);

        request1 = requestRepository.save(request1);
        request2 = requestRepository.save(request2);

        List<Request> requests = requestRepository.findAll();
        assertEquals(2, requests.size());
    }

    @Test
    void checkEmpty() {
        List<Request> requests = requestRepository.findAll();
        assertEquals(0, requests.size());
    }

    @Test
    void findByOwnerId() {
        Request request1 = new Request(1L, 2L, 3L, 4L, Status.PENDING, Positions.COACH);
        request1 = requestRepository.save(request1);
        List<Request> requests = requestRepository.findByOwnerId(2L);
        assertEquals(2L, requests.get(0).getOwnerId());
        assertEquals(3L, requests.get(0).getActivityId());
        assertEquals(4L, requests.get(0).getRequesterId());
        assertEquals(Status.PENDING, requests.get(0).getStatus());
        assertEquals(Positions.COACH, requests.get(0).getPositions());
    }

    @Test
    void findByOwnerIdAndStatus() {
        Request request1 = new Request(1L, 2L, 3L, 4L, Status.PENDING, Positions.COACH);
        Request request2 = new Request(4L, 2L, 7L, 10L, Status.ACCEPTED, Positions.COACH);

        request1 = requestRepository.save(request1);
        request2 = requestRepository.save(request2);

        List<Request> requests = requestRepository.findByOwnerIdAndStatus(2L, Status.PENDING);

        assertEquals(1, requests.size());
        assertEquals(1L, requests.get(0).getId());
        assertEquals(2L, requests.get(0).getOwnerId());
        assertEquals(3L, requests.get(0).getActivityId());
        assertEquals(4L, requests.get(0).getRequesterId());
        assertEquals(Status.PENDING, requests.get(0).getStatus());
        assertEquals(Positions.COACH, requests.get(0).getPositions());
    }
}