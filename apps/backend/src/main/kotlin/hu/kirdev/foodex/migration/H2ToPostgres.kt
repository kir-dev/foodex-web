package hu.kirdev.foodex.migration

import java.sql.Clob
import java.sql.Connection
import java.sql.DriverManager

/**
 * One-off H2 -> Postgres data copy for FoodEx.
 *
 * The target schema must already exist (start the backend once against the
 * empty Postgres database, then stop it before running this). The copy refuses
 * to touch a target that already has rows in any of the tables below.
 *
 * Run with:
 *   H2_URL=jdbc:h2:file:/path/to/db \
 *   H2_USER=sa H2_PASSWORD=password \
 *   POSTGRES_URL=jdbc:postgresql://localhost:5432/foodex \
 *   POSTGRES_USER=foodex POSTGRES_PASSWORD=... \
 *   ./gradlew migrateH2ToPostgres
 *
 * POSTGRES_USER/POSTGRES_PASSWORD may be omitted when the JDBC URL already
 * contains `user` and `password` (as CNPG's `jdbc-uri` does).
 */
private val TABLES = listOf(
    // Parents first, then rows that reference them, then join tables.
    "users",
    "cooking_clubs",
    "configuration",
    "admin_grants",
    "trial_grants",
    "requests",
    "shifts",
    "cooking_club_leaders",
    "shift_workers",
)

private val IDENTITY_TABLES = listOf(
    "users",
    "shifts",
    "requests",
    "admin_grants",
    "trial_grants",
)

fun main() {
    val h2Url = env("H2_URL") ?: "jdbc:h2:file:./db"
    val h2User = env("H2_USER") ?: "sa"
    val h2Password = env("H2_PASSWORD") ?: "password"
    val postgresUrl = requiredEnv("POSTGRES_URL")
    val postgresUser = env("POSTGRES_USER")
    val postgresPassword = env("POSTGRES_PASSWORD")

    DriverManager.getConnection(h2Url, h2User, h2Password).use { h2 ->
        val postgres = if (postgresUser != null) {
            DriverManager.getConnection(postgresUrl, postgresUser, postgresPassword ?: "")
        } else {
            DriverManager.getConnection(postgresUrl)
        }
        postgres.use { pg ->
            h2.autoCommit = false
            pg.autoCommit = false
            try {
                val nonEmpty = TABLES.filter { count(pg, it) > 0 }
                check(nonEmpty.isEmpty()) {
                    "Postgres already has rows in: ${nonEmpty.joinToString()}. " +
                        "Clear those tables first so this copy cannot duplicate data."
                }

                for (table in TABLES) {
                    val copied = copyTable(h2, pg, table)
                    println("$table: copied $copied row(s)")
                    check(count(pg, table) == copied.toLong()) { "$table: Postgres row count does not match the copy" }
                }

                for (table in IDENTITY_TABLES) {
                    resetSequence(pg, table)
                }

                pg.commit()
                println("Committed. Row counts: " + TABLES.joinToString { "$it=${count(pg, it)}" })
            } catch (error: Throwable) {
                pg.rollback()
                throw error
            }
        }
    }
}

private fun copyTable(source: Connection, target: Connection, table: String): Int {
    source.createStatement().use { statement ->
        statement.executeQuery("SELECT * FROM $table").use { rows ->
            val meta = rows.metaData
            val columns = (1..meta.columnCount).map { meta.getColumnName(it) }
            val insert = "INSERT INTO $table (${columns.joinToString(", ")}) VALUES (${columns.joinToString(", ") { "?" }})"

            target.prepareStatement(insert).use { prepared ->
                var copied = 0
                while (rows.next()) {
                    for (index in columns.indices) {
                        val parameter = index + 1
                        when (val value = rows.getObject(parameter)) {
                            null -> prepared.setNull(parameter, meta.getColumnType(parameter))
                            is Clob -> prepared.setString(parameter, value.characterStream.readText())
                            else -> prepared.setObject(parameter, value)
                        }
                    }
                    prepared.addBatch()
                    copied++
                    if (copied % 500 == 0) prepared.executeBatch()
                }
                if (copied % 500 != 0) prepared.executeBatch()
                return copied
            }
        }
    }
}

private fun resetSequence(connection: Connection, table: String) {
    connection.createStatement().use { statement ->
        statement.execute(
            """
            SELECT setval(
                pg_get_serial_sequence('$table', 'id'),
                COALESCE((SELECT MAX(id) FROM $table), 1),
                (SELECT COUNT(*) > 0 FROM $table)
            )
            """.trimIndent()
        )
    }
    println("$table: identity sequence advanced past the copied ids")
}

private fun count(connection: Connection, table: String): Long =
    connection.createStatement().use { statement ->
        statement.executeQuery("SELECT COUNT(*) FROM $table").use { rows ->
            rows.next()
            rows.getLong(1)
        }
    }

private fun env(name: String): String? = System.getenv(name)?.takeIf { it.isNotBlank() }

private fun requiredEnv(name: String): String = env(name) ?: error("Set $name")
