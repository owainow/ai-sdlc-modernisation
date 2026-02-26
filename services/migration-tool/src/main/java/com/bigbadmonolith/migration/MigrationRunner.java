package com.bigbadmonolith.migration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * CLI entry point for the Derby → PostgreSQL migration tool.
 * Run with: java -jar migration-tool.jar
 * 
 * Connects to source Derby and all 4 target PostgreSQL schemas,
 * runs migration, then validates parity.
 */
@Component
public class MigrationRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(MigrationRunner.class);

    private final MigrationService migrationService;
    private final MigrationValidator migrationValidator;

    @Value("${migration.source.url}")
    private String derbyUrl;
    @Value("${migration.source.username}")
    private String derbyUser;
    @Value("${migration.source.password}")
    private String derbyPassword;

    @Value("${migration.target.user-service.url}")
    private String pgUserUrl;
    @Value("${migration.target.user-service.username}")
    private String pgUserUser;
    @Value("${migration.target.user-service.password}")
    private String pgUserPassword;

    @Value("${migration.target.customer-service.url}")
    private String pgCustomerUrl;
    @Value("${migration.target.customer-service.username}")
    private String pgCustomerUser;
    @Value("${migration.target.customer-service.password}")
    private String pgCustomerPassword;

    @Value("${migration.target.billing-service.url}")
    private String pgBillingUrl;
    @Value("${migration.target.billing-service.username}")
    private String pgBillingUser;
    @Value("${migration.target.billing-service.password}")
    private String pgBillingPassword;

    @Value("${migration.target.reporting-service.url}")
    private String pgReportingUrl;
    @Value("${migration.target.reporting-service.username}")
    private String pgReportingUser;
    @Value("${migration.target.reporting-service.password}")
    private String pgReportingPassword;

    public MigrationRunner(MigrationService migrationService, MigrationValidator migrationValidator) {
        this.migrationService = migrationService;
        this.migrationValidator = migrationValidator;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("=== Derby → PostgreSQL Migration Tool ===");
        log.info("Source: {}", derbyUrl);
        log.info("Target User Service: {}", pgUserUrl);
        log.info("Target Customer Service: {}", pgCustomerUrl);
        log.info("Target Billing Service: {}", pgBillingUrl);
        log.info("Target Reporting Service: {}", pgReportingUrl);

        try (Connection derbyConn = DriverManager.getConnection(derbyUrl, derbyUser, derbyPassword);
             Connection pgUserConn = DriverManager.getConnection(pgUserUrl, pgUserUser, pgUserPassword);
             Connection pgCustomerConn = DriverManager.getConnection(pgCustomerUrl, pgCustomerUser, pgCustomerPassword);
             Connection pgBillingConn = DriverManager.getConnection(pgBillingUrl, pgBillingUser, pgBillingPassword);
             Connection pgReportingConn = DriverManager.getConnection(pgReportingUrl, pgReportingUser, pgReportingPassword)) {

            // Step 1: Migrate data
            MigrationResult result = migrationService.migrate(derbyConn, pgUserConn,
                    pgCustomerConn, pgBillingConn, pgReportingConn);
            log.info("Migration complete: {}", result);

            // Step 2: Validate parity
            log.info("Running post-migration validation...");
            ValidationResult validation = migrationValidator.validate(derbyConn, pgUserConn,
                    pgCustomerConn, pgBillingConn);

            if (validation.isValid()) {
                log.info("✅ Migration PASSED all validation checks");
                log.info("  Row counts match: {}", validation.isRowCountMatch());
                log.info("  Revenue parity: {} (source={}, target={})",
                        validation.isRevenueMatch(), validation.getSourceRevenue(), validation.getTargetRevenue());
                log.info("  FK integrity: {} (orphaned hours={})",
                        validation.isFkIntegrity(), validation.getOrphanedHours());
            } else {
                log.error("❌ Migration FAILED validation");
                if (!validation.isRowCountMatch()) {
                    log.error("  Row count mismatch: users({}/{}), customers({}/{}), categories({}/{}), hours({}/{})",
                            validation.getSourceUserCount(), validation.getTargetUserCount(),
                            validation.getSourceCustomerCount(), validation.getTargetCustomerCount(),
                            validation.getSourceCategoryCount(), validation.getTargetCategoryCount(),
                            validation.getSourceHourCount(), validation.getTargetHourCount());
                }
                if (!validation.isRevenueMatch()) {
                    log.error("  Revenue mismatch: source={}, target={}",
                            validation.getSourceRevenue(), validation.getTargetRevenue());
                }
                if (!validation.isFkIntegrity()) {
                    log.error("  FK integrity: {} orphaned billable hours", validation.getOrphanedHours());
                }
                throw new RuntimeException("Migration validation failed: " + validation);
            }
        }
    }
}
