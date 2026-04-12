package com.leander.lottery.admin.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
@AllArgsConstructor
public class DrawResponse {
    private ArrayList<DrawResult> results;

    @Getter
    @Setter
    public static class DrawResult {
        @JsonProperty("draw_id")
        private String drawId;
        @JsonProperty("item_id")
        private Long itemId;
        @JsonProperty("item_name")
        private String itemName;
        @JsonProperty("is_win")
        private boolean isWin;
    }
}