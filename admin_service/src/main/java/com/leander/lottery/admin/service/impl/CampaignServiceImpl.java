package com.leander.lottery.admin.service.impl;

import com.leander.lottery.admin.dto.*;
import com.leander.lottery.admin.entity.Campaign;
import com.leander.lottery.admin.entity.Item;
import com.leander.lottery.admin.exception.ResourceNotFoundException;
import com.leander.lottery.admin.repository.CampaignRepository;
import com.leander.lottery.admin.service.CampaignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CampaignServiceImpl implements CampaignService {

    @Value("${redis.key.prefix.campaign}")
    private String campaignKeyPrefix;

    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Transactional
    public Long createCampaign(CreateCampaignRequest req) {
        // 寫入 MySQL
        Campaign campaign = new Campaign();
        campaign.setName(req.getName());
        campaign.setMaxTries(req.getMaxTries());
        campaign.setStartTime(req.getStartTime());
        campaign.setEndTime(req.getEndTime());

        Campaign saved = campaignRepository.save(campaign);

        // 同步至 Redis
        syncCampaignToRedis(saved);

        return saved.getId();
    }

    private void syncCampaignToRedis(Campaign campaign) {
        String campaignKey = campaignKeyPrefix + campaign.getId();
        redisTemplate.opsForValue().set(campaignKey, campaign);
    }

    public CampaignResponse getCampaignById(Long id) {
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("no this campaign"));
        return new CampaignResponse(campaign);
    }
}
