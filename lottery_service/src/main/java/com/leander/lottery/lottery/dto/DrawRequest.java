package com.leander.lottery.lottery.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DrawRequest {
    @NotNull(message = "Campaign id is required")
    @JsonProperty("campaign_id")
    private Long campaignId;
    @NotNull(message = "Count is required")
    @Min(1)
    private Integer count;
}