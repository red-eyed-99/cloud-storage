package ru.redeyed.cloudstorage.resource;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, Long> {

    @Query("SELECT r FROM Resource r WHERE r.userId = :userId AND r.path = :path AND r.name = :name")
    Optional<Resource> findBy(UUID userId, String path, String name);

    @Modifying
    @Query("DELETE FROM Resource WHERE userId = :userId AND path = :path AND name = :name")
    void deleteBy(UUID userId, String path, String name);

    @Query("""
            SELECT EXISTS(
                SELECT 1 FROM Resource WHERE userId = :userId AND path = :path AND name = :name AND type = :type
            )
            """)
    boolean exists(UUID userId, String path, String name, ResourceType type);
}
