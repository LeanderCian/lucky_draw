package com.leander.lottery.admin.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateItemRequest {
    @NotNull(message = "Campaign Id is required")
    @JsonProperty("campaign_id")
    private Long campaignId;
    @NotBlank(message = "Name is required")
    private String name;
    @NotNull(message = "Probability is required")
    @Min(0)
    @Max(100000000)
    private Integer probability;
    @NotNull
    @Min(0)
    @JsonProperty("total_stock")
    Long totalStock;
}