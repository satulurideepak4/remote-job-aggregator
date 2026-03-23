package com.remotejobs.controller;

import com.remotejobs.entity.Subscription;
import com.remotejobs.repository.SubscriptionRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
@Tag(name = "Subscriptions", description = "Manage email/Telegram job alert subscriptions")
@CrossOrigin(origins = "*")
public class SubscriptionController {

    private final SubscriptionRepository subscriptionRepo;

    @GetMapping
    public ResponseEntity<List<Subscription>> list() {
        return ResponseEntity.ok(subscriptionRepo.findAll());
    }

    @PostMapping
    @Operation(summary = "Subscribe to job alerts (EMAIL or TELEGRAM)")
    public ResponseEntity<Subscription> subscribe(@RequestBody Subscription subscription) {
        subscription.setActive(true);
        return ResponseEntity.ok(subscriptionRepo.save(subscription));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> unsubscribe(@PathVariable Long id) {
        subscriptionRepo.findById(id).ifPresent(s -> {
            s.setActive(false);
            subscriptionRepo.save(s);
        });
        return ResponseEntity.noContent().build();
    }
}