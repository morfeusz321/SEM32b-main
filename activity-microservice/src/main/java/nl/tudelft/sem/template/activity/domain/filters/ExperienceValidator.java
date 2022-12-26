package nl.tudelft.sem.template.activity.domain.filters;

import nl.tudelft.sem.template.activity.domain.Activity;
import nl.tudelft.sem.template.activity.domain.Competition;
import nl.tudelft.sem.template.activity.domain.User;

public class ExperienceValidator extends BaseValidator {
    @Override
    public boolean handle(User user, Activity activity) {
        if (user.getLevel().label > ((Competition) activity).getAllowedLevel().label) {
            return false;
        }
        return super.checkNext(user, activity);
    }
}
