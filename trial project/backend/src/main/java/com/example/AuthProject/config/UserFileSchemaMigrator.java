package com.example.AuthProject.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Older mapping used {@code @Lob}, which created a PostgreSQL OID large-object column.
 * Convert to TEXT so reads work without Large Object / auto-commit issues.
 */
@Slf4j
@Component
public class UserFileSchemaMigrator implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    public UserFileSchemaMigrator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            String dataType = jdbcTemplate.query(
                    """
                            SELECT data_type
                            FROM information_schema.columns
                            WHERE table_schema = 'public'
                              AND table_name = 'user_files'
                              AND column_name = 'extracted_text'
                            """,
                    rs -> rs.next() ? rs.getString(1) : null
            );
            if (dataType == null) {
                return;
            }
            if ("oid".equalsIgnoreCase(dataType)) {
                log.warn("Migrating user_files.extracted_text from oid to TEXT (existing extracted text cleared)");
                jdbcTemplate.execute("ALTER TABLE user_files DROP COLUMN extracted_text");
                jdbcTemplate.execute("ALTER TABLE user_files ADD COLUMN extracted_text TEXT");
                log.info("user_files.extracted_text is now TEXT");
            }
        } catch (Exception e) {
            log.warn("Skipped user_files.extracted_text migration: {}", e.getMessage());
        }
    }
}
