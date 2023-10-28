package org.teamseven.hms.backend.catalog.entity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ServiceRepository extends CrudRepository<Service, UUID> {
    @Query(
            value = "SELECT *" +
                    "from services where type = :serviceType " +
                    "AND is_active = 1 "+
                    "ORDER BY name",
            nativeQuery=true
    )
    Page<Service> findAvailableServices(
            @Param("serviceType") String serviceType,
            Pageable pageable
    );

    Optional<Service> findByDoctorId(UUID doctorId);
}
