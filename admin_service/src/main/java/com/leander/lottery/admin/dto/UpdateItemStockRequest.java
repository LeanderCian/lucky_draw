package com.leander.lottery.admin.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateItemStockRequest {
    @NotNull(message = "Increment amount is required")
    @JsonProperty("increment_amount")
    Long incrementAmount;
}