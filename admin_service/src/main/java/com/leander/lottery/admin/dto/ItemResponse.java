package com.leander.lottery.admin.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.leander.lottery.admin.entity.Campaign;
import com.leander.lottery.admin.entity.Item;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItemResponse {
    private Long id;
    @JsonProperty("campaign_id")
    private Long campaignId;
    private String name;
    private Integer probability;
    @JsonProperty("total_stock")
    private Long totalStock;
    @JsonProperty("current_stock")
    private Long currentStock;

    public ItemResponse(Item i) {
        this.id = i.getId();
        this.campaignId = i.getCampaignId();
        this.name = i.getName();
        this.probability = i.getProbability();
        this.totalStock = i.getTotalStock();
        this.currentStock = i.getCurrentStock();
    }
}