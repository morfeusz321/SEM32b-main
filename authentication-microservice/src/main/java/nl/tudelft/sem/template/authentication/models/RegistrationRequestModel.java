package nl.tudelft.sem.template.authentication.models;

import java.util.Collection;
import lombok.Data;
import nl.tudelft.sem.template.authentication.domain.Availability;
import nl.tudelft.sem.template.authentication.domain.Positions;
import nl.tudelft.sem.template.authentication.domain.enums.BoatType;
import nl.tudelft.sem.template.authentication.domain.enums.Gender;
import nl.tudelft.sem.template.authentication.domain.enums.Level;

/**
 * Model representing a user registration request.
 */
@Data
public class RegistrationRequestModel {
    // Information stored in authentication
    private String memberId;
    private String password;

    // Information passed to be saved on another service
    private Collection<Positions> positions;
    private String firstName;
    private String lastName;
    private String organization;
    private BoatType certificate;
    private Gender gender;
    private Level level;
    private Collection<Availability> availabilities;
}