package nl.tudelft.sem.template.activity.domain.filters;

import java.util.HashSet;
import java.util.Set;
import nl.tudelft.sem.template.activity.domain.Activity;
import nl.tudelft.sem.template.activity.domain.RequiredPositions;
import nl.tudelft.sem.template.activity.domain.User;
import nl.tudelft.sem.template.activity.domain.enums.Positions;

public class PositionValidator extends BaseValidator {
    @Override
    public boolean handle(User user, Activity activity) {
        Set<Positions> positions = new HashSet<>();
        for (RequiredPositions rp : activity.getPositions()) {
            if (rp.getRequiredOfPosition() > 0) {
                positions.add(rp.getPosition());
            }
        }
        if (positions.contains(user.getPosition())) {
            return super.checkNext(user, activity);
        }
        return false;
    }
}
