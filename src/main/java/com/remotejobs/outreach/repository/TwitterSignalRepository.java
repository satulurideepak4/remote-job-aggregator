package com.remotejobs.outreach.repository;

import com.remotejobs.outreach.entity.TwitterSignal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TwitterSignalRepository extends JpaRepository<TwitterSignal, Long> {

    List<TwitterSignal> findAllByOrderByAddedAtDesc();
}
