package io.github.nambintsou32.medicalvisits.persistence;

import java.util.Objects;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.output.MigrateResult;

public final class DatabaseMigrator {

    private static final String MIGRATION_LOCATION =
        "classpath:db/migration";

    private DatabaseMigrator() {
    }

    public static MigrateResult migrate(
            String jdbcUrl,
            String username,
            String password
    ) {
        Objects.requireNonNull(jdbcUrl, "jdbcUrl");
        Objects.requireNonNull(username, "username");
        Objects.requireNonNull(password, "password");

        Flyway flyway = Flyway.configure()
            .dataSource(jdbcUrl, username, password)
            .locations(MIGRATION_LOCATION)
            .validateMigrationNaming(true)
            .validateOnMigrate(true)
            .cleanDisabled(true)
            .load();

        return flyway.migrate();
    }
}
