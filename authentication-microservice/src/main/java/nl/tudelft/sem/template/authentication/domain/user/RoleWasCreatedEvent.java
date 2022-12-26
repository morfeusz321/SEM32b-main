package nl.tudelft.sem.template.authentication.domain.user;

/**
 * A DDD domain event that indicated a role was created.
 */
public class RoleWasCreatedEvent {
    private final String name;

    public RoleWasCreatedEvent(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}
