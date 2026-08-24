package hu.kirdev.foodex.config

import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component

/**
 * Copies leftover newbie_grants rows into trial_grants (Hibernate ddl-auto does not rename tables)
 * and remaps grant-only NEWBIE users to TRIAL.
 */
@Component
class GrantSchemaMigrator(
    private val jdbcTemplate: JdbcTemplate,
) : ApplicationRunner {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun run(args: ApplicationArguments) {
        val newbieExists = tableExists("newbie_grants")
        val trialExists = tableExists("trial_grants")

        if (newbieExists && trialExists) {
            jdbcTemplate.update(
                """
                INSERT INTO trial_grants (name, internal_id)
                SELECT n.name, n.internal_id FROM newbie_grants n
                WHERE NOT EXISTS (
                    SELECT 1 FROM trial_grants t WHERE t.internal_id = n.internal_id
                )
                """.trimIndent()
            )
            jdbcTemplate.execute("DROP TABLE newbie_grants")
            log.info("Migrated newbie_grants into trial_grants")
        } else if (newbieExists && !trialExists) {
            jdbcTemplate.execute("ALTER TABLE newbie_grants RENAME TO trial_grants")
            log.info("Renamed newbie_grants to trial_grants")
        }

        if (tableExists("trial_grants")) {
            val updated = jdbcTemplate.update(
                """
                UPDATE users SET role = 'TRIAL'
                WHERE role = 'NEWBIE'
                AND internal_id IN (SELECT internal_id FROM trial_grants)
                """.trimIndent()
            )
            if (updated > 0) {
                log.info("Remapped {} NEWBIE user(s) with a trial grant to TRIAL", updated)
            }
        }
    }

    private fun tableExists(name: String): Boolean {
        val count = jdbcTemplate.queryForObject(
            """
            SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES
            WHERE UPPER(TABLE_NAME) = ?
            """.trimIndent(),
            Int::class.java,
            name.uppercase(),
        ) ?: 0
        return count > 0
    }
}
