package com.leander.lottery.admin.repository;

import com.leander.lottery.admin.entity.LotteryCount;
import com.leander.lottery.admin.entity.LotteryCountId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;

@Repository
public interface LotteryCountRepository extends JpaRepository<LotteryCount, LotteryCountId> {
    ArrayList<LotteryCount> findByCampaignId(Long campaignId);
}