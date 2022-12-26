package nl.tudelft.sem.template.user.domain;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nl.tudelft.sem.template.user.domain.enums.Position;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class RequiredPositions {
    private int id;

    // Position that needs to be filled
    private Position position;

    // Amount of the needed positions required
    private int requiredOfPosition;


    /**
     * Constructor method.
     *
     * @param position Position that needs to be filled
     * @param requiredOfPosition Amount of the needed positions required
     */
    public RequiredPositions(Position position, int requiredOfPosition) {
        this.position = position;
        this.requiredOfPosition = requiredOfPosition;
    }
}
