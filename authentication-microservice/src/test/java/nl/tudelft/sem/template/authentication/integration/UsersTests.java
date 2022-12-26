package nl.tudelft.sem.template.authentication.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import nl.tudelft.sem.template.authentication.authentication.JwtTokenGenerator;
import nl.tudelft.sem.template.authentication.domain.Availability;
import nl.tudelft.sem.template.authentication.domain.Positions;
import nl.tudelft.sem.template.authentication.domain.enums.BoatType;
import nl.tudelft.sem.template.authentication.domain.enums.Gender;
import nl.tudelft.sem.template.authentication.domain.enums.Level;
import nl.tudelft.sem.template.authentication.domain.enums.Position;
import nl.tudelft.sem.template.authentication.domain.user.AppUser;
import nl.tudelft.sem.template.authentication.domain.user.HashedPassword;
import nl.tudelft.sem.template.authentication.domain.user.MemberId;
import nl.tudelft.sem.template.authentication.domain.user.Password;
import nl.tudelft.sem.template.authentication.domain.user.PasswordHashingService;
import nl.tudelft.sem.template.authentication.domain.user.Role;
import nl.tudelft.sem.template.authentication.domain.user.RoleRepository;
import nl.tudelft.sem.template.authentication.domain.user.UserRepository;
import nl.tudelft.sem.template.authentication.framework.integration.utils.JsonUtil;
import nl.tudelft.sem.template.authentication.models.AddRoleToUserRequestModel;
import nl.tudelft.sem.template.authentication.models.AuthenticationRequestModel;
import nl.tudelft.sem.template.authentication.models.AuthenticationResponseModel;
import nl.tudelft.sem.template.authentication.models.ChangeUserPasswordRequestModel;
import nl.tudelft.sem.template.authentication.models.ListUserRolesRequestModel;
import nl.tudelft.sem.template.authentication.models.RegistrationRequestModel;
import nl.tudelft.sem.template.authentication.models.RegistrationRequestModelTest;
import nl.tudelft.sem.template.authentication.models.RoleRegistrationRequestModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.client.RestTemplate;

