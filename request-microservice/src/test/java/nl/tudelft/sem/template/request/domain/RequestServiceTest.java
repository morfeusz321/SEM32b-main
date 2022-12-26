package nl.tudelft.sem.template.request.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import javassist.tools.web.BadHttpRequest;
import nl.tudelft.sem.template.request.domain.exceptions.RequestNotFoundException;
import nl.tudelft.sem.template.request.event.RequestCreatedEvent;
import nl.tudelft.sem.template.request.models.RequestRequestModel;
import nl.tudelft.sem.template.request.models.RequestResponseModel;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;




class RequestServiceTest {

    private RequestRepository requestRepository = mock(RequestRepository.class);
    private KafkaTemplate kafkaTemplate = mock(KafkaTemplate.class);

    public static MockWebServer mockBackEnd;

    private RequestService requestService;

    @BeforeAll
    static void setUp() throws IOException {
        mockBackEnd = new MockWebServer();
        mockBackEnd.start();
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockBackEnd.shutdown();
    }

    @BeforeEach
    void initialize() {
        String baseUrl = String.format("http://localhost:%s",
                mockBackEnd.getPort());
        requestService = new RequestService(requestRepository, kafkaTemplate, baseUrl, baseUrl);
    }


    @Test
    void getRequestById() {
        Request request = new Request(1L, 1L, 2L, 3L, Status.PENDING, Positions.COACH);
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));
        RequestResponseModel requestResponseModel = requestService.getRequestById(1L);
        assertEquals(1L, requestResponseModel.getId());
        assertEquals(1L, requestResponseModel.getOwnerId());
        assertEquals(2L, requestResponseModel.getActivityId());
        assertEquals(3L, requestResponseModel.getRequesterId());
        assertEquals(Status.PENDING, requestResponseModel.getStatus());
        assertEquals(Positions.COACH, requestResponseModel.getPositions());
    }

    @Test
    void getRequestByIdWithEmpty() {
        Request request = new Request(1L, 1L, 2L, 3L, Status.PENDING, Positions.COACH);
        when(requestRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RequestNotFoundException.class, () -> requestService.getRequestById(1L));
    }

    @Test
    void listRequestsByUserId() {
        Request request1 = new Request(1L, 1L, 2L, 3L, Status.PENDING, Positions.COACH);
        Request request2 = new Request(1L, 1L, 2L, 3L, Status.PENDING, Positions.COACH);
        List<Request> requests = List.of(request2, request1);
        when(requestRepository.findByOwnerId(1L)).thenReturn(requests);
        List<RequestResponseModel> requestResponseModels = requestService.listRequestsByUserId(1L, Optional.empty());
        assertEquals(2, requestResponseModels.size());
    }

    @Test
    void listRequestsByUserIdWithStatus() {
        Request request1 = new Request(1L, 1L, 2L, 3L, Status.PENDING, Positions.COACH);
        Request request2 = new Request(1L, 1L, 2L, 3L, Status.PENDING, Positions.COACH);
        List<Request> requests = List.of(request2, request1);
        when(requestRepository.findByOwnerIdAndStatus(1L, Status.PENDING)).thenReturn(requests);
        List<RequestResponseModel> requestResponseModels = requestService
                .listRequestsByUserId(1L, Optional.of(Status.PENDING));
        assertEquals(2, requestResponseModels.size());
    }

    @Test
    void createRequest() {
        mockBackEnd.enqueue(new MockResponse()
                .setBody("No error")
                .addHeader("Content-Type", "application/json"));
        mockBackEnd.enqueue(new MockResponse()
                .setBody("No error")
                .addHeader("Content-Type", "application/json"));
        mockBackEnd.enqueue(new MockResponse()
                .setBody("No error")
                .addHeader("Content-Type", "application/json"));
        RequestRequestModel requestRequestModel = new RequestRequestModel(1L, 2L, 3L, Status.PENDING, Positions.COACH);
        Request request = new Request(1L, 1L, 2L, 3L, Status.PENDING, Positions.COACH);
        when(requestRepository.save(any(Request.class))).thenReturn(request);
        when(kafkaTemplate.send(any(String.class), any(RequestCreatedEvent.class))).thenReturn(null);
        RequestResponseModel requestResponseModel = requestService.createRequest(requestRequestModel, "asdasd");
        assertEquals(1L, requestResponseModel.getId());
        assertEquals(1L, requestResponseModel.getOwnerId());
        assertEquals(2L, requestResponseModel.getActivityId());
        assertEquals(3L, requestResponseModel.getRequesterId());
        assertEquals(Status.PENDING, requestResponseModel.getStatus());
        assertEquals(Positions.COACH, requestResponseModel.getPositions());
    }

    @Test
    void createRequestOwnerDoesNotExist() {
        mockBackEnd.enqueue(new MockResponse()
                .setBody("No error")
                .addHeader("Content-Type", "application/json"));
        mockBackEnd.enqueue(new MockResponse()
                .setBody("error")
                .setStatus(String.valueOf(HttpStatus.BAD_REQUEST))
                .addHeader("Content-Type", "application/json"));
        mockBackEnd.enqueue(new MockResponse()
                .setBody("No error")
                .addHeader("Content-Type", "application/json"));
        RequestRequestModel requestRequestModel = new RequestRequestModel(1L, 2L, 3L, Status.PENDING, Positions.COACH);
        Request request = new Request(1L, 1L, 2L, 3L, Status.PENDING, Positions.COACH);
        when(requestRepository.save(any(Request.class))).thenReturn(request);
        when(kafkaTemplate.send(any(String.class), any(RequestCreatedEvent.class))).thenReturn(null);
        assertThrows(Exception.class, () -> requestService.createRequest(requestRequestModel, "asdasd"));
    }

    @Test
    void createRequestRequesterDoesNotExist() {
        mockBackEnd.enqueue(new MockResponse()
                .setBody("Error")
                .setStatus(String.valueOf(HttpStatus.BAD_REQUEST))
                .addHeader("Content-Type", "application/json"));
        RequestRequestModel requestRequestModel = new RequestRequestModel(1L, 2L, 3L, Status.PENDING, Positions.COACH);
        Request request = new Request(1L, 1L, 2L, 3L, Status.PENDING, Positions.COACH);
        when(requestRepository.save(any(Request.class))).thenReturn(request);
        when(kafkaTemplate.send(any(String.class), any(RequestCreatedEvent.class))).thenReturn(null);
        assertThrows(Exception.class, () -> requestService.createRequest(requestRequestModel, "asdasd"));
    }

    @Test
    void createRequestActivityDoesNotExist() {
        mockBackEnd.enqueue(new MockResponse()
                .setBody("No error")
                .addHeader("Content-Type", "application/json"));
        mockBackEnd.enqueue(new MockResponse()
                .setBody("No error")
                .addHeader("Content-Type", "application/json"));
        mockBackEnd.enqueue(new MockResponse()
                .setBody("Error")
                .setStatus(String.valueOf(HttpStatus.BAD_REQUEST))
                .addHeader("Content-Type", "application/json"));
        RequestRequestModel requestRequestModel = new RequestRequestModel(1L, 2L, 3L, Status.PENDING, Positions.COACH);
        Request request = new Request(1L, 1L, 2L, 3L, Status.PENDING, Positions.COACH);
        when(requestRepository.save(any(Request.class))).thenReturn(request);
        when(kafkaTemplate.send(any(String.class), any(RequestCreatedEvent.class))).thenReturn(null);
        assertThrows(Exception.class, () -> requestService.createRequest(requestRequestModel, "asdasd"));
    }

    @Test
    void updateRequest() {
        mockBackEnd.enqueue(new MockResponse()
                .setBody("No error")
                .addHeader("Content-Type", "application/json"));
        mockBackEnd.enqueue(new MockResponse()
                .setBody("No error")
                .addHeader("Content-Type", "application/json"));
        mockBackEnd.enqueue(new MockResponse()
                .setBody("No error")
                .addHeader("Content-Type", "application/json"));
        RequestRequestModel requestRequestModel = new RequestRequestModel(1L, 2L, 3L, Status.PENDING, Positions.COACH);
        Request request = new Request(1L, 2L, 3L, 3L, Status.PENDING, Positions.COX);
        when(requestRepository.save(any(Request.class))).thenReturn(request);
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));
        RequestResponseModel requestResponseModel = requestService.updateRequest(requestRequestModel, 1L, "asdasd");
        assertEquals(1L, requestResponseModel.getId());
        assertEquals(1L, requestResponseModel.getOwnerId());
        assertEquals(2L, requestResponseModel.getActivityId());
        assertEquals(3L, requestResponseModel.getRequesterId());
        assertEquals(Status.PENDING, requestResponseModel.getStatus());
        assertEquals(Positions.COACH, requestResponseModel.getPositions());
    }

    @Test
    void updateRequestNotFound() {
        mockBackEnd.enqueue(new MockResponse()
                .setBody("No error")
                .addHeader("Content-Type", "application/json"));
        mockBackEnd.enqueue(new MockResponse()
                .setBody("No error")
                .addHeader("Content-Type", "application/json"));
        mockBackEnd.enqueue(new MockResponse()
                .setBody("No error")
                .addHeader("Content-Type", "application/json"));
        RequestRequestModel requestRequestModel = new RequestRequestModel(1L, 2L, 3L, Status.PENDING, Positions.COACH);
        Request request = new Request(1L, 1L, 2L, 3L, Status.PENDING, Positions.COX);
        when(requestRepository.save(any(Request.class))).thenReturn(request);
        when(requestRepository.findById(1L)).thenReturn(Optional.empty());
        RequestResponseModel requestResponseModel = requestService.updateRequest(requestRequestModel, 1L, "asdasd");
        assertEquals(1L, requestResponseModel.getId());
        assertEquals(1L, requestResponseModel.getOwnerId());
        assertEquals(2L, requestResponseModel.getActivityId());
        assertEquals(3L, requestResponseModel.getRequesterId());
        assertEquals(Status.PENDING, requestResponseModel.getStatus());
        assertEquals(Positions.COX, requestResponseModel.getPositions());
    }

    @Test
    void declineRequest() throws BadHttpRequest {

        Request request = new Request(1L, 1L, 2L, 3L, Status.PENDING, Positions.COX);
        when(requestRepository.save(any(Request.class))).thenReturn(request);
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));
        RequestResponseModel requestResponseModel = requestService.declineRequest(1L, 1L);
        assertEquals(1L, requestResponseModel.getId());
        assertEquals(1L, requestResponseModel.getOwnerId());
        assertEquals(2L, requestResponseModel.getActivityId());
        assertEquals(3L, requestResponseModel.getRequesterId());
        assertEquals(Status.DENIED, requestResponseModel.getStatus());
        assertEquals(Positions.COX, requestResponseModel.getPositions());
    }

    @Test
    void declineRequestIsNotOwner() {

        Request request = new Request(1L, 1L, 2L, 3L, Status.PENDING, Positions.COX);
        when(requestRepository.save(any(Request.class))).thenReturn(request);
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));
        assertThrows(BadHttpRequest.class, () -> requestService.declineRequest(1L, 2L));
    }

    @Test
    void acceptRequest() throws BadHttpRequest {
        mockBackEnd.enqueue(new MockResponse()
                .setBody("No error")
                .addHeader("Content-Type", "application/json"));
        Request request = new Request(1L, 1L, 2L, 3L, Status.PENDING, Positions.COX);
        when(requestRepository.save(any(Request.class))).thenReturn(request);
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));
        RequestResponseModel requestResponseModel = requestService.acceptRequest(1L, 1L, "asdasd");
        assertEquals(1L, requestResponseModel.getId());
        assertEquals(1L, requestResponseModel.getOwnerId());
        assertEquals(2L, requestResponseModel.getActivityId());
        assertEquals(3L, requestResponseModel.getRequesterId());
        assertEquals(Status.ACCEPTED, requestResponseModel.getStatus());
        assertEquals(Positions.COX, requestResponseModel.getPositions());
    }

    @Test
    void acceptRequestIsNotOwner() {

        Request request = new Request(1L, 1L, 2L, 3L, Status.PENDING, Positions.COX);
        when(requestRepository.save(any(Request.class))).thenReturn(request);
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));
        assertThrows(BadHttpRequest.class, () -> requestService.acceptRequest(1L, 2L, "asdasd"));
    }

    @Test
    void deleteRequest() throws BadHttpRequest {
        Request request = new Request(1L, 1L, 2L, 3L, Status.PENDING, Positions.COX);
        doNothing().when(requestRepository).delete(any(Request.class));
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));
        requestService.deleteRequest(1L, 1L);
        verify(requestRepository, times(1)).delete(any(Request.class));
        verify(requestRepository, times(1)).findById(1L);
    }

    @Test
    void deleteRequestNotTheOwner() throws BadHttpRequest {
        Request request = new Request(1L, 1L, 2L, 3L, Status.PENDING, Positions.COX);
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));
        requestService.deleteRequest(1L, 1L);
        assertThrows(BadHttpRequest.class, () -> requestService.deleteRequest(1L, 2L));
    }
}