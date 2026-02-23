package com.sourcegraph.demo.bigbadmonolith.integration;

import com.sourcegraph.demo.bigbadmonolith.TestDatabaseConfig;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

/**
 * Base integration test abstract class that sets up and tears down
 * the in-memory Derby database per test class/method.
 */
public abstract class BaseIntegrationTest {

    @BeforeAll
    static void initDatabase() {
        TestDatabaseConfig.initialize();
    }

    @BeforeEach
    public void cleanDatabase() {
        TestDatabaseConfig.cleanAllTables();
    }
}
