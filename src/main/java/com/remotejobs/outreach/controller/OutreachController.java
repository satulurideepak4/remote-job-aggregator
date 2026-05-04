package com.remotejobs.outreach.controller;

import com.remotejobs.outreach.dto.*;
import com.remotejobs.outreach.entity.LinkedInSignal;
import com.remotejobs.outreach.entity.OutreachTarget;
import com.remotejobs.outreach.entity.ProductHuntCompany;
import com.remotejobs.outreach.entity.TwitterSignal;
import com.remotejobs.outreach.service.OutreachService;
import com.remotejobs.outreach.service.ProductHuntService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/outreach")
@RequiredArgsConstructor
@Tag(name = "Outreach", description = "Outreach dashboard — signals and tracker endpoints")
@CrossOrigin(origins = "*")
public class OutreachController {

    private final OutreachService outreachService;
    private final ProductHuntService productHuntService;

    // ── Product Hunt ──────────────────────────────────────────────────────────────

    @GetMapping("/product-hunt")
    @Operation(summary = "Get Product Hunt launches")
    public ResponseEntity<List<ProductHuntCompany>> getProductHuntCompanies(
            @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(productHuntService.getRecent(days));
    }

    @PostMapping("/product-hunt/fetch")
    @Operation(summary = "Manually trigger Product Hunt fetch")
    public ResponseEntity<List<ProductHuntCompany>> triggerProductHuntFetch() {
        return ResponseEntity.ok(productHuntService.fetchAndStore());
    }

    // ── LinkedIn Signals ──────────────────────────────────────────────────────────

    @GetMapping("/linkedin-signals")
    @Operation(summary = "Get all LinkedIn hiring signals")
    public ResponseEntity<List<LinkedInSignal>> getLinkedInSignals() {
        return ResponseEntity.ok(outreachService.getLinkedInSignals());
    }

    @PostMapping("/linkedin-signal")
    @Operation(summary = "Add a LinkedIn hiring signal (manual ingestion)")
    public ResponseEntity<LinkedInSignal> addLinkedInSignal(
            @Valid @RequestBody LinkedInSignalRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(outreachService.addLinkedInSignal(request));
    }

    // ── Twitter Signals ───────────────────────────────────────────────────────────

    @GetMapping("/twitter-signals")
    @Operation(summary = "Get all Twitter/X hiring signals")
    public ResponseEntity<List<TwitterSignal>> getTwitterSignals() {
        return ResponseEntity.ok(outreachService.getTwitterSignals());
    }

    @PostMapping("/twitter-signal")
    @Operation(summary = "Add a Twitter/X hiring signal (manual ingestion)")
    public ResponseEntity<TwitterSignal> addTwitterSignal(
            @Valid @RequestBody TwitterSignalRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(outreachService.addTwitterSignal(request));
    }

    // ── Outreach Targets ──────────────────────────────────────────────────────────

    @GetMapping("/targets")
    @Operation(summary = "List outreach targets with optional status/source filters")
    public ResponseEntity<List<OutreachTarget>> getTargets(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String source) {
        return ResponseEntity.ok(outreachService.getTargets(status, source));
    }

    @PostMapping("/targets")
    @Operation(summary = "Add a company to outreach tracker")
    public ResponseEntity<OutreachTarget> addTarget(
            @Valid @RequestBody OutreachTargetRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(outreachService.addTarget(request));
    }

    @PatchMapping("/targets/{id}")
    @Operation(summary = "Update outreach target (status, contact info, notes)")
    public ResponseEntity<OutreachTarget> updateTarget(
            @PathVariable Long id,
            @RequestBody OutreachTargetUpdateRequest request) {
        return ResponseEntity.ok(outreachService.updateTarget(id, request));
    }

    @DeleteMapping("/targets/{id}")
    @Operation(summary = "Remove an outreach target")
    public ResponseEntity<Void> deleteTarget(@PathVariable Long id) {
        outreachService.deleteTarget(id);
        return ResponseEntity.noContent().build();
    }

    // ── Stats ─────────────────────────────────────────────────────────────────────

    @GetMapping("/stats")
    @Operation(summary = "Get outreach dashboard stats")
    public ResponseEntity<OutreachStatsDto> getStats() {
        return ResponseEntity.ok(outreachService.getStats());
    }
}
