package nl.tudelft.sem.template.user.models;

import java.util.Collection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import nl.tudelft.sem.template.user.domain.Availability;
import nl.tudelft.sem.template.user.domain.Positions;
import nl.tudelft.sem.template.user.domain.enums.BoatType;
import nl.tudelft.sem.template.user.domain.enums.Gender;
import nl.tudelft.sem.template.user.domain.enums.Level;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserRequestModel {

    private Collection<Positions> positions;

    private String username;

    private Gender gender;

    private String firstName;

    private String lastName;

    private Level level;

    private String organisation;

    private BoatType certificate;

    private Collection<Availability> availabilities;
}

