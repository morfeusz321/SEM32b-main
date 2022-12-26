package nl.tudelft.sem.template.activity.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalTime;
import java.util.Date;
import java.util.Set;
import nl.tudelft.sem.template.activity.authentication.AuthManager;
import nl.tudelft.sem.template.activity.authentication.JwtTokenVerifier;
import nl.tudelft.sem.template.activity.domain.ActivityRepository;
import nl.tudelft.sem.template.activity.domain.Competition;
import nl.tudelft.sem.template.activity.domain.RequiredPositions;
import nl.tudelft.sem.template.activity.domain.RequiredPositionsRepository;
import nl.tudelft.sem.template.activity.domain.Training;
import nl.tudelft.sem.template.activity.domain.enums.BoatType;
import nl.tudelft.sem.template.activity.domain.enums.Gender;
import nl.tudelft.sem.template.activity.domain.enums.Level;
import nl.tudelft.sem.template.activity.domain.enums.Positions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest
@ExtendWith(SpringExtension.class)
// activate profiles to have spring use mocks during auto-injection of certain beans.
@ActiveProfiles({"test", "mockTokenVerifier", "mockAuthenticationManager"})
//@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ActivityControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private transient ActivityRepository activityRepository;

    @Autowired
    private transient RequiredPositionsRepository positionsRepository;

    @Autowired
    private transient JwtTokenVerifier mockJwtTokenVerifier;

    @Autowired
    private transient AuthManager mockAuthenticationManager;

    private Training training = new Training(0, new Date(12345), LocalTime.of(10, 35),
            LocalTime.of(12, 45), Set.of(new RequiredPositions(Positions.COX, 2)),
            BoatType.C4);

    private Training training2 = new Training(2, new Date(12345), LocalTime.of(10, 35),
            LocalTime.of(12, 45), Set.of(new RequiredPositions(Positions.COX, 2),
            new RequiredPositions(Positions.COACH, 0)), BoatType.C4);

    private Competition competition = new Competition(0, new Date(12345), LocalTime.of(10, 35),
            LocalTime.of(12, 45), Set.of(new RequiredPositions(Positions.COX, 2)),
            Level.AMATEUR, Gender.MALE, BoatType.C4, "tudelft");

    private Competition competition2 = new Competition(2, new Date(12345), LocalTime.of(10, 35),
            LocalTime.of(12, 45), Set.of(new RequiredPositions(Positions.COX, 2)),
            Level.AMATEUR, Gender.MALE, BoatType.C4, "tudelft");

    public void addTraining(Training t) {
        activityRepository.save(t);
    }

    public void addCompetition(Competition c) {
        activityRepository.save(c);
    }

    @Test
    public void retrieveActivityById() throws Exception {
        when(mockAuthenticationManager.getNetId()).thenReturn("ExampleUser");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getNetIdFromToken(anyString())).thenReturn("ExampleUser");

        addTraining(training);

        ResultActions result = mockMvc.perform(get("/activities/1")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken"));

        // Assert
        result.andExpect(status().isOk());

        String response = result.andReturn().getResponse().getContentAsString();

        assertThat(response).isEqualTo("{\"activityId\":1,\"ownerId\":0,\"activityDate\":\"1970-01-01\","
                + "\"startTime\":\"10:35\",\"endTime\":\"12:45\",\"positions\":[{\"id\":2,\"position\":\"COX\","
                + "\"requiredOfPosition\":2}],\"boatType\":\"C4\"}");
    }

    @Test
    public void createTraining() throws Exception {
        when(mockAuthenticationManager.getNetId()).thenReturn("ExampleUser");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getNetIdFromToken(anyString())).thenReturn("ExampleUser");

        String requestJson = "{\n"
                + "    \"ownerId\" : \"12\",\n"
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

        ResultActions result = mockMvc.perform(put("/createActivity/training")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .header("Authorization", "Bearer MockedToken"));

        // Assert
        result.andExpect(status().isOk());

        String response = result.andReturn().getResponse().getContentAsString();

        assertThat(response).isEqualTo("Training successfully added!");
    }

    @Test
    public void createTrainingBadOwner() throws Exception {
        when(mockAuthenticationManager.getNetId()).thenReturn("ExampleUser");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getNetIdFromToken(anyString())).thenReturn("ExampleUser");

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

        ResultActions result = mockMvc.perform(put("/createActivity/training")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .header("Authorization", "Bearer MockedToken"));

        // Assert
        result.andExpect(status().isBadRequest());

        String response = result.andReturn().getResponse().getContentAsString();

        assertThat(response).isEqualTo("All fields must be introduced");
    }

    @Test
    public void createTrainingBadDate() throws Exception {
        when(mockAuthenticationManager.getNetId()).thenReturn("ExampleUser");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getNetIdFromToken(anyString())).thenReturn("ExampleUser");

        String requestJson = "{\n"
                + "    \"ownerId\" : \"12\",\n"
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

        ResultActions result = mockMvc.perform(put("/createActivity/training")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .header("Authorization", "Bearer MockedToken"));

        // Assert
        result.andExpect(status().isBadRequest());

        String response = result.andReturn().getResponse().getContentAsString();

        assertThat(response).isEqualTo("All fields must be introduced");
    }

    @Test
    public void createTrainingBadPosition() throws Exception {
        when(mockAuthenticationManager.getNetId()).thenReturn("ExampleUser");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getNetIdFromToken(anyString())).thenReturn("ExampleUser");

        String requestJson = "{\n"
                + "    \"ownerId\" : \"12\",\n"
                + "    \"date\": \"2023-01-03\",\n"
                + "    \"requiredPositions\": [\n"
                + "    ],\n"
                + "    \"boatType\": \"C4\",\n"
                + "    \"startTime\": \"13:30\",\n"
                + "    \"endTime\": \"15:00\"\n"
                + "}";

        ResultActions result = mockMvc.perform(put("/createActivity/training")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .header("Authorization", "Bearer MockedToken"));

        // Assert
        result.andExpect(status().isBadRequest());

        String response = result.andReturn().getResponse().getContentAsString();

        assertThat(response).isEqualTo("All fields must be introduced");
    }

    @Test
    public void createTrainingBadBoat() throws Exception {
        when(mockAuthenticationManager.getNetId()).thenReturn("ExampleUser");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getNetIdFromToken(anyString())).thenReturn("ExampleUser");

        String requestJson = "{\n"
                + "    \"ownerId\" : \"12\",\n"
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
                + "    \"startTime\": \"13:30\",\n"
                + "    \"endTime\": \"15:00\"\n"
                + "}";

        ResultActions result = mockMvc.perform(put("/createActivity/training")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .header("Authorization", "Bearer MockedToken"));

        // Assert
        result.andExpect(status().isBadRequest());

        String response = result.andReturn().getResponse().getContentAsString();

        assertThat(response).isEqualTo("All fields must be introduced");
    }

    @Test
    public void createTrainingBadStart() throws Exception {
        when(mockAuthenticationManager.getNetId()).thenReturn("ExampleUser");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getNetIdFromToken(anyString())).thenReturn("ExampleUser");

        String requestJson = "{\n"
                + "    \"ownerId\" : \"12\",\n"
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
                + "    \"endTime\": \"15:00\"\n"
                + "}";

        ResultActions result = mockMvc.perform(put("/createActivity/training")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .header("Authorization", "Bearer MockedToken"));

        // Assert
        result.andExpect(status().isBadRequest());

        String response = result.andReturn().getResponse().getContentAsString();

        assertThat(response).isEqualTo("All fields must be introduced");
    }

    @Test
    public void createTrainingBadEnd() throws Exception {
        when(mockAuthenticationManager.getNetId()).thenReturn("ExampleUser");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getNetIdFromToken(anyString())).thenReturn("ExampleUser");

        String requestJson = "{\n"
                + "    \"ownerId\" : \"12\",\n"
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
                + "    \"startTime\": \"13:30\"\n"
                + "}";

        ResultActions result = mockMvc.perform(put("/createActivity/training")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .header("Authorization", "Bearer MockedToken"));

        // Assert
        result.andExpect(status().isBadRequest());

        String response = result.andReturn().getResponse().getContentAsString();

        assertThat(response).isEqualTo("All fields must be introduced");
    }

    @Test
    public void createCompetition() throws Exception {
        when(mockAuthenticationManager.getNetId()).thenReturn("ExampleUser");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getNetIdFromToken(anyString())).thenReturn("ExampleUser");

        String requestJson = "{\n"
                + "    \"ownerId\" : \"12\",\n"
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
                + "    \"allowedGender\": \"MALE\",\n"
                + "    \"allowedLevel\": \"AMATEUR\",\n"
                + "    \"organization\": \"tudelft\"\n"
                + "}";

        ResultActions result = mockMvc.perform(put("/createActivity/competition")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .header("Authorization", "Bearer MockedToken"));

        // Assert
        result.andExpect(status().isOk());

        String response = result.andReturn().getResponse().getContentAsString();

        assertThat(response).isEqualTo("Competition successfully added!");
    }

    @Test
    public void createCompetitionBadOwner() throws Exception {
        when(mockAuthenticationManager.getNetId()).thenReturn("ExampleUser");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getNetIdFromToken(anyString())).thenReturn("ExampleUser");

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
                + "    \"allowedGender\": \"MALE\",\n"
                + "    \"allowedLevel\": \"AMATEUR\",\n"
                + "    \"organization\": \"tudelft\"\n"
                + "}";

        ResultActions result = mockMvc.perform(put("/createActivity/competition")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .header("Authorization", "Bearer MockedToken"));

        // Assert
        result.andExpect(status().isBadRequest());

        String response = result.andReturn().getResponse().getContentAsString();

        assertThat(response).isEqualTo("All fields must be introduced");
    }

    @Test
    public void createCompetitionBadDate() throws Exception {
        when(mockAuthenticationManager.getNetId()).thenReturn("ExampleUser");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getNetIdFromToken(anyString())).thenReturn("ExampleUser");

        String requestJson = "{\n"
                + "    \"ownerId\" : \"12\",\n"
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
                + "    \"allowedGender\": \"MALE\",\n"
                + "    \"allowedLevel\": \"AMATEUR\",\n"
                + "    \"organization\": \"tudelft\"\n"
                + "}";

        ResultActions result = mockMvc.perform(put("/createActivity/competition")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .header("Authorization", "Bearer MockedToken"));

        // Assert
        result.andExpect(status().isBadRequest());

        String response = result.andReturn().getResponse().getContentAsString();

        assertThat(response).isEqualTo("All fields must be introduced");
    }

    @Test
    public void createCompetitionBadPosition() throws Exception {
        when(mockAuthenticationManager.getNetId()).thenReturn("ExampleUser");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getNetIdFromToken(anyString())).thenReturn("ExampleUser");

        String requestJson = "{\n"
                + "    \"ownerId\" : \"12\",\n"
                + "    \"date\": \"2023-01-03\",\n"
                + "    \"requiredPositions\": [\n"
                + "    ],\n"
                + "    \"boatType\": \"C4\",\n"
                + "    \"startTime\": \"13:30\",\n"
                + "    \"endTime\": \"15:00\",\n"
                + "    \"allowedGender\": \"MALE\",\n"
                + "    \"allowedLevel\": \"AMATEUR\",\n"
                + "    \"organization\": \"tudelft\"\n"
                + "}";

        ResultActions result = mockMvc.perform(put("/createActivity/competition")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .header("Authorization", "Bearer MockedToken"));

        // Assert
        result.andExpect(status().isBadRequest());

        String response = result.andReturn().getResponse().getContentAsString();

        assertThat(response).isEqualTo("All fields must be introduced");
    }

    @Test
    public void createCompetitionBadBoat() throws Exception {
        when(mockAuthenticationManager.getNetId()).thenReturn("ExampleUser");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getNetIdFromToken(anyString())).thenReturn("ExampleUser");

        String requestJson = "{\n"
                + "    \"ownerId\" : \"12\",\n"
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
                + "    \"startTime\": \"13:30\",\n"
                + "    \"endTime\": \"15:00\",\n"
                + "    \"allowedGender\": \"MALE\",\n"
                + "    \"allowedLevel\": \"AMATEUR\",\n"
                + "    \"organization\": \"tudelft\"\n"
                + "}";

        ResultActions result = mockMvc.perform(put("/createActivity/competition")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .header("Authorization", "Bearer MockedToken"));

        // Assert
        result.andExpect(status().isBadRequest());

        String response = result.andReturn().getResponse().getContentAsString();

        assertThat(response).isEqualTo("All fields must be introduced");
    }

    @Test
    public void createCompetitionBadStart() throws Exception {
        when(mockAuthenticationManager.getNetId()).thenReturn("ExampleUser");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getNetIdFromToken(anyString())).thenReturn("ExampleUser");

        String requestJson = "{\n"
                + "    \"ownerId\" : \"12\",\n"
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
                + "    \"endTime\": \"15:00\",\n"
                + "    \"allowedGender\": \"MALE\",\n"
                + "    \"allowedLevel\": \"AMATEUR\",\n"
                + "    \"organization\": \"tudelft\"\n"
                + "}";

        ResultActions result = mockMvc.perform(put("/createActivity/competition")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .header("Authorization", "Bearer MockedToken"));

        // Assert
        result.andExpect(status().isBadRequest());

        String response = result.andReturn().getResponse().getContentAsString();

        assertThat(response).isEqualTo("All fields must be introduced");
    }

    @Test
    public void createCompetitionBadEnd() throws Exception {
        when(mockAuthenticationManager.getNetId()).thenReturn("ExampleUser");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getNetIdFromToken(anyString())).thenReturn("ExampleUser");

        String requestJson = "{\n"
                + "    \"ownerId\" : \"12\",\n"
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
                + "    \"allowedGender\": \"MALE\",\n"
                + "    \"allowedLevel\": \"AMATEUR\",\n"
                + "    \"organization\": \"tudelft\"\n"
                + "}";

        ResultActions result = mockMvc.perform(put("/createActivity/competition")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .header("Authorization", "Bearer MockedToken"));

        // Assert
        result.andExpect(status().isBadRequest());

        String response = result.andReturn().getResponse().getContentAsString();

        assertThat(response).isEqualTo("All fields must be introduced");
    }

    @Test
    public void createCompetitionBadGender() throws Exception {
        when(mockAuthenticationManager.getNetId()).thenReturn("ExampleUser");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getNetIdFromToken(anyString())).thenReturn("ExampleUser");

        String requestJson = "{\n"
                + "    \"ownerId\" : \"12\",\n"
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
                + "    \"organization\": \"tudelft\"\n"
                + "}";

        ResultActions result = mockMvc.perform(put("/createActivity/competition")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .header("Authorization", "Bearer MockedToken"));

        // Assert
        result.andExpect(status().isBadRequest());

        String response = result.andReturn().getResponse().getContentAsString();

        assertThat(response).isEqualTo("All fields must be introduced");
    }

    @Test
    public void createCompetitionBadLevel() throws Exception {
        when(mockAuthenticationManager.getNetId()).thenReturn("ExampleUser");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getNetIdFromToken(anyString())).thenReturn("ExampleUser");

        String requestJson = "{\n"
                + "    \"ownerId\" : \"12\",\n"
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
                + "    \"allowedGender\": \"MALE\",\n"
                + "    \"organization\": \"tudelft\"\n"
                + "}";

        ResultActions result = mockMvc.perform(put("/createActivity/competition")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .header("Authorization", "Bearer MockedToken"));

        // Assert
        result.andExpect(status().isBadRequest());

        String response = result.andReturn().getResponse().getContentAsString();

        assertThat(response).isEqualTo("All fields must be introduced");
    }

    @Test
    public void retrieveTrainings() throws Exception {
        when(mockAuthenticationManager.getNetId()).thenReturn("ExampleUser");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getNetIdFromToken(anyString())).thenReturn("ExampleUser");

        addTraining(training);
        addCompetition(competition);

        ResultActions result = mockMvc.perform(get("/retrieveActivities/training")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken"));

        // Assert
        result.andExpect(status().isOk());

        String response = result.andReturn().getResponse().getContentAsString();

        assertThat(response).isEqualTo("[{\"activityId\":1,\"ownerId\":0,\"activityDate\":\"1970-01-01\","
                + "\"startTime\":\"10:35\",\"endTime\":\"12:45\",\"positions\":[{\"id\":2,\"position\":\"COX\","
                + "\"requiredOfPosition\":2}],\"boatType\":\"C4\"}]");
    }

    @Test
    public void retrieveCompetitions() throws Exception {
        when(mockAuthenticationManager.getNetId()).thenReturn("ExampleUser");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getNetIdFromToken(anyString())).thenReturn("ExampleUser");

        addTraining(training);
        addCompetition(competition);

        ResultActions result = mockMvc.perform(get("/retrieveActivities/competition")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken"));

        // Assert
        result.andExpect(status().isOk());

        String response = result.andReturn().getResponse().getContentAsString();

        assertThat(response).isEqualTo("[{\"activityId\":3,\"ownerId\":0,\"activityDate\":\"1970-01-01\","
                + "\"startTime\":\"10:35\",\"endTime\":\"12:45\",\"positions\":[{\"id\":4,\"position\":\"COX\","
                + "\"requiredOfPosition\":2}],\"boatType\":\"C4\",\"allowedLevel\":\"AMATEUR\",\"allowedGender\":"
                + "\"MALE\",\"organization\":\"tudelft\"}]");
    }

    @Test
    public void fillPosition() throws Exception {
        when(mockAuthenticationManager.getNetId()).thenReturn("ExampleUser");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getNetIdFromToken(anyString())).thenReturn("ExampleUser");

        addTraining(training);

        String requestJson = "{\n"
                + "    \"activityId\": \"1\",\n"
                + "    \"position\": \"COX\"\n"
                + "}";

        ResultActions result = mockMvc.perform(put("/fillPosition")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .header("Authorization", "Bearer MockedToken"));

        // Assert
        result.andExpect(status().isOk());

        String response = result.andReturn().getResponse().getContentAsString();

        assertThat(response).isEqualTo("Position successfully filled");
    }

    @Test
    public void fillPositionBadId() throws Exception {
        when(mockAuthenticationManager.getNetId()).thenReturn("ExampleUser");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getNetIdFromToken(anyString())).thenReturn("ExampleUser");

        addTraining(training);

        String requestJson = "{\n"
                + "    \"activityId\": \"2\",\n"
                + "    \"position\": \"COX\"\n"
                + "}";

        ResultActions result = mockMvc.perform(put("/fillPosition")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .header("Authorization", "Bearer MockedToken"));

        // Assert
        result.andExpect(status().isBadRequest());

        String response = result.andReturn().getResponse().getContentAsString();

        assertThat(response).isEqualTo("Activity does not exist");
    }

    @Test
    public void fillPositionAlreadyFilled() throws Exception {
        when(mockAuthenticationManager.getNetId()).thenReturn("ExampleUser");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getNetIdFromToken(anyString())).thenReturn("ExampleUser");

        addTraining(training2);

        String requestJson = "{\n"
                + "    \"activityId\": \"1\",\n"
                + "    \"position\": \"COACH\"\n"
                + "}";

        ResultActions result = mockMvc.perform(put("/fillPosition")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .header("Authorization", "Bearer MockedToken"));

        // Assert
        result.andExpect(status().isBadRequest());

        String response = result.andReturn().getResponse().getContentAsString();

        assertThat(response).isEqualTo("Position already filled");
    }

    @Test
    public void fillPositionNotAvailable() throws Exception {
        when(mockAuthenticationManager.getNetId()).thenReturn("ExampleUser");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getNetIdFromToken(anyString())).thenReturn("ExampleUser");

        addTraining(training);

        String requestJson = "{\n"
                + "    \"activityId\": \"1\",\n"
                + "    \"position\": \"PORT_SIDE_ROWER\"\n"
                + "}";

        ResultActions result = mockMvc.perform(put("/fillPosition")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .header("Authorization", "Bearer MockedToken"));

        // Assert
        result.andExpect(status().isBadRequest());

        String response = result.andReturn().getResponse().getContentAsString();

        assertThat(response).isEqualTo("Position not available in team");
    }

    @Test
    public void deleteActivity() throws Exception {
        when(mockAuthenticationManager.getNetId()).thenReturn("ExampleUser");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getNetIdFromToken(anyString())).thenReturn("ExampleUser");

        addTraining(training2);

        ResultActions result = mockMvc.perform(delete("/deleteActivity/1/2")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken"));

        // Assert
        result.andExpect(status().isOk());

        String response = result.andReturn().getResponse().getContentAsString();

        assertThat(response).isEqualTo("Activity with id 1 has been deleted");
    }

    @Test
    public void deleteActivityBadActivityId() throws Exception {
        when(mockAuthenticationManager.getNetId()).thenReturn("ExampleUser");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getNetIdFromToken(anyString())).thenReturn("ExampleUser");

        addTraining(training2);

        ResultActions result = mockMvc.perform(delete("/deleteActivity/2/2")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken"));

        // Assert
        result.andExpect(status().isBadRequest());

        String response = result.andReturn().getResponse().getContentAsString();

        assertThat(response).isEqualTo("Activity not available");
    }

    @Test
    public void deleteActivityBadOwner() throws Exception {
        when(mockAuthenticationManager.getNetId()).thenReturn("ExampleUser");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getNetIdFromToken(anyString())).thenReturn("ExampleUser");

        addTraining(training2);

        ResultActions result = mockMvc.perform(delete("/deleteActivity/1/3")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken"));

        // Assert
        result.andExpect(status().isBadRequest());

        String response = result.andReturn().getResponse().getContentAsString();

        assertThat(response).isEqualTo("User does not have permissions to delete activity!");
    }

    @Test
    public void updateCompetition() throws Exception {
        when(mockAuthenticationManager.getNetId()).thenReturn("ExampleUser");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getNetIdFromToken(anyString())).thenReturn("ExampleUser");

        addCompetition(competition2);

        String requestJson = "{\n"
                + "    \"activityId\": \"1\",\n"
                + "    \"ownerId\": \"2\",\n"
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

        ResultActions result = mockMvc.perform(put("/updateActivity/competition")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .header("Authorization", "Bearer MockedToken"));

        // Assert
        result.andExpect(status().isOk());

        String response = result.andReturn().getResponse().getContentAsString();

        assertThat(response).isEqualTo("Competition successfully updated");
    }

    @Test
    public void updateCompetitionBadId() throws Exception {
        when(mockAuthenticationManager.getNetId()).thenReturn("ExampleUser");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getNetIdFromToken(anyString())).thenReturn("ExampleUser");

        addCompetition(competition2);

        String requestJson = "{\n"
                + "    \"ownerId\": \"2\",\n"
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

        ResultActions result = mockMvc.perform(put("/updateActivity/competition")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .header("Authorization", "Bearer MockedToken"));

        // Assert
        result.andExpect(status().isBadRequest());

        String response = result.andReturn().getResponse().getContentAsString();

        assertThat(response).isEqualTo("Please provide an activity ID");
    }

    @Test
    public void updateCompetitionBadId2() throws Exception {
        when(mockAuthenticationManager.getNetId()).thenReturn("ExampleUser");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getNetIdFromToken(anyString())).thenReturn("ExampleUser");

        addCompetition(competition2);

        String requestJson = "{\n"
                + "    \"activityId\": \"2\",\n"
                + "    \"ownerId\": \"2\",\n"
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

        ResultActions result = mockMvc.perform(put("/updateActivity/competition")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .header("Authorization", "Bearer MockedToken"));

        // Assert
        result.andExpect(status().isBadRequest());

        String response = result.andReturn().getResponse().getContentAsString();

        assertThat(response).isEqualTo("Activity does not exist");
    }

    @Test
    public void updateCompetitionBadActivityType() throws Exception {
        when(mockAuthenticationManager.getNetId()).thenReturn("ExampleUser");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getNetIdFromToken(anyString())).thenReturn("ExampleUser");

        addTraining(training2);

        String requestJson = "{\n"
                + "    \"activityId\": \"1\",\n"
                + "    \"ownerId\": \"2\",\n"
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

        ResultActions result = mockMvc.perform(put("/updateActivity/competition")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .header("Authorization", "Bearer MockedToken"));

        // Assert
        result.andExpect(status().isBadRequest());

        String response = result.andReturn().getResponse().getContentAsString();

        assertThat(response).isEqualTo("Id provided for training, please use 'updateActivity/training'");
    }

    @Test
    public void updateCompetitionBadOwner() throws Exception {
        when(mockAuthenticationManager.getNetId()).thenReturn("ExampleUser");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getNetIdFromToken(anyString())).thenReturn("ExampleUser");

        addCompetition(competition2);

        String requestJson = "{\n"
                + "    \"activityId\": \"1\",\n"
                + "    \"ownerId\": \"3\",\n"
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

        ResultActions result = mockMvc.perform(put("/updateActivity/competition")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .header("Authorization", "Bearer MockedToken"));

        // Assert
        result.andExpect(status().isBadRequest());

        String response = result.andReturn().getResponse().getContentAsString();

        assertThat(response).isEqualTo("User does not have permissions to edit activity!");
    }

    @Test
    public void updateTraining() throws Exception {
        when(mockAuthenticationManager.getNetId()).thenReturn("ExampleUser");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getNetIdFromToken(anyString())).thenReturn("ExampleUser");

        addTraining(training2);

        String requestJson = "{\n"
                + "    \"activityId\": \"1\",\n"
                + "    \"ownerId\": \"2\",\n"
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

        ResultActions result = mockMvc.perform(put("/updateActivity/training")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .header("Authorization", "Bearer MockedToken"));

        // Assert
        result.andExpect(status().isOk());

        String response = result.andReturn().getResponse().getContentAsString();

        assertThat(response).isEqualTo("Training successfully updated");
    }

    @Test
    public void updateTrainingBadId() throws Exception {
        when(mockAuthenticationManager.getNetId()).thenReturn("ExampleUser");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getNetIdFromToken(anyString())).thenReturn("ExampleUser");

        addTraining(training2);

        String requestJson = "{\n"
                + "    \"ownerId\": \"2\",\n"
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

        ResultActions result = mockMvc.perform(put("/updateActivity/training")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .header("Authorization", "Bearer MockedToken"));

        // Assert
        result.andExpect(status().isBadRequest());

        String response = result.andReturn().getResponse().getContentAsString();

        assertThat(response).isEqualTo("Please provide an activity ID");
    }

    @Test
    public void updateTrainingBadId2() throws Exception {
        when(mockAuthenticationManager.getNetId()).thenReturn("ExampleUser");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getNetIdFromToken(anyString())).thenReturn("ExampleUser");

        addTraining(training2);

        String requestJson = "{\n"
                + "    \"activityId\": \"3\",\n"
                + "    \"ownerId\": \"2\",\n"
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

        ResultActions result = mockMvc.perform(put("/updateActivity/training")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .header("Authorization", "Bearer MockedToken"));

        // Assert
        result.andExpect(status().isBadRequest());

        String response = result.andReturn().getResponse().getContentAsString();

        assertThat(response).isEqualTo("Activity does not exist");
    }

    @Test
    public void updateTrainingBadOwner() throws Exception {
        when(mockAuthenticationManager.getNetId()).thenReturn("ExampleUser");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getNetIdFromToken(anyString())).thenReturn("ExampleUser");

        addTraining(training2);

        String requestJson = "{\n"
                + "    \"activityId\": \"1\",\n"
                + "    \"ownerId\": \"3\",\n"
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

        ResultActions result = mockMvc.perform(put("/updateActivity/training")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .header("Authorization", "Bearer MockedToken"));

        // Assert
        result.andExpect(status().isBadRequest());

        String response = result.andReturn().getResponse().getContentAsString();

        assertThat(response).isEqualTo("User does not have permissions to edit activity!");
    }

    @Test
    public void updateTrainingBadActivityType() throws Exception {
        when(mockAuthenticationManager.getNetId()).thenReturn("ExampleUser");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getNetIdFromToken(anyString())).thenReturn("ExampleUser");

        addCompetition(competition2);

        String requestJson = "{\n"
                + "    \"activityId\": \"1\",\n"
                + "    \"ownerId\": \"2\",\n"
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

        ResultActions result = mockMvc.perform(put("/updateActivity/training")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .header("Authorization", "Bearer MockedToken"));

        // Assert
        result.andExpect(status().isBadRequest());

        String response = result.andReturn().getResponse().getContentAsString();

        assertThat(response).isEqualTo("Id provided for competition, please use 'updateActivity/competition'");
    }
}
