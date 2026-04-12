package com.leander.lottery.lottery.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class LotteryCountResponse {
    private Long campaignId;
    @JsonProperty("users")
    private List<UserCount> userCountList;

    @Getter
    @Setter
    @AllArgsConstructor
    public static class UserCount {
        private Long id;
        @JsonProperty("total_lottery_count")
        private Integer totalLotteryCount;
        @JsonProperty("remaining_lottery_count")
        private Integer remainingLotteryCount;
    }
}
