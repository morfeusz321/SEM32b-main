package nl.tudelft.sem.template.user.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import nl.tudelft.sem.template.user.authentication.AuthManager;
import nl.tudelft.sem.template.user.domain.enums.BoatType;
import nl.tudelft.sem.template.user.domain.enums.Gender;
import nl.tudelft.sem.template.user.domain.enums.Level;
import nl.tudelft.sem.template.user.models.UserRequestModel;
import nl.tudelft.sem.template.user.models.UserResponseModel;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;



class UserServiceTest {
    private final UserRepository userRepository = mock(UserRepository.class);

    private final AuthManager authManager = mock(AuthManager.class);
    private final WebClient webClient = mock(WebClient.class);

    private final UserService userService = new UserService(userRepository, authManager, webClient);

    @Test
    void findByNetId() {
        Positions position = new Positions();
        Availability availability = new Availability();
        when(userRepository.findById(10L)).thenReturn(new User(10L, "abc", Gender.MALE,
                "test", "test", Level.AMATEUR, "org", BoatType.C4, List.of(availability), List.of(position)));
        UserResponseModel userResponseModel = userService.findById(10L);
        assertEquals("abc", userResponseModel.getUsername());
        assertEquals("test", userResponseModel.getFirstName());
        assertEquals("test", userResponseModel.getLastName());
        assertEquals(Level.AMATEUR, userResponseModel.getLevel());
        assertEquals("org", userResponseModel.getOrganisation());
        assertEquals(BoatType.C4, userResponseModel.getCertificate());
        assertEquals(Gender.MALE, userResponseModel.getGender());
        assertEquals(List.of(position), userResponseModel.getPositions());
    }

    @Test
    void existsByNetId() {
        when(userRepository.existsById(10L)).thenReturn(true);
        assertTrue(userService.existsById(10L));
    }

    @Test
    void save() {

        User user = User.builder().build();
        user.setId(10L);
        UserResponseModel userResponseModel = UserResponseModel.builder()
                .id(10L)
                .build();
        UserRequestModel userRequestModel = UserRequestModel.builder()
                .build();
        when(userRepository.save(any(User.class))).thenReturn(user);
        assertEquals(userResponseModel, userService.save(userRequestModel));
    }

    @Test
    void delete() {
        User user = new User();
        user.setId(10L);
        UserRequestModel userRequestModel = UserRequestModel.builder()
                .build();
        doNothing().when(userRepository).delete(any(User.class));
        userService.delete(user);
        verify(userRepository, times(1)).delete(any(User.class));
    }

    @Test
    void deleteByNetId() {
        doNothing().when(userRepository).deleteById(10L);
        userService.deleteById(10L);
        verify(userRepository, times(1)).deleteById(10L);
    }

    @Test
    void findAll() {
        when(userRepository.findAll()).thenReturn(List.of(new User()));
        assertEquals(1, userService.findAll().size());
    }

    @Test
    void findAllByNetId() {
        when(userRepository.findAllById(10L)).thenReturn(List.of(new User()));
        assertEquals(1, userService.findAllById(10L).size());
    }
}
