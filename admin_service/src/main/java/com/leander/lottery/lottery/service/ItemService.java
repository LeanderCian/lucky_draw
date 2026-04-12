package com.leander.lottery.lottery.service;

import com.leander.lottery.lottery.dto.ItemResponse;
import com.leander.lottery.lottery.dto.CreateItemRequest;
import com.leander.lottery.lottery.dto.UpdateItemRequest;
import com.leander.lottery.lottery.entity.Item;

public interface ItemService {
    public Long createItem(CreateItemRequest req);

    public Item updateItem(Long itemId, UpdateItemRequest req);

    public Item updateItemStock(Long itemId, Long incrementAmount);

    public ItemResponse getItemById(Long id);
}
