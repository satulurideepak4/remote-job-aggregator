package com.remotejobs.outreach.dto;

import com.remotejobs.outreach.entity.OutreachTarget;
import lombok.Data;

@Data
public class OutreachTargetUpdateRequest {

    private OutreachTarget.OutreachStatus outreachStatus;
    private String contactName;
    private String contactRole;
    private String contactEmail;
    private String companyWebsite;
    private String notes;
}
