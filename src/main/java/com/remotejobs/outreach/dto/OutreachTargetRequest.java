package com.remotejobs.outreach.dto;

import com.remotejobs.outreach.entity.OutreachTarget;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OutreachTargetRequest {

    @NotBlank
    private String companyName;

    private OutreachTarget.Source source = OutreachTarget.Source.MANUAL;
    private String companyWebsite;
    private String contactName;
    private String contactRole;
    private String contactEmail;
    private String notes;
}
