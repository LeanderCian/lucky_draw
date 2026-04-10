package com.leander.lottery.admin.service.impl;

import com.leander.lottery.admin.dto.*;
import com.leander.lottery.admin.entity.Item;
import com.leander.lottery.admin.exception.ProbabilityExceededException;
import com.leander.lottery.admin.exception.ResourceNotFoundException;
import com.leander.lottery.admin.repository.CampaignRepository;
import com.leander.lottery.admin.repository.ItemRepository;
import com.leander.lottery.admin.service.ItemService;
import com.leander.lottery.admin.util.LuaUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ItemServiceImpl implements ItemService {

    @Value("${redis.key.prefix.campaign.itemlist}")
    private String campaignItemListKeyPrefix;

    @Value("${redis.key.prefix.item.stock}")
    private String itemStockKeyPrefix;

    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Transactional
    public Long createItem(CreateItemRequest req) {
        //  檢查活動是否存在
        if (!campaignRepository.existsById(req.getCampaignId())) {
            throw new ResourceNotFoundException("no this campaign");
        }

        // 檢查總機率是否超過 100%
        int totalProbability = itemRepository.sumProbabilityByCampaignId(req.getCampaignId());
        if (totalProbability + req.getProbability() > 100000000) {
            throw new ProbabilityExceededException("probability exceeded");
        }

        // 寫入 MySQL
        Item item = new Item();
        item.setCampaignId(req.getCampaignId());
        item.setName(req.getName());
        item.setProbability(req.getProbability());
        item.setTotalStock(req.getTotalStock());
        item.setCurrentStock(req.getTotalStock());

        Item saved = itemRepository.save(item);

        // 同步至 Redis
        insertItemToRedis(saved);

        return saved.getId();
    }

    private void insertItemToRedis(Item item) {
        String campaignItemListKey = campaignItemListKeyPrefix + item.getCampaignId();
        String itemStockKey = itemStockKeyPrefix + item.getId();
        String itemHashKey = item.getId().toString();
        List<String> keys = new ArrayList<>();
        keys.add(itemStockKey);
        keys.add(campaignItemListKey);
        keys.add(itemHashKey);

        // 準備腳本執行器
        DefaultRedisScript<Boolean> script = new DefaultRedisScript<>(LuaUtils.INSERT_ITEM_LUA, Boolean.class);

        // 執行腳本
        try {
            redisTemplate.execute(
                    script,
                    keys,
                    item.getTotalStock(),
                    item
            );
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("insert item to Redis failed", e);
        }
    }

    public ItemResponse getItemById(Long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("no this item"));
        return new ItemResponse(item);
    }
}
