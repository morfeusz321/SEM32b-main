package nl.tudelft.sem.template.authentication.models;

import lombok.Data;

/**
 * Model representing a user registration request.
 */
@Data
public class RegistrationRequestModelTest {
    // Information stored in authentication
    private String memberId;
    private String password;
}