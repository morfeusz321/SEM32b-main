package nl.tudelft.sem.template.user.profiles;

import nl.tudelft.sem.template.user.domain.UserService;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Profile("mockUserService")
@Configuration
public class MockUserService {

    /**
     * Mocks the UserService.
     *
     * @return A mocked UserService.
     */
    @Bean
    @Primary  // marks this bean as the first bean to use when trying to inject a UserService
    public UserService getUserService() {
        return Mockito.mock(UserService.class);
    }
}
