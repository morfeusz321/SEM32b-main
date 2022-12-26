package nl.tudelft.sem.template.authentication.domain.user;

import static javax.persistence.GenerationType.AUTO;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import nl.tudelft.sem.template.authentication.domain.HasEvents;

/**
 * A DDD entity representing the roles an application user in our domain can have.
 * Each role has a set of low-level privileges
 */
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Role extends HasEvents {
    @Id
    @GeneratedValue(strategy = AUTO)
    @Column(name = "id", nullable = false)
    private int id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    public Role(String name) {
        this.name = name;
        this.recordThat(new RoleWasCreatedEvent(name));
    }

    public String getRole() {
        return name;
    }
}
