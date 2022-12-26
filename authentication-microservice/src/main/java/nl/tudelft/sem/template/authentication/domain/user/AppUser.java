package nl.tudelft.sem.template.authentication.domain.user;

import static javax.persistence.FetchType.EAGER;
import static javax.persistence.GenerationType.AUTO;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import lombok.NoArgsConstructor;
import nl.tudelft.sem.template.authentication.domain.HasEvents;

/**
 * A DDD entity representing an application user in our domain.
 */
@Entity
@Table(name = "users")
@NoArgsConstructor
public class AppUser extends HasEvents {
    /**
     * Identifier for the application user in the DB.
     */
    @Id @GeneratedValue(strategy = AUTO)
    @Column(name = "id", nullable = false)
    private int id;

    /**
     * Identifier used by app user at login.
     */
    @Column(name = "member_id", nullable = false, unique = true)
    @Convert(converter = MemberIdAttributeConverter.class)
    private MemberId memberId;

    @Column(name = "password_hash", nullable = false)
    @Convert(converter = HashedPasswordAttributeConverter.class)
    private HashedPassword password;

    @Column(name = "role_list", nullable = false)
    @ManyToMany(fetch = EAGER)
    private Collection<Role> roles = new ArrayList<>();
    /**
     * Create new application user.
     *
     * @param memberId The NetId for the new user
     * @param password The password for the new user
     */

    public AppUser(MemberId memberId, HashedPassword password) {
        this.memberId = memberId;
        this.password = password;
        this.recordThat(new UserWasCreatedEvent(memberId));
    }

    public void changePassword(HashedPassword password) {
        this.password = password;
        this.recordThat(new PasswordWasChangedEvent(this));
    }

    public MemberId getMemberId() {
        return memberId;
    }

    public HashedPassword getPassword() {
        return password;
    }

    public Collection<Role> getRoles() {
        return roles;
    }

    /**
     * Equality is only based on the identifier.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AppUser appUser = (AppUser) o;
        return id == (appUser.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(memberId);
    }
}
