package com.leander.lottery.lottery.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.leander.lottery.lottery.entity.Campaign;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CampaignResponse {
    private Long id;
    private String name;
    @JsonProperty("max_tries")
    private Integer maxTries;
    @JsonProperty("start_time")
    private Long startTime;
    @JsonProperty("end_time")
    private Long endTime;

    public CampaignResponse(Campaign c) {
        this.id = c.getId();
        this.name = c.getName();
        this.maxTries = c.getMaxTries();
        this.startTime = c.getStartTime();
        this.endTime = c.getEndTime();
    }
}