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
                row."${resultSet.getMetaData().getColumnName(columnIndex).toLowerCase()}" = resultSet.getObject(columnIndex)
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
select 
    c.*
from
    information_schema.columns c 
where
    lower(c.table_name) = lower('${table}')
order by
    c.ordinal_position 
""")
    }

    def allForeignKeys(String table) {
        def fks = select("""
select distinct 
    tc.table_schema,
    tc.constraint_name,
    tc.table_name,
    ccu.table_name as foreign_table_name
from
    information_schema.table_constraints as tc
join information_schema.constraint_column_usage as ccu on
    ccu.constraint_name = tc.constraint_name
    and ccu.table_schema = tc.table_schema
where
    tc.constraint_type = 'FOREIGN KEY'
    and lower(tc.table_name) = lower('${table}')
order by tc.constraint_name ;
""")
        fks.each { fk ->
            fk.columns = allForeignKeyColumns(fk.table_name, fk.constraint_name)
        }
    }

    def allForeignKeyColumns(String table, String foreignKey) {
        select("""
select
    tc.table_schema,
    tc.constraint_name,
    tc.table_name,
    kcu.column_name,
    ccu.table_schema as foreign_table_schema,
    ccu.table_name as foreign_table_name,
    ccu.column_name as foreign_column_name
from
    information_schema.table_constraints as tc
join information_schema.key_column_usage as kcu on
    tc.constraint_name = kcu.constraint_name
    and tc.table_schema = kcu.table_schema
join information_schema.constraint_column_usage as ccu on
    ccu.constraint_name = tc.constraint_name
    and ccu.table_schema = tc.table_schema
where
    tc.constraint_type = 'FOREIGN KEY'
    and lower(tc.table_name) = lower('${table}')
    and lower(tc.constraint_name) = lower('${foreignKey}')
order by ccu.column_name
""")
    }

    def primaryKey(String table) {
        def pk = select("""
select
    tc.table_schema,
    tc.constraint_name,
    tc.table_name
from
    information_schema.table_constraints as tc
where
    tc.constraint_type = 'PRIMARY KEY'
    and lower(tc.table_name) = lower ('${table}')
order by 
    tc.constraint_name
""")
        pk.each { key ->
            key.columns = allPrimaryKeyColumns(key.table_name, key.constraint_name)
        }
        pk
    }

    def allPrimaryKeyColumns(table, constraint) {
        select("""
select
    kcu.column_name,
    kcu.ordinal_position
from
    information_schema.table_constraints as tc
join information_schema.key_column_usage as kcu on
    tc.constraint_name = kcu.constraint_name
    and tc.table_schema = kcu.table_schema
where
    tc.constraint_type = 'PRIMARY KEY'
    and lower(tc.table_name) = lower ('${table}')
    and lower(tc.constraint_name) = lower ('${constraint}')
order by
    kcu.ordinal_position 
""")
    }

    def execute(String script) {
        def connection = getConnection()
        connection.createStatement().execute(script)
        connection.close()
    }
}
