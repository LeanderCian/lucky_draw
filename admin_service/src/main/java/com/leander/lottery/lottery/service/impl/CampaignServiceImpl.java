package com.leander.lottery.lottery.service.impl;

import com.leander.lottery.lottery.dto.*;
import com.leander.lottery.lottery.entity.Campaign;
import com.leander.lottery.lottery.exception.ResourceNotFoundException;
import com.leander.lottery.lottery.repository.CampaignRepository;
import com.leander.lottery.lottery.service.CampaignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CampaignServiceImpl implements CampaignService {

    @Value("${redis.prefix.hashtag}")
    private String hashTagPrefix;

    @Value("${redis.prefix.key.campaign}")
    private String campaignKeyPrefix;

    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Transactional(rollbackFor = Exception.class)
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

    @Transactional(rollbackFor = Exception.class)
    public Campaign updateCampaign(Long campaignId, UpdateCampaignRequest req) {
        // find campaign from MySQL
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("no this campaign"));

        // 寫入 MySQL
        campaign.setName(req.getName());
        campaign.setMaxTries(req.getMaxTries());
        campaign.setStartTime(req.getStartTime());
        campaign.setEndTime(req.getEndTime());

        Campaign saved = campaignRepository.save(campaign);

        // 同步至 Redis
        syncCampaignToRedis(saved);

        return saved;
    }

    private void syncCampaignToRedis(Campaign campaign) {
        String campaignKey = "{" + hashTagPrefix + campaign.getId() + "}_" + campaignKeyPrefix + campaign.getId();
        redisTemplate.opsForValue().set(campaignKey, campaign);
    }

    @Transactional(readOnly = true)
    public CampaignResponse getCampaignById(Long id) {
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("no this campaign"));
        return new CampaignResponse(campaign);
    }
}
