package nl.tudelft.sem.template.activity.domain;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * A DDD repository for querying and persisting user aggregate roots.
 */
@Repository
public interface RequiredPositionsRepository extends JpaRepository<RequiredPositions, String> {
    /**
     * Find position by its ID.
     */
    Optional<RequiredPositions> findById(int positionId);

    /**
     * Check if an existing position already uses an ID.
     */
    boolean existsById(int positionId);
}