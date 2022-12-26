package nl.tudelft.sem.template.authentication.application.user;

import nl.tudelft.sem.template.authentication.domain.user.RoleWasCreatedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * This event listener is automatically called when a domain entity is saved
 * which has stored events of type: RoleWasCreated.
 */
@Component
public class RoleWasCreatedListener {
    /**
     * The name of the function indicated which event is listened to.
     * The format is onEVENTNAME.
     *
     * @param event The event to react to
     */
    @EventListener
    public void onRoleWasCreated(RoleWasCreatedEvent event) {
        // Handler code here
        System.out.println("Role (" + event.getName() + ") was created.");
    }
}
