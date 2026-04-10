package com.leander.lottery.admin.repository;

import com.leander.lottery.admin.entity.Item;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.PropertyValues;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    @Query("SELECT COALESCE(SUM(i.probability), 0) FROM Item i WHERE i.campaignId = :campaignId")
    Integer sumProbabilityByCampaignId(@Param("campaignId") Long campaignId);
}