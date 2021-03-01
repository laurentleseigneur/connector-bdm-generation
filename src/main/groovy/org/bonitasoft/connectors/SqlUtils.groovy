package org.bonitasoft.connectors

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource

import java.sql.Connection
import java.sql.ResultSet

import static java.sql.ResultSet.CONCUR_READ_ONLY
import static java.sql.ResultSet.TYPE_SCROLL_INSENSITIVE

class SqlUtils {

    private HikariDataSource hikariDataSource

    void connect(String jdbcUrl, String user_name, String password, String driverClassName) {
        HikariConfig hikariConfig = new HikariConfig()
        hikariConfig.setDriverClassName(driverClassName)
        hikariConfig.setJdbcUrl(jdbcUrl)
        hikariConfig.setUsername(user_name)
        hikariConfig.setPassword(password)
        hikariDataSource = new HikariDataSource(hikariConfig)
    }

    void disconnect() {
        if (hikariDataSource) {
            hikariDataSource.close()
        }
    }

    def select(String script) {
        def connection = getConnection()
        def selectStatement = connection.createStatement(TYPE_SCROLL_INSENSITIVE,
                CONCUR_READ_ONLY)
        ResultSet resultSet = selectStatement.executeQuery(script)
        def rows = []
        int columnCount = resultSet.getMetaData().getColumnCount()
        while (resultSet.next()) {
            def row = [:]
            for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
                row."${resultSet.getMetaData().getColumnName(columnIndex)}" = resultSet.getObject(columnIndex)
            }
            rows.add(row)
        }
        resultSet.close()
        connection.close()
        rows
    }

    private Connection getConnection() {
        hikariDataSource.getConnection()
    }

    def allTables() {
        select("""
select   t.*
from     information_schema.tables t
where    t.table_schema = 'public'
and      t.table_type  = 'BASE TABLE'
order by t.table_name 
""")
    }


    def allColumns(String table) {
        select("""
select c.*
from information_schema.columns c 
where lower(c.table_name) = lower('${table}')
order by c.column_name 
""")
    }

    def allForeignKeys(String table) {
        select("""
SELECT
    tc.table_schema, 
    tc.constraint_name, 
    tc.table_name, 
    kcu.column_name, 
    ccu.table_schema AS foreign_table_schema,
    ccu.table_name AS foreign_table_name,
    ccu.column_name AS foreign_column_name 
FROM 
    information_schema.table_constraints AS tc 
    JOIN information_schema.key_column_usage AS kcu
      ON tc.constraint_name = kcu.constraint_name
      AND tc.table_schema = kcu.table_schema
    JOIN information_schema.constraint_column_usage AS ccu
      ON ccu.constraint_name = tc.constraint_name
      AND ccu.table_schema = tc.table_schema
WHERE tc.constraint_type = 'FOREIGN KEY' AND lower(tc.table_name) = lower('${table}')
""")
    }

    def execute(String script) {
        def connection = getConnection()
        connection.createStatement().execute(script)
        connection.close()
    }

}