@SpringBootTest
@ExtendWith(SpringExtension.class)
// activate profiles to have spring use mocks during auto-injection of certain beans.
@ActiveProfiles({"test", "mockPasswordEncoder", "mockTokenGenerator", "mockAuthenticationManager", "mockRestTemplate"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureMockMvc
public class UsersTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private transient PasswordHashingService mockPasswordEncoder;

    @Autowired
    private transient JwtTokenGenerator mockJwtTokenGenerator;

    @Autowired
    private transient AuthenticationManager mockAuthenticationManager;

    @Autowired
    private transient UserRepository userRepository;

    @Autowired
    private transient RoleRepository roleRepository;

    @Autowired
    private transient RestTemplate mockRestTemplate;

    @Test
    public void registerTest_withValidData_worksCorrectly() throws Exception {
        // Arrange
        final MemberId testUser = new MemberId("SomeUser");
        final Password testPassword = new Password("password123");
        final HashedPassword testHashedPassword = new HashedPassword("hashedTestPassword");

        when(mockPasswordEncoder.hash(testPassword)).thenReturn(testHashedPassword);

        RegistrationRequestModelTest model = new RegistrationRequestModelTest();
        model.setMemberId(testUser.toString());
        model.setPassword(testPassword.toString());

        // Act
        ResultActions resultActions = mockMvc.perform(post("/authentication/registerTest")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.serialize(model)));

        // Assert
        resultActions.andExpect(status().isOk());

        AppUser savedUser = userRepository.findByMemberId(testUser).orElseThrow();

        assertThat(savedUser.getMemberId()).isEqualTo(testUser);
        assertThat(savedUser.getPassword()).isEqualTo(testHashedPassword);
    }


    @Test
    public void register_withValidData_worksCorrectly() throws Exception {
        // Arrange

        final MemberId testUser = new MemberId("SomeUser");
        final Password testPassword = new Password("password123");
        final HashedPassword testHashedPassword = new HashedPassword("hashedTestPassword");

        when(mockPasswordEncoder.hash(testPassword)).thenReturn(testHashedPassword);
        when(mockRestTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(new ResponseEntity(HttpStatus.OK));

        RegistrationRequestModel model = new RegistrationRequestModel();
        model.setMemberId(testUser.toString());
        model.setPassword(testPassword.toString());

        Collection<Positions> positions = new ArrayList<>();
        Positions position1 = new Positions();
        position1.setPosition(Position.COX);
        positions.add(position1);
        model.setPositions(positions);

        String firstName = "Walter";
        String lastName = "White";
        String organization = "Yes";
        BoatType certificate = BoatType.C4;
        Gender gender = Gender.OTHER;
        Level level = Level.PROFESSIONAL;

        model.setFirstName(firstName);
        model.setLastName(lastName);
        model.setOrganization(organization);
        model.setCertificate(certificate);
        model.setGender(gender);
        model.setLevel(level);

        Availability availability1 = new Availability();
        availability1.setDayOfWeek(1);
        Collection<Availability> availabilities = new ArrayList<>();
        availabilities.add(availability1);
        model.setAvailabilities(availabilities);

        // Act
        ResultActions resultActions = mockMvc.perform(post("/authentication/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.serialize(model)));

        // Assert
        resultActions.andExpect(status().isOk());
        verify(mockRestTemplate, times(1))
                .postForEntity(anyString(), any(HttpEntity.class), eq(String.class));

        AppUser savedUser = userRepository.findByMemberId(testUser).orElseThrow();

        assertThat(savedUser.getMemberId()).isEqualTo(testUser);
        assertThat(savedUser.getPassword()).isEqualTo(testHashedPassword);
    }

    @Test
    public void registerTest_withExistingUser_throwsException() throws Exception {
        // Arrange
        final MemberId testUser = new MemberId("SomeUser");
        final Password newTestPassword = new Password("password456");
        final HashedPassword existingTestPassword = new HashedPassword("password123");

        AppUser existingAppUser = new AppUser(testUser, existingTestPassword);
        userRepository.save(existingAppUser);

        RegistrationRequestModelTest model = new RegistrationRequestModelTest();
        model.setMemberId(testUser.toString());
        model.setPassword(newTestPassword.toString());

        // Act
        ResultActions resultActions = mockMvc.perform(post("/authentication/registerTest")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.serialize(model)));

        // Assert
        resultActions.andExpect(status().isBadRequest());

        AppUser savedUser = userRepository.findByMemberId(testUser).orElseThrow();

        assertThat(savedUser.getMemberId()).isEqualTo(testUser);
        assertThat(savedUser.getPassword()).isEqualTo(existingTestPassword);
    }

    @Test
    public void register_withInvalidData_worksCorrectly() throws Exception {
        // Arrange

        final MemberId testUser = new MemberId("SomeUser");
        final Password testPassword = new Password("password123");
        final HashedPassword testHashedPassword = new HashedPassword("hashedTestPassword");

        when(mockPasswordEncoder.hash(testPassword)).thenReturn(testHashedPassword);
        when(mockRestTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(new ResponseEntity(HttpStatus.BAD_REQUEST));

        RegistrationRequestModel model = new RegistrationRequestModel();
        model.setMemberId(testUser.toString());
        model.setPassword(testPassword.toString());

        Collection<Positions> positions = new ArrayList<>();
        Positions position1 = new Positions();
        position1.setPosition(Position.COX);
        positions.add(position1);
        model.setPositions(positions);

        String firstName = "Walter";
        String lastName = "White";
        String organization = "Yes";
        BoatType certificate = BoatType.C4;
        Gender gender = Gender.OTHER;
        Level level = Level.PROFESSIONAL;

        model.setFirstName(firstName);
        model.setLastName(lastName);
        model.setOrganization(organization);
        model.setCertificate(certificate);
        model.setGender(gender);
        model.setLevel(level);

        Availability availability1 = new Availability();
        availability1.setDayOfWeek(1);
        Collection<Availability> availabilities = new ArrayList<>();
        availabilities.add(availability1);
        model.setAvailabilities(availabilities);

        // Act
        ResultActions resultActions = mockMvc.perform(post("/authentication/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.serialize(model)));

        // Assert
        resultActions.andExpect(status().isBadRequest());
        verify(mockRestTemplate, times(1))
                .postForEntity(anyString(), any(HttpEntity.class), eq(String.class));
        assertThat(userRepository.existsByMemberId(testUser)).isFalse();
    }

    @Test
    public void login_withValidUser_returnsToken() throws Exception {
        // Arrange
        final MemberId testUser = new MemberId("SomeUser");
        final Password testPassword = new Password("password123");
        final HashedPassword testHashedPassword = new HashedPassword("hashedTestPassword");
        when(mockPasswordEncoder.hash(testPassword)).thenReturn(testHashedPassword);

        when(mockAuthenticationManager.authenticate(argThat(authentication ->
                !testUser.toString().equals(authentication.getPrincipal())
                    || !testPassword.toString().equals(authentication.getCredentials())
        ))).thenThrow(new UsernameNotFoundException("User not found"));

        final String testToken = "testJWTToken";
        when(mockJwtTokenGenerator.generateToken(
            argThat(userDetails -> userDetails.getUsername().equals(testUser.toString())))
        ).thenReturn(testToken);

        AppUser appUser = new AppUser(testUser, testHashedPassword);
        userRepository.save(appUser);

        AuthenticationRequestModel model = new AuthenticationRequestModel();
        model.setMemberId(testUser.toString());
        model.setPassword(testPassword.toString());

        // Act
        ResultActions resultActions = mockMvc.perform(post("/authentication/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.serialize(model)));


        // Assert
        MvcResult result = resultActions
                .andExpect(status().isOk())
                .andReturn();

        AuthenticationResponseModel responseModel = JsonUtil.deserialize(result.getResponse().getContentAsString(),
                AuthenticationResponseModel.class);

        assertThat(responseModel.getToken()).isEqualTo(testToken);

        verify(mockAuthenticationManager).authenticate(argThat(authentication ->
                testUser.toString().equals(authentication.getPrincipal())
                    && testPassword.toString().equals(authentication.getCredentials())));
    }

    @Test
    public void login_withNonexistentUsername_returns403() throws Exception {
        // Arrange
        final String testUser = "SomeUser";
        final String testPassword = "password123";

        when(mockAuthenticationManager.authenticate(argThat(authentication ->
                testUser.equals(authentication.getPrincipal())
                    && testPassword.equals(authentication.getCredentials())
        ))).thenThrow(new UsernameNotFoundException("User not found"));

        AuthenticationRequestModel model = new AuthenticationRequestModel();
        model.setMemberId(testUser);
        model.setPassword(testPassword);

        // Act
        ResultActions resultActions = mockMvc.perform(post("/authentication/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.serialize(model)));

        // Assert
        resultActions.andExpect(status().isForbidden());

        verify(mockAuthenticationManager).authenticate(argThat(authentication ->
                testUser.equals(authentication.getPrincipal())
                    && testPassword.equals(authentication.getCredentials())));

        verify(mockJwtTokenGenerator, times(0)).generateToken(any());
    }

    @Test
    public void login_withInvalidPassword_returns403() throws Exception {
        // Arrange
        final String testUser = "SomeUser";
        final String wrongPassword = "password1234";
        final String testPassword = "password123";
        final HashedPassword testHashedPassword = new HashedPassword("hashedTestPassword");
        when(mockPasswordEncoder.hash(new Password(testPassword))).thenReturn(testHashedPassword);

        when(mockAuthenticationManager.authenticate(argThat(authentication ->
                testUser.equals(authentication.getPrincipal())
                    && wrongPassword.equals(authentication.getCredentials())
        ))).thenThrow(new BadCredentialsException("Invalid password"));

        AppUser appUser = new AppUser(new MemberId(testUser), testHashedPassword);
        userRepository.save(appUser);

        AuthenticationRequestModel model = new AuthenticationRequestModel();
        model.setMemberId(testUser);
        model.setPassword(wrongPassword);

        // Act
        ResultActions resultActions = mockMvc.perform(post("/authentication/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.serialize(model)));

        // Assert
        resultActions.andExpect(status().isUnauthorized());

        verify(mockAuthenticationManager).authenticate(argThat(authentication ->
                testUser.equals(authentication.getPrincipal())
                    && wrongPassword.equals(authentication.getCredentials())));

        verify(mockJwtTokenGenerator, times(0)).generateToken(any());
    }

    @Test
    public void registerRole_withValidData_worksCorrectly() throws Exception {
        // Arrange
        final String testRole = "SomeRole";

        RoleRegistrationRequestModel model = new RoleRegistrationRequestModel();
        model.setName(testRole);
        // Act

        ResultActions resultActions = mockMvc.perform(post("/authentication/registerRole")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.serialize(model)));

        // Assert
        resultActions.andExpect(status().isOk());

        Role savedRole = roleRepository.findByName(testRole).orElseThrow();

        assertThat(savedRole.getRole()).isEqualTo(testRole);
    }

    @Test
    public void registerRole_withExistingUser_throwsException() throws Exception {
        // Arrange
        final String testRole = "SomeRole";
        roleRepository.save(new Role(testRole));

        RoleRegistrationRequestModel model = new RoleRegistrationRequestModel();
        model.setName(testRole);

        // Act
        ResultActions resultActions = mockMvc.perform(post("/authentication/registerRole")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.serialize(model)));
        final Role oldRole = roleRepository.findByName(testRole).orElseThrow();

        // Assert
        resultActions.andExpect(status().isBadRequest());

        Role savedRole = roleRepository.findByName(testRole).orElseThrow();

        assertThat(savedRole.getRole()).isEqualTo(oldRole.getRole());
    }

    @Test
    public void addRoleToUser_withValidData_worksCorrectly() throws Exception {
        // Arrange
        final String testRole = "SomeRole";
        roleRepository.save(new Role(testRole));

        final MemberId testUser = new MemberId("SomeUser");
        final Password testPassword = new Password("password123");
        final HashedPassword testHashedPassword = new HashedPassword("hashedTestPassword");
        userRepository.save(new AppUser(testUser, testHashedPassword));

        when(mockPasswordEncoder.hash(testPassword)).thenReturn(testHashedPassword);

        AddRoleToUserRequestModel model = new AddRoleToUserRequestModel();
        model.setMemberId(testUser.toString());
        model.setRoleName(testRole);
        assertThat(userRepository.findByMemberId(testUser).get().getRoles()).isEmpty();

        // Act
        ResultActions resultActions = mockMvc.perform(post("/authentication/addRoleToUser")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.serialize(model)));

        // Assert
        Iterator<Role> savedRole;

        resultActions.andExpect(status().isOk());
        assertThat(userRepository.findByMemberId(testUser).get().getRoles()).hasSize(1);
        savedRole = userRepository.findByMemberId(testUser).get().getRoles().iterator();
        assertThat(savedRole.next().getRole()).isEqualTo(testRole);

        //Duplicate elimination test
        resultActions.andExpect(status().isOk());
        assertThat(userRepository.findByMemberId(testUser).get().getRoles()).hasSize(1);
        savedRole = userRepository.findByMemberId(testUser).get().getRoles().iterator();
        assertThat(savedRole.next().getRole()).isEqualTo(testRole);
    }

    @Test
    public void addRoleToUser_withInvalidData_doesNothing() throws Exception {
        // Arrange
        final String invalidRole = "InvalidRole";

        final MemberId testUser = new MemberId("SomeUser");
        final Password testPassword = new Password("password123");
        final HashedPassword testHashedPassword = new HashedPassword("hashedTestPassword");
        userRepository.save(new AppUser(testUser, testHashedPassword));

        when(mockPasswordEncoder.hash(testPassword)).thenReturn(testHashedPassword);

        AddRoleToUserRequestModel model = new AddRoleToUserRequestModel();
        model.setMemberId(testUser.toString());
        model.setRoleName(invalidRole);

        // Act
        ResultActions resultActions = mockMvc.perform(post("/authentication/addRoleToUser")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.serialize(model)));

        // Assert
        assertThat(userRepository.findByMemberId(testUser).get().getRoles()).isEmpty();

        resultActions.andExpect(status().isOk());
        assertThat(userRepository.findByMemberId(testUser).get().getRoles()).isEmpty();
    }

    @Test
    public void listUserRoles_worksCorrectly() throws Exception {
        // Arrange
        final String testRole = "SomeRole";
        final String testRole2 = "SomeOtherRole";
        roleRepository.save(new Role(testRole));
        roleRepository.save(new Role(testRole2));

        final MemberId testUser = new MemberId("SomeUser");
        final Password testPassword = new Password("password123");
        final HashedPassword testHashedPassword = new HashedPassword("hashedTestPassword");
        userRepository.save(new AppUser(testUser, testHashedPassword));

        when(mockPasswordEncoder.hash(testPassword)).thenReturn(testHashedPassword);

        AddRoleToUserRequestModel model1 = new AddRoleToUserRequestModel();
        model1.setMemberId(testUser.toString());
        model1.setRoleName(testRole);

        mockMvc.perform(post("/authentication/addRoleToUser")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.serialize(model1)));

        model1 = new AddRoleToUserRequestModel();
        model1.setMemberId(testUser.toString());
        model1.setRoleName(testRole2);

        mockMvc.perform(post("/authentication/addRoleToUser")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.serialize(model1)));

        assertThat(userRepository.findByMemberId(testUser).get().getRoles()).hasSize(2);

        ListUserRolesRequestModel model2 = new ListUserRolesRequestModel();
        model2.setMemberId(testUser.toString());
        // Act
        ResultActions resultActions = mockMvc.perform(get("/authentication/listUserRoles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.serialize(model2)));

        // Assert
        Iterator<Role> savedRole;

        String[] response = resultActions.andExpect(status().isOk()).andReturn().getResponse()
                .getContentAsString().split("},");
        assertThat(response[0]).contains("role", "SomeRole");
        assertThat(response[1]).contains("role", "SomeOtherRole");
        assertThat(response.length).isEqualTo(2);
    }

    @Test
    public void listUsers_worksCorrectly() throws Exception {
        // Arrange
        final String testRole = "SomeRole";
        final String testRole2 = "SomeOtherRole";
        roleRepository.save(new Role(testRole));
        roleRepository.save(new Role(testRole2));

        final MemberId testUser = new MemberId("SomeUser");
        final MemberId testUser2 = new MemberId("SomeOtherUser");
        final Password testPassword = new Password("password123");
        final HashedPassword testHashedPassword = new HashedPassword("hashedTestPassword");
        userRepository.save(new AppUser(testUser, testHashedPassword));
        userRepository.save(new AppUser(testUser2, testHashedPassword));

        when(mockPasswordEncoder.hash(testPassword)).thenReturn(testHashedPassword);

        AddRoleToUserRequestModel model1 = new AddRoleToUserRequestModel();
        model1.setMemberId(testUser.toString());
        model1.setRoleName(testRole);

        mockMvc.perform(post("/authentication/addRoleToUser")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.serialize(model1)));

        model1 = new AddRoleToUserRequestModel();
        model1.setMemberId(testUser.toString());
        model1.setRoleName(testRole2);

        mockMvc.perform(post("/authentication/addRoleToUser")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.serialize(model1)));

        model1 = new AddRoleToUserRequestModel();
        model1.setMemberId(testUser2.toString());
        model1.setRoleName(testRole2);

        mockMvc.perform(post("/authentication/addRoleToUser")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.serialize(model1)));

        // Act
        ResultActions resultActions = mockMvc.perform(get("/authentication/listUsers"));

        // Assert

        String response = resultActions.andExpect(status().isOk()).andReturn().getResponse()
                .getContentAsString();
        assertThat(response).contains("SomeUser SomeRole SomeOtherRole", "SomeOtherUser SomeOtherRole");
    }

    @Test
    public void listRoles_worksCorrectly() throws Exception {
        // Arrange
        final String testRole = "SomeRole";
        final String testRole2 = "SomeOtherRole";
        roleRepository.save(new Role(testRole));
        roleRepository.save(new Role(testRole2));

        // Act
        ResultActions resultActions = mockMvc.perform(get("/authentication/listRoles"));

        // Assert

        String response = resultActions.andExpect(status().isOk()).andReturn().getResponse()
                .getContentAsString();
        assertThat(response).contains("SomeRole", "SomeOtherRole");
    }

    @Test
    public void changeUserPassword_worksCorrectly() throws Exception {
        // Arrange
        final MemberId testUser = new MemberId("SomeUser");
        final Password testPassword = new Password("password123");
        final Password testNewPassword = new Password("StrongerPassword");
        final HashedPassword testHashedPassword = new HashedPassword("hashedTestPassword");
        final HashedPassword testNewHashedPassword = new HashedPassword("hashedNewTestPassword");
        userRepository.save(new AppUser(testUser, testHashedPassword));

        when(mockPasswordEncoder.hash(testPassword)).thenReturn(testHashedPassword);
        when(mockPasswordEncoder.hash(testNewPassword)).thenReturn(testNewHashedPassword);
        when(mockPasswordEncoder.match(testHashedPassword, testPassword)).thenReturn(true);

        ChangeUserPasswordRequestModel model = new ChangeUserPasswordRequestModel();
        model.setMemberId(testUser.toString());
        model.setOldPassword(testPassword.toString());
        model.setNewPassword(testNewPassword.toString());


        // Act
        ResultActions resultActions = mockMvc.perform(post("/authentication/changeUserPassword")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.serialize(model)));

        // Assert
        AppUser savedUser = userRepository.findByMemberId(testUser).orElseThrow();

        assertThat(savedUser.getMemberId()).isEqualTo(testUser);
        assertThat(savedUser.getPassword()).isEqualTo(testHashedPassword);

        resultActions.andExpect(status().isOk());
        savedUser = userRepository.findByMemberId(testUser).orElseThrow();

        assertThat(savedUser.getMemberId()).isEqualTo(testUser);
        assertThat(savedUser.getPassword()).isEqualTo(testNewHashedPassword);
    }

    @Test
    public void changeUserPassword_invalidCredentials() throws Exception {
        // Arrange
        final MemberId testUser = new MemberId("SomeUser");
        final Password testPassword = new Password("password123");
        final Password testNewPassword = new Password("StrongerPassword");
        final HashedPassword testHashedPassword = new HashedPassword("hashedTestPassword");
        final HashedPassword testNewHashedPassword = new HashedPassword("hashedNewTestPassword");
        userRepository.save(new AppUser(testUser, testHashedPassword));

        when(mockPasswordEncoder.hash(testPassword)).thenReturn(testHashedPassword);
        when(mockPasswordEncoder.hash(testNewPassword)).thenReturn(testNewHashedPassword);
        when(mockPasswordEncoder.match(testHashedPassword, testPassword)).thenReturn(false);

        ChangeUserPasswordRequestModel model = new ChangeUserPasswordRequestModel();
        model.setMemberId(testUser.toString());
        model.setOldPassword(testPassword.toString());
        model.setNewPassword(testNewPassword.toString());


        // Act
        ResultActions resultActions = mockMvc.perform(post("/authentication/changeUserPassword")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.serialize(model)));

        // Assert
        AppUser savedUser = userRepository.findByMemberId(testUser).orElseThrow();

        assertThat(savedUser.getMemberId()).isEqualTo(testUser);
        assertThat(savedUser.getPassword()).isEqualTo(testHashedPassword);

        resultActions.andExpect(status().isBadRequest());
        savedUser = userRepository.findByMemberId(testUser).orElseThrow();

        assertThat(savedUser.getMemberId()).isEqualTo(testUser);
        assertThat(savedUser.getPassword()).isEqualTo(testHashedPassword);
    }
}
