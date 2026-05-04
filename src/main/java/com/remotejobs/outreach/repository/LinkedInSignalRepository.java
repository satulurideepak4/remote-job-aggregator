package com.remotejobs.outreach.repository;

import com.remotejobs.outreach.entity.LinkedInSignal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LinkedInSignalRepository extends JpaRepository<LinkedInSignal, Long> {

    List<LinkedInSignal> findAllByOrderByAddedAtDesc();
}
