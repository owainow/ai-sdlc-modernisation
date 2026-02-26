package com.bigbadmonolith.migration;

import java.math.BigDecimal;

/**
 * Holds validation results for post-migration parity checks.
 */
public class ValidationResult {
    private long sourceUserCount;
    private long targetUserCount;
    private long sourceCustomerCount;
    private long targetCustomerCount;
    private long sourceCategoryCount;
    private long targetCategoryCount;
    private long sourceHourCount;
    private long targetHourCount;
    private BigDecimal sourceRevenue = BigDecimal.ZERO;
    private BigDecimal targetRevenue = BigDecimal.ZERO;
    private long orphanedHours;

    public boolean isRowCountMatch() {
        return sourceUserCount == targetUserCount
                && sourceCustomerCount == targetCustomerCount
                && sourceCategoryCount == targetCategoryCount
                && sourceHourCount == targetHourCount;
    }

    public boolean isRevenueMatch() {
        return sourceRevenue.compareTo(targetRevenue) == 0;
    }

    public boolean isFkIntegrity() {
        return orphanedHours == 0;
    }

    public boolean isValid() {
        return isRowCountMatch() && isRevenueMatch() && isFkIntegrity();
    }

    // Getters and setters
    public long getSourceUserCount() { return sourceUserCount; }
    public void setSourceUserCount(long v) { this.sourceUserCount = v; }
    public long getTargetUserCount() { return targetUserCount; }
    public void setTargetUserCount(long v) { this.targetUserCount = v; }
    public long getSourceCustomerCount() { return sourceCustomerCount; }
    public void setSourceCustomerCount(long v) { this.sourceCustomerCount = v; }
    public long getTargetCustomerCount() { return targetCustomerCount; }
    public void setTargetCustomerCount(long v) { this.targetCustomerCount = v; }
    public long getSourceCategoryCount() { return sourceCategoryCount; }
    public void setSourceCategoryCount(long v) { this.sourceCategoryCount = v; }
    public long getTargetCategoryCount() { return targetCategoryCount; }
    public void setTargetCategoryCount(long v) { this.targetCategoryCount = v; }
    public long getSourceHourCount() { return sourceHourCount; }
    public void setSourceHourCount(long v) { this.sourceHourCount = v; }
    public long getTargetHourCount() { return targetHourCount; }
    public void setTargetHourCount(long v) { this.targetHourCount = v; }
    public BigDecimal getSourceRevenue() { return sourceRevenue; }
    public void setSourceRevenue(BigDecimal v) { this.sourceRevenue = v; }
    public BigDecimal getTargetRevenue() { return targetRevenue; }
    public void setTargetRevenue(BigDecimal v) { this.targetRevenue = v; }
    public long getOrphanedHours() { return orphanedHours; }
    public void setOrphanedHours(long v) { this.orphanedHours = v; }

    @Override
    public String toString() {
        return String.format("ValidationResult{rowMatch=%s, revenueMatch=%s (src=%s, tgt=%s), fkIntegrity=%s, orphaned=%d}",
                isRowCountMatch(), isRevenueMatch(), sourceRevenue, targetRevenue, isFkIntegrity(), orphanedHours);
    }
}
