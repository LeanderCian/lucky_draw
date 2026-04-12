package com.leander.lottery.admin.service;

import com.leander.lottery.admin.dto.*;
import com.leander.lottery.admin.entity.Campaign;

public interface CampaignService {
    public Long createCampaign(CreateCampaignRequest req);

    public Campaign updateCampaign(Long campaignId, UpdateCampaignRequest req);

    public CampaignResponse getCampaignById(Long id);
}
