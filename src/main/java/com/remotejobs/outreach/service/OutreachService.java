package com.remotejobs.outreach.service;

import com.remotejobs.outreach.dto.*;
import com.remotejobs.outreach.entity.LinkedInSignal;
import com.remotejobs.outreach.entity.OutreachTarget;
import com.remotejobs.outreach.entity.TwitterSignal;
import com.remotejobs.outreach.repository.LinkedInSignalRepository;
import com.remotejobs.outreach.repository.OutreachTargetRepository;
import com.remotejobs.outreach.repository.TwitterSignalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutreachService {

    private final LinkedInSignalRepository linkedInRepo;
    private final TwitterSignalRepository twitterRepo;
    private final OutreachTargetRepository targetRepo;

    // ── LinkedIn Signals ──────────────────────────────────────────────────────────

    public LinkedInSignal addLinkedInSignal(LinkedInSignalRequest req) {
        LinkedInSignal signal = LinkedInSignal.builder()
                .postUrl(req.getPostUrl())
                .authorName(req.getAuthorName())
                .companyName(req.getCompanyName())
                .postSnippet(req.getPostSnippet())
                .hiringSignal(req.getHiringSignal())
                .build();
        log.info("[Outreach] LinkedIn signal added for company={}", req.getCompanyName());
        return linkedInRepo.save(signal);
    }

    public List<LinkedInSignal> getLinkedInSignals() {
        return linkedInRepo.findAllByOrderByAddedAtDesc();
    }

    // ── Twitter Signals ───────────────────────────────────────────────────────────

    public TwitterSignal addTwitterSignal(TwitterSignalRequest req) {
        TwitterSignal signal = TwitterSignal.builder()
                .tweetUrl(req.getTweetUrl())
                .authorHandle(req.getAuthorHandle())
                .authorName(req.getAuthorName())
                .companyName(req.getCompanyName())
                .tweetSnippet(req.getTweetSnippet())
                .hiringSignal(req.getHiringSignal())
                .build();
        log.info("[Outreach] Twitter signal added for company={}", req.getCompanyName());
        return twitterRepo.save(signal);
    }

    public List<TwitterSignal> getTwitterSignals() {
        return twitterRepo.findAllByOrderByAddedAtDesc();
    }

    // ── Outreach Targets ──────────────────────────────────────────────────────────

    public OutreachTarget addTarget(OutreachTargetRequest req) {
        OutreachTarget target = OutreachTarget.builder()
                .companyName(req.getCompanyName())
                .source(req.getSource() != null ? req.getSource() : OutreachTarget.Source.MANUAL)
                .companyWebsite(req.getCompanyWebsite())
                .contactName(req.getContactName())
                .contactRole(req.getContactRole())
                .contactEmail(req.getContactEmail())
                .notes(req.getNotes())
                .outreachStatus(OutreachTarget.OutreachStatus.PENDING)
                .build();
        log.info("[Outreach] Target added company={} source={}", req.getCompanyName(), req.getSource());
        return targetRepo.save(target);
    }

    public List<OutreachTarget> getTargets(String status, String source) {
        OutreachTarget.OutreachStatus parsedStatus = parseStatus(status);
        OutreachTarget.Source parsedSource = parseSource(source);

        if (parsedStatus != null && parsedSource != null) {
            return targetRepo.findByOutreachStatusAndSourceOrderByCreatedAtDesc(parsedStatus, parsedSource);
        }
        if (parsedStatus != null) {
            return targetRepo.findByOutreachStatusOrderByCreatedAtDesc(parsedStatus);
        }
        if (parsedSource != null) {
            return targetRepo.findBySourceOrderByCreatedAtDesc(parsedSource);
        }
        return targetRepo.findAllByOrderByCreatedAtDesc();
    }

    @Transactional
    public OutreachTarget updateTarget(Long id, OutreachTargetUpdateRequest req) {
        OutreachTarget target = targetRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Outreach target not found: " + id));

        if (req.getOutreachStatus() != null) target.setOutreachStatus(req.getOutreachStatus());
        if (req.getContactName() != null) target.setContactName(req.getContactName());
        if (req.getContactRole() != null) target.setContactRole(req.getContactRole());
        if (req.getContactEmail() != null) target.setContactEmail(req.getContactEmail());
        if (req.getCompanyWebsite() != null) target.setCompanyWebsite(req.getCompanyWebsite());
        if (req.getNotes() != null) target.setNotes(req.getNotes());

        return targetRepo.save(target);
    }

    public void deleteTarget(Long id) {
        if (!targetRepo.existsById(id)) {
            throw new RuntimeException("Outreach target not found: " + id);
        }
        targetRepo.deleteById(id);
        log.info("[Outreach] Target deleted id={}", id);
    }

    public OutreachStatsDto getStats() {
        long total = targetRepo.count();
        long sent = targetRepo.countSent();
        long replied = targetRepo.countReplied();
        double replyRate = sent > 0 ? Math.round((double) replied / sent * 1000.0) / 10.0 : 0.0;
        return new OutreachStatsDto(total, sent, replied, replyRate);
    }

    private OutreachTarget.OutreachStatus parseStatus(String status) {
        if (status == null || status.isBlank()) return null;
        try { return OutreachTarget.OutreachStatus.valueOf(status.toUpperCase()); }
        catch (IllegalArgumentException e) { return null; }
    }

    private OutreachTarget.Source parseSource(String source) {
        if (source == null || source.isBlank()) return null;
        try { return OutreachTarget.Source.valueOf(source.toUpperCase()); }
        catch (IllegalArgumentException e) { return null; }
    }
}
