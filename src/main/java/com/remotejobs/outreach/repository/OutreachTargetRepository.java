package com.remotejobs.outreach.repository;

import com.remotejobs.outreach.entity.OutreachTarget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OutreachTargetRepository extends JpaRepository<OutreachTarget, Long> {

    List<OutreachTarget> findByOutreachStatusOrderByCreatedAtDesc(OutreachTarget.OutreachStatus status);

    List<OutreachTarget> findBySourceOrderByCreatedAtDesc(OutreachTarget.Source source);

    List<OutreachTarget> findByOutreachStatusAndSourceOrderByCreatedAtDesc(
            OutreachTarget.OutreachStatus status, OutreachTarget.Source source);

    List<OutreachTarget> findAllByOrderByCreatedAtDesc();

    @Query("SELECT COUNT(t) FROM OutreachTarget t WHERE t.outreachStatus = 'SENT'")
    long countSent();

    @Query("SELECT COUNT(t) FROM OutreachTarget t WHERE t.outreachStatus = 'REPLIED'")
    long countReplied();
}
