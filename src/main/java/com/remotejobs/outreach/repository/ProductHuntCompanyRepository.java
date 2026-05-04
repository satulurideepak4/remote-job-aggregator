package com.remotejobs.outreach.repository;

import com.remotejobs.outreach.entity.ProductHuntCompany;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductHuntCompanyRepository extends JpaRepository<ProductHuntCompany, Long> {

    Optional<ProductHuntCompany> findByExternalId(String externalId);

    List<ProductHuntCompany> findByLaunchDateAfterOrderByLaunchDateDesc(LocalDateTime since);

    List<ProductHuntCompany> findAllByOrderByLaunchDateDesc();
}
