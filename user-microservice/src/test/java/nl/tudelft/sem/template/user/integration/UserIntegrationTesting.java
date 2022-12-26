package nl.tudelft.sem.template.user.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import nl.tudelft.sem.template.user.authentication.AuthManager;
import nl.tudelft.sem.template.user.authentication.JwtTokenVerifier;
import nl.tudelft.sem.template.user.domain.Availability;
import nl.tudelft.sem.template.user.domain.User;
import nl.tudelft.sem.template.user.domain.UserRepository;
import nl.tudelft.sem.template.user.domain.enums.BoatType;
import nl.tudelft.sem.template.user.domain.enums.Gender;
import nl.tudelft.sem.template.user.domain.enums.Level;
import nl.tudelft.sem.template.user.models.CreateAvailability;
import nl.tudelft.sem.template.user.models.UserRequestModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@SpringBootTest
@ExtendWith(SpringExtension.class)
// activate profiles to have spring use mocks during auto-injection of certain beans.
@ActiveProfiles({"test", "mockTokenVerifier", "mockAuthenticationManager"})
//@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class UserIntegrationTesting {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private transient JwtTokenVerifier mockJwtTokenVerifier;

    @Autowired
    private transient AuthManager mockAuthenticationManager;

    @Autowired
    private transient UserRepository userRepository;

    @MockBean
    private transient WebClient mockWebClient;

    User user1 = new User(null, "matei", Gender.MALE, "matei", "mirica", Level.PROFESSIONAL, "tudelft", BoatType.C4,
            new ArrayList<>(), new ArrayList<>());

    User user2 = new User(null, "user", Gender.MALE, "name1", "name2", Level.AMATEUR, "romania", BoatType.C4,
            List.of(new Availability(null, 1, LocalTime.of(10, 30), LocalTime.of(12, 45))), new ArrayList<>());

    User user3 = new User(null, "user", Gender.MALE, "name1", "name2", Level.AMATEUR, "romania", BoatType.C4,
            List.of(new Availability(null, 3, LocalTime.of(12, 30), LocalTime.of(13, 45)),
                    new Availability(null, 1, LocalTime.of(10, 30), LocalTime.of(12, 45))), new ArrayList<>());

    UserRequestModel userRequestModel1 = new UserRequestModel(new ArrayList<>(), "matei", Gender.MALE,
            "matei", "mirica", Level.PROFESSIONAL, "tudelft", BoatType.C4,
            new ArrayList<>());

    public void addUser(User user) {
        userRepository.save(user);
    }

    @Test
    public void getUsersWithNoUsers() throws Exception {
        when(mockAuthenticationManager.getNetId()).thenReturn("ExampleUser");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getNetIdFromToken(anyString())).thenReturn("ExampleUser");

        ResultActions result = mockMvc.perform(get("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken"));

        // Assert
        result.andExpect(status().isOk());

        String response = result.andReturn().getResponse().getContentAsString();

        assertThat(response).isEqualTo("[]");
    }

    @Test
    public void getUsersWithUsers() throws Exception {
        when(mockAuthenticationManager.getNetId()).thenReturn("ExampleUser");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getNetIdFromToken(anyString())).thenReturn("ExampleUser");

        addUser(user1);

        ResultActions result = mockMvc.perform(get("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken"));

        // Assert
        result.andExpect(status().isOk());

        String response = result.andReturn().getResponse().getContentAsString();

        assertThat(response).isEqualTo("[{\"id\":1,\"username\":\"matei\",\"positions\":[],\"gender\":\"MALE\","
                + "\"firstName\":\"matei\",\"lastName\":\"mirica\",\"level\":\"PROFESSIONAL\",\"availabilities\":[],"
                + "\"organisation\":\"tudelft\",\"certificate\":\"C4\"}]");
    }


    @Test
    public void testSaveUser() throws Exception {
        when(mockAuthenticationManager.getNetId()).thenReturn("ExampleUser");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getNetIdFromToken(anyString())).thenReturn("ExampleUser");

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
        String requestJson = ow.writeValueAsString(userRequestModel1);

        ResultActions result = mockMvc.perform(post("/saveUser")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .header("Authorization", "Bearer MockedToken"));

        // Assert
        result.andExpect(status().isOk());

        String response = result.andReturn().getResponse().getContentAsString();

        assertThat(response).isEqualTo("User successfully added!");

        result = mockMvc.perform(get("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken"));

        // Assert
        result.andExpect(status().isOk());

        response = result.andReturn().getResponse().getContentAsString();

        assertThat(response).isEqualTo("[{\"id\":1,\"username\":\"matei\",\"positions\":[],\"gender\":\"MALE\","
                + "\"firstName\":\"matei\",\"lastName\":\"mirica\",\"level\":\"PROFESSIONAL\",\"availabilities\":[],"
                + "\"organisation\":\"tudelft\",\"certificate\":\"C4\"}]");
    }


    @Test
    public void deleteUserWorks() throws Exception {
        when(mockAuthenticationManager.getNetId()).thenReturn("ExampleUser");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getNetIdFromToken(anyString())).thenReturn("ExampleUser");

        addUser(user1);

        ResultActions result = mockMvc.perform(delete("/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken"));

        // Assert
        result.andExpect(status().isOk());

        String response = result.andReturn().getResponse().getContentAsString();

        assertThat(response).isEqualTo("");
    }

    @Test
    public void deleteUserBadId() throws Exception {
        when(mockAuthenticationManager.getNetId()).thenReturn("ExampleUser");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getNetIdFromToken(anyString())).thenReturn("ExampleUser");

        addUser(user1);

        ResultActions result = mockMvc.perform(delete("/users/2")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken"));

        // Assert
        result.andExpect(status().isNotFound());

        String response = result.andReturn().getResponse().getContentAsString();

        assertThat(response).isEqualTo("");
    }

    @Test
    public void getUserExists() throws Exception {
        when(mockAuthenticationManager.getNetId()).thenReturn("ExampleUser");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getNetIdFromToken(anyString())).thenReturn("ExampleUser");

        addUser(user1);

        ResultActions result = mockMvc.perform(get("/exists/1")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken"));

        // Assert
        result.andExpect(status().isOk());

        String response = result.andReturn().getResponse().getContentAsString();

        assertThat(response).isEqualTo("true");
    }

    @Test
    public void getUserDoesNotExistsCorrectRequest() throws Exception {
        when(mockAuthenticationManager.getNetId()).thenReturn("ExampleUser");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getNetIdFromToken(anyString())).thenReturn("ExampleUser");

        addUser(user1);

        ResultActions result = mockMvc.perform(get("/exists/2")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken"));

        // Assert
        result.andExpect(status().isOk());

        String response = result.andReturn().getResponse().getContentAsString();

        assertThat(response).isEqualTo("false");
    }

    @Test
    public void updateAvailability() throws Exception {
        when(mockAuthenticationManager.getNetId()).thenReturn("ExampleUser");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getNetIdFromToken(anyString())).thenReturn("ExampleUser");

        addUser(user2);

        CreateAvailability createAvailability = new CreateAvailability(3, LocalTime.of(16, 20),
                LocalTime.of(20, 45));

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
        String requestJson = "{\n"
                + "    \"dayOfWeek\" : \"3\",\n"
                + "    \"startTime\": \"08:00\",\n"
                + "    \"endTime\": \"23:30\"\n"
                + "}";

        ResultActions result = mockMvc.perform(post("/updateAvailability/1/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .header("Authorization", "Bearer MockedToken"));

        // Assert
        result.andExpect(status().isOk());

        String response = result.andReturn().getResponse().getContentAsString();

        assertThat(response).isEqualTo("availability successfully updated!");
    }

    @Test
    public void updateAvailability2() throws Exception {
        when(mockAuthenticationManager.getNetId()).thenReturn("ExampleUser");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getNetIdFromToken(anyString())).thenReturn("ExampleUser");

        addUser(user3);

        CreateAvailability createAvailability = new CreateAvailability(3, LocalTime.of(16, 20),
                LocalTime.of(20, 45));

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
        String requestJson = "{\n"
                + "    \"dayOfWeek\" : \"3\",\n"
                + "    \"startTime\": \"08:00\",\n"
                + "    \"endTime\": \"23:30\"\n"
                + "}";

        ResultActions result = mockMvc.perform(post("/updateAvailability/1/2")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .header("Authorization", "Bearer MockedToken"));

        // Assert
        result.andExpect(status().isOk());

        String response = result.andReturn().getResponse().getContentAsString();

        assertThat(response).isEqualTo("availability successfully updated!");
    }

    @Test
    public void updateAvailabilityInvalidUser() throws Exception {
        when(mockAuthenticationManager.getNetId()).thenReturn("ExampleUser");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getNetIdFromToken(anyString())).thenReturn("ExampleUser");

        addUser(user2);

        CreateAvailability createAvailability = new CreateAvailability(3, LocalTime.of(16, 20),
                LocalTime.of(20, 45));

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
        String requestJson = "{\n"
                + "    \"dayOfWeek\" : \"3\",\n"
                + "    \"startTime\": \"08:00\",\n"
                + "    \"endTime\": \"23:30\"\n"
                + "}";

        ResultActions result = mockMvc.perform(post("/updateAvailability/2/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .header("Authorization", "Bearer MockedToken"));

        // Assert
        result.andExpect(status().isBadRequest());

        String response = result.andReturn().getResponse().getContentAsString();

        assertThat(response).isEqualTo("User does not exist!");
    }

    @Test
    public void getAvailabilities() throws Exception {
        when(mockAuthenticationManager.getNetId()).thenReturn("ExampleUser");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getNetIdFromToken(anyString())).thenReturn("ExampleUser");

        addUser(user2);

        ResultActions result = mockMvc.perform(get("/users/1/getAvailabilities")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken"));

        // Assert
        result.andExpect(status().isOk());

        String response = result.andReturn().getResponse().getContentAsString();

        assertThat(response).isEqualTo("[{\"id\":1,\"dayOfWeek\":1,\""
                + "startTime\":\"10:30:00\",\"endTime\":\"12:45:00\"}]");
    }

    @Test
    public void getAvailabilitiesIncorrect() throws Exception {
        when(mockAuthenticationManager.getNetId()).thenReturn("ExampleUser");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getNetIdFromToken(anyString())).thenReturn("ExampleUser");

        addUser(user2);

        ResultActions result = mockMvc.perform(get("/users/2/getAvailabilities")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken"));

        // Assert
        result.andExpect(status().isBadRequest());
    }

    @Test
    public void deleteAvailabilities() throws Exception {
        when(mockAuthenticationManager.getNetId()).thenReturn("ExampleUser");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getNetIdFromToken(anyString())).thenReturn("ExampleUser");

        addUser(user2);

        ResultActions result = mockMvc.perform(delete("/deleteAvailability/1/1")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken"));

        // Assert
        result.andExpect(status().isOk());

        String response = result.andReturn().getResponse().getContentAsString();

        assertThat(response).isEqualTo("Availability successfully deleted!");
    }

    @Test
    public void deleteAvailabilities2() throws Exception {
        when(mockAuthenticationManager.getNetId()).thenReturn("ExampleUser");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getNetIdFromToken(anyString())).thenReturn("ExampleUser");

        addUser(user3);

        ResultActions result = mockMvc.perform(delete("/deleteAvailability/1/2")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken"));

        // Assert
        result.andExpect(status().isOk());

        String response = result.andReturn().getResponse().getContentAsString();

        assertThat(response).isEqualTo("Availability successfully deleted!");
    }


    @Test
    public void deleteAvailabilitiesUserInvalid() throws Exception {
        when(mockAuthenticationManager.getNetId()).thenReturn("ExampleUser");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getNetIdFromToken(anyString())).thenReturn("ExampleUser");

        addUser(user2);

        ResultActions result = mockMvc.perform(delete("/deleteAvailability/2/1")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken"));

        // Assert
        result.andExpect(status().isBadRequest());

        String response = result.andReturn().getResponse().getContentAsString();

        assertThat(response).isEqualTo("User does not exist!");
    }

    @Test
    public void deleteAvailabilitiesInvalid() throws Exception {
        when(mockAuthenticationManager.getNetId()).thenReturn("ExampleUser");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getNetIdFromToken(anyString())).thenReturn("ExampleUser");

        addUser(user2);

        ResultActions result = mockMvc.perform(delete("/deleteAvailability/1/2")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken"));

        // Assert
        result.andExpect(status().isBadRequest());

        String response = result.andReturn().getResponse().getContentAsString();

        assertThat(response).isEqualTo("Availability does not exist!");
    }

    @Test
    public void addAvailabilities() throws Exception {
        when(mockAuthenticationManager.getNetId()).thenReturn("ExampleUser");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getNetIdFromToken(anyString())).thenReturn("ExampleUser");

        addUser(user1);

        String requestJson = "{\n"
                + "    \"dayOfWeek\" : \"3\",\n"
                + "    \"startTime\": \"08:00\",\n"
                + "    \"endTime\": \"23:30\"\n"
                + "}";

        ResultActions result = mockMvc.perform(post("/addAvailability/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .header("Authorization", "Bearer MockedToken"));

        // Assert
        result.andExpect(status().isOk());

        String response = result.andReturn().getResponse().getContentAsString();

        assertThat(response).isEqualTo("Availability(id=1, dayOfWeek=3, startTime=08:00, "
                + "endTime=23:30) successfully added!");
    }

    @Test
    public void addAvailabilitiesUserInvalid() throws Exception {
        when(mockAuthenticationManager.getNetId()).thenReturn("ExampleUser");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getNetIdFromToken(anyString())).thenReturn("ExampleUser");

        addUser(user1);

        String requestJson = "{\n"
                + "    \"dayOfWeek\" : \"3\",\n"
                + "    \"startTime\": \"08:00\",\n"
                + "    \"endTime\": \"23:30\"\n"
                + "}";

        ResultActions result = mockMvc.perform(post("/addAvailability/2")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .header("Authorization", "Bearer MockedToken"));

        // Assert
        result.andExpect(status().isBadRequest());

        String response = result.andReturn().getResponse().getContentAsString();

        assertThat(response).isEqualTo("User does not exist!");
    }

    @Test
    public void createCompetition() throws Exception {
        when(mockAuthenticationManager.getNetId()).thenReturn("user");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getNetIdFromToken(anyString())).thenReturn("user");

        WebClient.RequestBodyUriSpec requestBodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
        when(mockWebClient.method(HttpMethod.PUT))
                .thenReturn(requestBodyUriSpec);

        WebClient.RequestBodySpec requestBodySpec = mock(WebClient.RequestBodySpec.class);
        when(requestBodyUriSpec.uri("http://localhost:8083/createActivity/competition"))
                .thenReturn(requestBodySpec);

        WebClient.RequestHeadersSpec requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        when(requestBodySpec.bodyValue(any()))
                .thenReturn(requestHeadersSpec);

        WebClient.RequestBodySpec requestBodySpec1 = mock(WebClient.RequestBodySpec.class);
        when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestBodySpec1);

        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);
        when(requestBodySpec1.retrieve()).thenReturn(responseSpec);

        Mono<String> mono = mock(Mono.class);
        when(responseSpec.bodyToMono(String.class)).thenReturn(mono);

        when(mono.block()).thenReturn("Competition successfully added!");

        addUser(user2);

        String requestJson = "{\n"
                + "    \"date\": \"2023-01-03\",\n"
                + "    \"requiredPositions\": [\n"
                + "    {\n"
                + "        \"position\": \"COX\",\n"
                + "        \"requiredOfPosition\": \"1\"\n"
                + "    },\n"
                + "    {\n"
                + "        \"position\": \"COACH\",\n"
                + "        \"requiredOfPosition\": \"1\"\n"
                + "    }\n"
                + "    ],\n"
                + "    \"boatType\": \"C4\",\n"
                + "    \"startTime\": \"13:30\",\n"
                + "    \"endTime\": \"15:00\",\n"
                + "    \"allowedLevel\": \"AMATEUR\",\n"
                + "    \"allowedGender\": \"MALE\",\n"
                + "    \"organization\": \"romania\"\n"
                + "}";

        ResultActions result = mockMvc.perform(put("/create/competition")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .header("Authorization", "Bearer MockedToken"));

        // Assert
        result.andExpect(status().isOk());

        String response = result.andReturn().getResponse().getContentAsString();

        assertThat(response).isEqualTo("Competition successfully added!");
    }

    @Test
    public void createTraining() throws Exception {
        when(mockAuthenticationManager.getNetId()).thenReturn("user");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getNetIdFromToken(anyString())).thenReturn("user");

        WebClient.RequestBodyUriSpec requestBodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
        when(mockWebClient.method(HttpMethod.PUT))
                .thenReturn(requestBodyUriSpec);

        WebClient.RequestBodySpec requestBodySpec = mock(WebClient.RequestBodySpec.class);
        when(requestBodyUriSpec.uri("http://localhost:8083/createActivity/training"))
                .thenReturn(requestBodySpec);

        WebClient.RequestHeadersSpec requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        when(requestBodySpec.bodyValue(any()))
                .thenReturn(requestHeadersSpec);

        WebClient.RequestBodySpec requestBodySpec1 = mock(WebClient.RequestBodySpec.class);
        when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestBodySpec1);

        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);
        when(requestBodySpec1.retrieve()).thenReturn(responseSpec);

        Mono<String> mono = mock(Mono.class);
        when(responseSpec.bodyToMono(String.class)).thenReturn(mono);

        when(mono.block()).thenReturn("Training successfully added!");

        addUser(user2);

        String requestJson = "{\n"
                + "    \"date\": \"2023-01-03\",\n"
                + "    \"requiredPositions\": [\n"
                + "    {\n"
                + "        \"position\": \"COX\",\n"
                + "        \"requiredOfPosition\": \"1\"\n"
                + "    },\n"
                + "    {\n"
                + "        \"position\": \"COACH\",\n"
                + "        \"requiredOfPosition\": \"1\"\n"
                + "    }\n"
                + "    ],\n"
                + "    \"boatType\": \"C4\",\n"
                + "    \"startTime\": \"13:30\",\n"
                + "    \"endTime\": \"15:00\"\n"
                + "}";

        ResultActions result = mockMvc.perform(put("/create/training")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .header("Authorization", "Bearer MockedToken"));

        // Assert
        result.andExpect(status().isOk());

        String response = result.andReturn().getResponse().getContentAsString();

        assertThat(response).isEqualTo("Training successfully added!");
    }

    @Test
    public void deleteActivity() throws Exception {
        when(mockAuthenticationManager.getNetId()).thenReturn("user");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getNetIdFromToken(anyString())).thenReturn("user");


        WebClient.RequestBodyUriSpec requestBodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
        when(mockWebClient.method(HttpMethod.DELETE))
                .thenReturn(requestBodyUriSpec);

        WebClient.RequestBodySpec requestBodySpec = mock(WebClient.RequestBodySpec.class);
        when(requestBodyUriSpec.uri("http://localhost:8083/deleteActivity/1/1"))
                .thenReturn(requestBodySpec);

        WebClient.RequestBodySpec requestBodySpec1 = mock(WebClient.RequestBodySpec.class);
        when(requestBodySpec.header(anyString(), anyString()))
                .thenReturn(requestBodySpec1);

        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);
        when(requestBodySpec1.retrieve()).thenReturn(responseSpec);

        Mono<String> mono = mock(Mono.class);
        when(responseSpec.bodyToMono(String.class)).thenReturn(mono);

        when(mono.block()).thenReturn("Activity with id 1 has been deleted");

        addUser(user2);

        ResultActions result = mockMvc.perform(delete("/deleteActivity/1")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken"));

        // Assert
        result.andExpect(status().isOk());

        String response = result.andReturn().getResponse().getContentAsString();

        assertThat(response).isEqualTo("Activity with id 1 has been deleted");
    }

    @Test
    public void updateTraining() throws Exception {
        when(mockAuthenticationManager.getNetId()).thenReturn("user");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getNetIdFromToken(anyString())).thenReturn("user");

        WebClient.RequestBodyUriSpec requestBodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
        when(mockWebClient.method(HttpMethod.PUT))
                .thenReturn(requestBodyUriSpec);

        WebClient.RequestBodySpec requestBodySpec = mock(WebClient.RequestBodySpec.class);
        when(requestBodyUriSpec.uri("http://localhost:8083/updateActivity/training"))
                .thenReturn(requestBodySpec);

        WebClient.RequestHeadersSpec requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        when(requestBodySpec.bodyValue(any()))
                .thenReturn(requestHeadersSpec);

        WebClient.RequestBodySpec requestBodySpec1 = mock(WebClient.RequestBodySpec.class);
        when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestBodySpec1);

        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);
        when(requestBodySpec1.retrieve()).thenReturn(responseSpec);

        Mono<String> mono = mock(Mono.class);
        when(responseSpec.bodyToMono(String.class)).thenReturn(mono);

        when(mono.block()).thenReturn("Training successfully updated");

        addUser(user2);

        String requestJson = "{\n"
                + "    \"activityId\": \"5\",\n"
                + "    \"date\": \"2024-01-03\",\n"
                + "    \"requiredPositions\": [\n"
                + "    {\n"
                + "        \"position\": \"COX\",\n"
                + "        \"requiredOfPosition\": \"1\"\n"
                + "    }\n"
                + "    ],\n"
                + "    \"boatType\": \"C4\",\n"
                + "    \"startTime\": \"13:30\",\n"
                + "    \"endTime\": \"15:00\"\n"
                + "}";

        ResultActions result = mockMvc.perform(put("/update/training")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .header("Authorization", "Bearer MockedToken"));

        // Assert
        result.andExpect(status().isOk());

        String response = result.andReturn().getResponse().getContentAsString();

        assertThat(response).isEqualTo("Training successfully updated");
    }

    @Test
    public void updateCompetition() throws Exception {
        when(mockAuthenticationManager.getNetId()).thenReturn("user");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getNetIdFromToken(anyString())).thenReturn("user");

        WebClient.RequestBodyUriSpec requestBodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
        when(mockWebClient.method(HttpMethod.PUT))
                .thenReturn(requestBodyUriSpec);

        WebClient.RequestBodySpec requestBodySpec = mock(WebClient.RequestBodySpec.class);
        when(requestBodyUriSpec.uri("http://localhost:8083/updateActivity/competition"))
                .thenReturn(requestBodySpec);

        WebClient.RequestHeadersSpec requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        when(requestBodySpec.bodyValue(any()))
                .thenReturn(requestHeadersSpec);

        WebClient.RequestBodySpec requestBodySpec1 = mock(WebClient.RequestBodySpec.class);
        when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestBodySpec1);

        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);
        when(requestBodySpec1.retrieve()).thenReturn(responseSpec);

        Mono<String> mono = mock(Mono.class);
        when(responseSpec.bodyToMono(String.class)).thenReturn(mono);

        when(mono.block()).thenReturn("Competition successfully updated");

        addUser(user2);

        String requestJson = "{\n"
                + "    \"activityId\": \"5\",\n"
                + "    \"date\": \"2024-01-03\",\n"
                + "    \"requiredPositions\": [\n"
                + "    {\n"
                + "        \"position\": \"COX\",\n"
                + "        \"requiredOfPosition\": \"1\"\n"
                + "    }\n"
                + "    ],\n"
                + "    \"boatType\": \"C4\",\n"
                + "    \"startTime\": \"13:30\",\n"
                + "    \"endTime\": \"15:00\",\n"
                + "    \"allowedLevel\": \"AMATEUR\",\n"
                + "    \"allowedGender\": \"FEMALE\",\n"
                + "    \"organization\": \"abc\"\n"
                + "}";

        ResultActions result = mockMvc.perform(put("/update/competition")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .header("Authorization", "Bearer MockedToken"));

        // Assert
        result.andExpect(status().isOk());

        String response = result.andReturn().getResponse().getContentAsString();

        assertThat(response).isEqualTo("Competition successfully updated");
    }

    @Test
    public void acceptRequest() throws Exception {
        when(mockAuthenticationManager.getNetId()).thenReturn("user");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getNetIdFromToken(anyString())).thenReturn("user");


        WebClient.RequestBodyUriSpec requestBodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
        when(mockWebClient.method(HttpMethod.PUT))
                .thenReturn(requestBodyUriSpec);

        WebClient.RequestBodySpec requestBodySpec = mock(WebClient.RequestBodySpec.class);
        when(requestBodyUriSpec.uri("http://localhost:8086/request/1/1/accept"))
                .thenReturn(requestBodySpec);

        WebClient.RequestBodySpec requestBodySpec1 = mock(WebClient.RequestBodySpec.class);
        when(requestBodySpec.header(anyString(), anyString()))
                .thenReturn(requestBodySpec1);

        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);
        when(requestBodySpec1.retrieve()).thenReturn(responseSpec);

        Mono<Object> mono = mock(Mono.class);
        when(responseSpec.bodyToMono(Object.class)).thenReturn(mono);

        when(mono.block()).thenReturn(new Object());

        addUser(user2);

        ResultActions result = mockMvc.perform(put("/request/1/1/accept")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken"));

        // Assert
        result.andExpect(status().isOk());

        String response = result.andReturn().getResponse().getContentAsString();

        assertThat(response).isEqualTo("Request was accepted");
    }

    @Test
    public void declineRequest() throws Exception {
        when(mockAuthenticationManager.getNetId()).thenReturn("user");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getNetIdFromToken(anyString())).thenReturn("user");


        WebClient.RequestBodyUriSpec requestBodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
        when(mockWebClient.method(HttpMethod.PUT))
                .thenReturn(requestBodyUriSpec);

        WebClient.RequestBodySpec requestBodySpec = mock(WebClient.RequestBodySpec.class);
        when(requestBodyUriSpec.uri("http://localhost:8086/request/1/1/decline"))
                .thenReturn(requestBodySpec);

        WebClient.RequestBodySpec requestBodySpec1 = mock(WebClient.RequestBodySpec.class);
        when(requestBodySpec.header(anyString(), anyString()))
                .thenReturn(requestBodySpec1);

        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);
        when(requestBodySpec1.retrieve()).thenReturn(responseSpec);

        Mono<Object> mono = mock(Mono.class);
        when(responseSpec.bodyToMono(Object.class)).thenReturn(mono);

        when(mono.block()).thenReturn(new Object());

        addUser(user2);

        ResultActions result = mockMvc.perform(put("/request/1/1/decline")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken"));

        // Assert
        result.andExpect(status().isOk());

        String response = result.andReturn().getResponse().getContentAsString();

        assertThat(response).isEqualTo("Request was declined");
    }

    @Test
    public void joinActivityInvalidPosition() throws Exception {
        when(mockAuthenticationManager.getNetId()).thenReturn("user");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getNetIdFromToken(anyString())).thenReturn("user");

        addUser(user2);

        String requestJson = "{\n"
                + "    \"position\":\"PORT_SIDE_ROWER\"\n"
                + "}";

        ResultActions result = mockMvc.perform(get("/join/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .header("Authorization", "Bearer MockedToken"));

        // Assert
        result.andExpect(status().isBadRequest());

        String response = result.andReturn().getResponse().getContentAsString();

        assertThat(response).isEqualTo("User cannot fill position");
    }

    @Test
    public void joinActivityInvalidToken() throws Exception {
        when(mockAuthenticationManager.getNetId()).thenReturn("user1");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getNetIdFromToken(anyString())).thenReturn("user1");

        addUser(user2);

        String requestJson = "{\n"
                + "    \"position\":\"COACH\"\n"
                + "}";

        ResultActions result = mockMvc.perform(get("/join/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .header("Authorization", "Bearer MockedToken"));

        // Assert
        result.andExpect(status().isBadRequest());

        String response = result.andReturn().getResponse().getContentAsString();

        assertThat(response).isEqualTo("User does not exist!");
    }

    @Test
    public void retrieveTrainingInvalidPosition() throws Exception {
        when(mockAuthenticationManager.getNetId()).thenReturn("user");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getNetIdFromToken(anyString())).thenReturn("user");

        addUser(user2);

        String requestJson = "{\n"
                + "    \"position\":\"PORT_SIDE_ROWER\"\n"
                + "}";

        ResultActions result = mockMvc.perform(get("/retrieve/trainings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .header("Authorization", "Bearer MockedToken"));

        // Assert
        result.andExpect(status().isBadRequest());

        String response = result.andReturn().getResponse().getContentAsString();

        assertThat(response).isEqualTo("");
    }

    @Test
    public void retrieveTrainingInvalidToken() throws Exception {
        when(mockAuthenticationManager.getNetId()).thenReturn("user1");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getNetIdFromToken(anyString())).thenReturn("user1");

        addUser(user2);

        String requestJson = "{\n"
                + "    \"position\":\"COACH\"\n"
                + "}";

        ResultActions result = mockMvc.perform(get("/retrieve/trainings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .header("Authorization", "Bearer MockedToken"));

        // Assert
        result.andExpect(status().isNotFound());

        String response = result.andReturn().getResponse().getContentAsString();

        assertThat(response).isEqualTo("");
    }

    @Test
    public void retrieveCompetitionInvalidPosition() throws Exception {
        when(mockAuthenticationManager.getNetId()).thenReturn("user");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getNetIdFromToken(anyString())).thenReturn("user");

        addUser(user2);

        String requestJson = "{\n"
                + "    \"position\":\"PORT_SIDE_ROWER\"\n"
                + "}";

        ResultActions result = mockMvc.perform(get("/retrieve/competitions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .header("Authorization", "Bearer MockedToken"));

        // Assert
        result.andExpect(status().isBadRequest());

        String response = result.andReturn().getResponse().getContentAsString();

        assertThat(response).isEqualTo("");
    }

    @Test
    public void retrieveCompetitionsInvalidToken() throws Exception {
        when(mockAuthenticationManager.getNetId()).thenReturn("user1");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getNetIdFromToken(anyString())).thenReturn("user1");

        addUser(user2);

        String requestJson = "{\n"
                + "    \"position\":\"COACH\"\n"
                + "}";

        ResultActions result = mockMvc.perform(get("/retrieve/competitions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .header("Authorization", "Bearer MockedToken"));

        // Assert
        result.andExpect(status().isNotFound());

        String response = result.andReturn().getResponse().getContentAsString();

        assertThat(response).isEqualTo("");
    }
}
