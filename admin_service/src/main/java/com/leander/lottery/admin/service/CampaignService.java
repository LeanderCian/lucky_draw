package com.leander.lottery.admin.service;

import com.leander.lottery.admin.dto.*;

public interface CampaignService {
    public Long createCampaign(CreateCampaignRequest req);

    public CampaignResponse getCampaignById(Long id);
}
