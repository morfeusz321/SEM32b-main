package nl.tudelft.sem.template.user.models;


import java.util.Collection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nl.tudelft.sem.template.user.domain.Availability;
import nl.tudelft.sem.template.user.domain.Positions;
import nl.tudelft.sem.template.user.domain.enums.BoatType;
import nl.tudelft.sem.template.user.domain.enums.Gender;
import nl.tudelft.sem.template.user.domain.enums.Level;



@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class UserResponseModel {
    private Long id;

    private String username;

    private Collection<Positions> positions;

    private Gender gender;

    private String firstName;

    private String lastName;

    private Level level;

    private Collection<Availability> availabilities;

    private String organisation;

    private BoatType certificate;
}

