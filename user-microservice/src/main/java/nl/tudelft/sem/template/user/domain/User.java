package nl.tudelft.sem.template.user.domain;

import java.util.Collection;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import nl.tudelft.sem.template.user.domain.enums.BoatType;
import nl.tudelft.sem.template.user.domain.enums.Gender;
import nl.tudelft.sem.template.user.domain.enums.Level;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class User {
    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private Gender gender;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    private Level level;

    @Column(nullable = false)
    private String organisation;

    @Column(nullable = false)
    private BoatType certificate;

    @OneToMany(cascade = CascadeType.PERSIST)
    @OrderColumn
    private Collection<Availability> availabilities;

    @OneToMany(cascade = CascadeType.PERSIST)
    @OrderColumn
    private Collection<Positions> positions;


    public Long addAvailability(Availability availability) {
        availabilities.add(availability);
        return availability.getId();
    }

    public void updateAvailability(Availability availability) {
        availabilities.removeIf(a -> a.getId().equals(availability.getId()));
        availabilities.add(availability);
    }
}
