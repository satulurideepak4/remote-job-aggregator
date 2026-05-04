package com.remotejobs.outreach.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OutreachStatsDto {
    private long totalTracked;
    private long outreachSent;
    private long repliesReceived;
    private double replyRate;
}
