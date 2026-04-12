package com.leander.lottery.lottery.repository;

import com.leander.lottery.lottery.entity.LotteryCount;
import com.leander.lottery.lottery.entity.LotteryCountId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;

@Repository
public interface LotteryCountRepository extends JpaRepository<LotteryCount, LotteryCountId> {
    ArrayList<LotteryCount> findByCampaignId(Long campaignId);
}