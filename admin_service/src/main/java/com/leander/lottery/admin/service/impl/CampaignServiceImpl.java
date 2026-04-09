package com.leander.lottery.admin.service.impl;

import com.leander.lottery.admin.dto.*;
import com.leander.lottery.admin.entity.Campaign;
import com.leander.lottery.admin.exception.ResourceNotFoundException;
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

    public CampaignResponse getCampaignById(Long id) {
        // 1. 查詢資料庫，若找不到則拋出異常 (後續由 GlobalExceptionHandler 轉為 404)
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("no this campaign"));

        // 2. 轉換 Entity 為 DTO
        return new CampaignResponse(campaign);
    }
}
