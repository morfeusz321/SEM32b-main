package nl.tudelft.sem.template.activity.domain.filters;

import nl.tudelft.sem.template.activity.domain.Activity;
import nl.tudelft.sem.template.activity.domain.User;

public class CertificateValidator extends BaseValidator {
    @Override
    public boolean handle(User user, Activity activity) {
        if (user.getCertificate().label < activity.getBoatType().label) {
            return false;
        }
        return super.checkNext(user, activity);
    }
}
