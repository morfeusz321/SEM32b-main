package nl.tudelft.sem.template.activity.domain.filters;

import nl.tudelft.sem.template.activity.domain.Activity;
import nl.tudelft.sem.template.activity.domain.User;

public interface Validator {
    void setNext(Validator handler);

    boolean handle(User user, Activity activity);
}
