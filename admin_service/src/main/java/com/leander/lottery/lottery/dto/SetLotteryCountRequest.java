package com.leander.lottery.lottery.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SetLotteryCountRequest {
    @NotNull(message = "Total Lottery Count is required")
    @Min(0)
    @JsonProperty("total_lottery_count")
    private Integer totalLotteryCount;
}