package nl.tudelft.sem.template.activity.domain;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * A DDD repository for querying and persisting user aggregate roots.
 */
@Repository
public interface ActivityRepository extends JpaRepository<Activity, String> {
    /**
     * Find activity by its ID.
     */
    Optional<Activity> findByActivityId(int activityId);

    /**
     * Check if an existing activity already uses an ID.
     */
    boolean existsByActivityId(int activityId);
}

