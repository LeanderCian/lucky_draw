package com.leander.lottery.admin.service;

import com.leander.lottery.admin.dto.ItemResponse;
import com.leander.lottery.admin.dto.CreateItemRequest;

public interface ItemService {
    public Long createItem(CreateItemRequest req);

    public ItemResponse getItemById(Long id);
}
