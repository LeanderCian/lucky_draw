package com.leander.lottery.lottery.service;

import com.leander.lottery.lottery.dto.*;

public interface LotteryService {
    public DrawResponse executeDraw(Long userId, DrawRequest req);
}
