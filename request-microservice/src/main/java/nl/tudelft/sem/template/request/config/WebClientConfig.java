package nl.tudelft.sem.template.request.config;

import nl.tudelft.sem.template.request.models.AuthenticationResponseModel;
import nl.tudelft.sem.template.request.models.RegistrationRequestModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Configuration
public class WebClientConfig {
    /**
     * Creates web client with token.
     *
     * @return web client.
     */
    @Bean
    public WebClient webClient() {
        WebClient webClient = WebClient.builder().build();
        RegistrationRequestModel registrationRequestModel = new RegistrationRequestModel();
        webClient.post().uri("http://localhost:8081/authentication/register").body(Mono.just(registrationRequestModel), RegistrationRequestModel.class).retrieve().bodyToMono(AuthenticationResponseModel.class).block();
        AuthenticationResponseModel response = webClient.post().uri("http://localhost:8081/authentication/authenticate").body(Mono.just(registrationRequestModel), RegistrationRequestModel.class).retrieve().bodyToMono(AuthenticationResponseModel.class).block();
        webClient = WebClient.builder().defaultHeader("Authorization", "Bearer " + response.getToken()).build();
        return webClient;
    }
}
