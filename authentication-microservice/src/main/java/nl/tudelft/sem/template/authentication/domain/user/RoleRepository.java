package nl.tudelft.sem.template.authentication.domain.user;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * A DDD repository for quering and persisting roles.
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    /**
     * Find role by name.
     */
    Optional<Role> findByName(String name);

    /**
     * Check if an existing role already uses the name.
     */
    boolean existsByName(String name);
}
