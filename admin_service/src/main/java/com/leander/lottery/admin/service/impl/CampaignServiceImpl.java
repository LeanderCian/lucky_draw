package com.leander.lottery.admin.service.impl;

import com.leander.lottery.admin.dto.*;
import com.leander.lottery.admin.entity.Campaign;
import com.leander.lottery.admin.repository.CampaignRepository;
import com.leander.lottery.admin.service.CampaignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CampaignServiceImpl implements CampaignService {

    @Autowired
    private CampaignRepository campaignRepository;

    @Transactional
    public Long createCampaign(CreateCampaignRequest req) {
        Campaign entity = new Campaign();
        entity.setName(req.getName());
        entity.setStatus(req.getStatus());
        entity.setMaxTries(req.getMaxTries());
        entity.setStartTime(req.getStartTime());
        entity.setEndTime(req.getEndTime());

        Campaign saved = campaignRepository.save(entity);
        return saved.getId();
    }
}
