package com.remotejobs.repository;

import com.remotejobs.entity.CompanySource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CompanySourceRepository extends JpaRepository<CompanySource, Long> {
    List<CompanySource> findByActiveTrue();
}