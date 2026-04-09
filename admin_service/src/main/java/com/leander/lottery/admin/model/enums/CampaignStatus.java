package com.leander.lottery.admin.model.enums;

public enum CampaignStatus {
    INACTIVE(0),
    ACTIVE(1);

    private final int value;

    CampaignStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    // 根據數字尋找對應的 Enum
    public static CampaignStatus fromValue(Integer value) {
        for (CampaignStatus status : CampaignStatus.values()) {
            if (status.value == value) {
                return status;
            }
        }
        return null; // 找不到則回傳 null
    }
}
