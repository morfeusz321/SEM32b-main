package nl.tudelft.sem.template.activity.domain.filters;

import nl.tudelft.sem.template.activity.domain.Activity;
import nl.tudelft.sem.template.activity.domain.Competition;
import nl.tudelft.sem.template.activity.domain.User;

public class OrganizationValidator extends BaseValidator {
    @Override
    public boolean handle(User user, Activity activity) {
        if (!user.getOrganisation().equals(((Competition) activity).getOrganization())) {
            return false;
        }
        return super.checkNext(user, activity);
    }
}
