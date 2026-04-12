package com.leander.lottery.lottery.repository;

import com.leander.lottery.lottery.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    @Query("SELECT COALESCE(SUM(i.probability), 0) FROM Item i WHERE i.campaignId = :campaignId")
    Integer sumProbabilityByCampaignId(@Param("campaignId") Long campaignId);
}