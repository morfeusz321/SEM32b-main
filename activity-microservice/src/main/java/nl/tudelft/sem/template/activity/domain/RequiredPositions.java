package nl.tudelft.sem.template.activity.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nl.tudelft.sem.template.activity.domain.enums.Positions;

@Entity
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
public class RequiredPositions {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    // Position that needs to be filled
    private Positions position;

    // Amount of the needed positions required
    private int requiredOfPosition;


    /**
     * Constructor method.
     *
     * @param position Position that needs to be filled
     * @param requiredOfPosition Amount of the needed positions required
     */
    public RequiredPositions(Positions position, int requiredOfPosition) {
        this.position = position;
        this.requiredOfPosition = requiredOfPosition;
    }

    /**
     * Fills a position by decreasing the number of positions of the type needed.
     *
     */
    public boolean fillPosition() {
        if (this.requiredOfPosition == 0) {
            return false;
        }
        this.requiredOfPosition--;
        return true;
    }
}
