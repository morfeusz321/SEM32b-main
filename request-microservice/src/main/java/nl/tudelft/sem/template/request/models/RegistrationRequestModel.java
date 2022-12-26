package nl.tudelft.sem.template.request.models;

import java.util.ArrayList;
import java.util.Collection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * Model representing a user registration request.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegistrationRequestModel {
    public static final String ADMIN = "admin";
    private String memberId = ADMIN;
    private String password = ADMIN;

    // Information passed to be saved on another service
    private Collection<Object> positions = new ArrayList<>();
    private String firstName = ADMIN;
    private String lastName = ADMIN;
    private String organization = ADMIN;
    private String certificate = "C4";
    private String gender = "MALE";
    private String level = "AMATEUR";
    private Collection<Object> availabilities = new ArrayList<>();
}