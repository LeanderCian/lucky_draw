package com.leander.lottery.admin.service;

import com.leander.lottery.admin.dto.ItemResponse;
import com.leander.lottery.admin.dto.CreateItemRequest;
import com.leander.lottery.admin.dto.UpdateItemRequest;
import com.leander.lottery.admin.entity.Item;

public interface ItemService {
    public Long createItem(CreateItemRequest req);

    public Item updateItem(Long itemId, UpdateItemRequest req);

    public Item updateItemStock(Long itemId, Long incrementAmount);

    public ItemResponse getItemById(Long id);
}
