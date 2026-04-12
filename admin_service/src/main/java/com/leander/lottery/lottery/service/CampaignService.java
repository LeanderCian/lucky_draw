package com.leander.lottery.lottery.service;

import com.leander.lottery.lottery.dto.*;
import com.leander.lottery.lottery.entity.Campaign;

public interface CampaignService {
    public Long createCampaign(CreateCampaignRequest req);

    public Campaign updateCampaign(Long campaignId, UpdateCampaignRequest req);

    public CampaignResponse getCampaignById(Long id);
}
