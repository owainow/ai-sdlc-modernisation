package com.bigbadmonolith.migration;

/**
 * Holds the results of a migration run for validation.
 */
public class MigrationResult {
    private int usersMigrated;
    private int customersMigrated;
    private int categoriesMigrated;
    private int hoursMigrated;

    public int getUsersMigrated() { return usersMigrated; }
    public void setUsersMigrated(int usersMigrated) { this.usersMigrated = usersMigrated; }

    public int getCustomersMigrated() { return customersMigrated; }
    public void setCustomersMigrated(int customersMigrated) { this.customersMigrated = customersMigrated; }

    public int getCategoriesMigrated() { return categoriesMigrated; }
    public void setCategoriesMigrated(int categoriesMigrated) { this.categoriesMigrated = categoriesMigrated; }

    public int getHoursMigrated() { return hoursMigrated; }
    public void setHoursMigrated(int hoursMigrated) { this.hoursMigrated = hoursMigrated; }

    public int totalRecords() {
        return usersMigrated + customersMigrated + categoriesMigrated + hoursMigrated;
    }

    @Override
    public String toString() {
        return String.format("MigrationResult{users=%d, customers=%d, categories=%d, hours=%d, total=%d}",
                usersMigrated, customersMigrated, categoriesMigrated, hoursMigrated, totalRecords());
    }
}
