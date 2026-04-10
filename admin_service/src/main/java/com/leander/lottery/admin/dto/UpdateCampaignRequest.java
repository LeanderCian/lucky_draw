package com.leander.lottery.admin.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateCampaignRequest {
    @NotBlank(message = "Name is required")
    private String name;
    @NotNull(message = "Max tries is required")
    @Min(1)
    @JsonProperty("max_tries")
    private Integer maxTries;
    @NotNull(message = "Start time is required")
    @JsonProperty("start_time")
    private Long startTime;
    @NotNull(message = "end time is required")
    @JsonProperty("end_time")
    private Long endTime;
}