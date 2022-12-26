package nl.tudelft.sem.template.activity.domain;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import nl.tudelft.sem.template.activity.domain.enums.BoatType;
import nl.tudelft.sem.template.activity.domain.enums.Gender;
import nl.tudelft.sem.template.activity.domain.enums.Level;
import nl.tudelft.sem.template.activity.domain.enums.Positions;

@Data
@AllArgsConstructor
public class User {
    // The id of the user.
    Long id;

    // The username of the user.
    String username;

    // The firstname of the user.
    private String firstName;

    // The lastname of the user.
    private String lastName;

    // The organization of which the user is part of.
    String organisation;

    // The boat certificate of the user.
    BoatType certificate;

    // The gender of the user.
    Gender gender;

    // The level of the user.
    Level level;

    // The position for which the user applied.
    Positions position;

    // A list of availabilities of a user.
    List<Availability> availabilities;
}
