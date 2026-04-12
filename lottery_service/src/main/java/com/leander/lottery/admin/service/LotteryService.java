package com.leander.lottery.admin.service;

import com.leander.lottery.admin.dto.*;

public interface LotteryService {
    public DrawResponse executeDraw(Long userId, DrawRequest req);
}
