package com.leander.lottery.lottery.service.impl;

import com.leander.lottery.lottery.dto.*;
import com.leander.lottery.lottery.entity.Item;
import com.leander.lottery.lottery.exception.ProbabilityExceededException;
import com.leander.lottery.lottery.exception.ResourceNotFoundException;
import com.leander.lottery.lottery.exception.StockNotEnoughException;
import com.leander.lottery.lottery.repository.CampaignRepository;
import com.leander.lottery.lottery.repository.ItemRepository;
import com.leander.lottery.lottery.service.ItemService;
import com.leander.lottery.lottery.util.LuaUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ItemServiceImpl implements ItemService {

    @Value("${redis.prefix.hashtag}")
    private String hashTagPrefix;

    @Value("${redis.prefix.key.campaign.itemlist}")
    private String campaignItemListKeyPrefix;

    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Transactional(rollbackFor = Exception.class)
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
        syncItemToRedis(saved);

        return saved.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public Item updateItem(Long itemId, UpdateItemRequest req) {
        // find item from MySQL
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("no this item"));

        // 檢查總機率是否超過 100%
        int totalProbability = itemRepository.sumProbabilityByCampaignId(item.getCampaignId());
        if (totalProbability - item.getProbability() + req.getProbability() > 100000000) {
            throw new ProbabilityExceededException("probability exceeded");
        }

        // 寫入 MySQL
        item.setName(req.getName());
        item.setProbability(req.getProbability());

        Item saved = itemRepository.save(item);

        // 同步至 Redis
        syncItemToRedis(saved);

        return saved;
    }

    private void syncItemToRedis(Item item) {
        String campaignItemListKey = "{" + hashTagPrefix + item.getCampaignId() + "}_" + campaignItemListKeyPrefix + item.getCampaignId();
        String itemHashKey = item.getId().toString();
        List<String> keys = new ArrayList<>();
        keys.add(campaignItemListKey);
        keys.add(itemHashKey);

        // 準備腳本執行器
        DefaultRedisScript<Boolean> script = new DefaultRedisScript<>(LuaUtils.SYNC_ITEM_TO_REDIS_LUA, Boolean.class);

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

    @Transactional(rollbackFor = Exception.class)
    public Item updateItemStock(Long itemId, Long incrementAmount) {
        // find item from MySQL
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("no this item"));

        // 寫入 MySQL
        item.setTotalStock(item.getTotalStock() + incrementAmount);
        item.setCurrentStock(item.getCurrentStock() + incrementAmount);

        Item saved = itemRepository.save(item);

        // 同步至 Redis
        syncItemStockToRedis(saved, incrementAmount);

        return saved;
    }

    private void syncItemStockToRedis(Item item, Long incrementAmount) {
        String campaignItemListKey = "{" + hashTagPrefix + item.getCampaignId() + "}_" + campaignItemListKeyPrefix + item.getCampaignId();
        String itemHashKey = item.getId().toString();
        List<String> keys = new ArrayList<>();
        keys.add(campaignItemListKey);
        keys.add(itemHashKey);

        // 準備腳本執行器
        DefaultRedisScript<Long> script = new DefaultRedisScript<>(LuaUtils.SYNC_ITEM_STOCK_TO_REDIS_LUA, Long.class);

        log.info("totalStock:" + item.getTotalStock());
        log.info("incrementAmount:" + incrementAmount);

        // 執行腳本
        try {
            Long result = redisTemplate.execute(
                    script,
                    keys,
                    item.getTotalStock(),
                    incrementAmount
            );

            if (result < 0) {
                throw new StockNotEnoughException("item stock is not enough");
            }
        } catch (StockNotEnoughException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("insert item to Redis failed", e);
        }
    }

    @Transactional(readOnly = true)
    public ItemResponse getItemById(Long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("no this item"));
        return new ItemResponse(item);
    }
}
