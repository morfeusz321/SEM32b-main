package nl.tudelft.sem.template.activity.domain.filters;

import nl.tudelft.sem.template.activity.domain.Activity;
import nl.tudelft.sem.template.activity.domain.User;

public abstract class BaseValidator implements Validator {
    // The next validator in the chain of responsibility
    private transient Validator next;

    public void setNext(Validator h) {
        this.next = h;
    }

    /**
     * Runs check on the next object in chain or ends traversing f we are in last object in chain.
     *
     * @param user the user for which to check the requirements
     * @param activity the activity for which to check the requirements.
     * @return if the user matches the requirement(s) for the activity.
     */
    protected boolean checkNext(User user, Activity activity) {
        if (next == null) {
            return true;
        }
        return next.handle(user, activity);
    }

}
