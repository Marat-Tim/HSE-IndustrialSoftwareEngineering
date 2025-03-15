package ru.marattim.todolist.tests_infrastructure;


import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.sql.DataSource;

import org.springframework.lang.NonNull;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;
import org.springframework.test.context.transaction.TestContextTransactionUtils;

/**
 * Класс для очистки БД в промежутках между выполнением тестов
 */
public class PostgresTestExecutionListener extends AbstractTestExecutionListener {

    private List<String> tables;
    private List<String> sequences;

    @Override
    public int getOrder() {
        return 4999;
    }

    @Override
    public void beforeTestClass(@NonNull TestContext testContext) throws Exception {
        DataSource dataSource = TestContextTransactionUtils.retrieveDataSource(testContext, null);
        Connection connection = Objects.requireNonNull(dataSource).getConnection();

        try (connection; Statement statement = connection.createStatement()) {
            tables = truncateTables(statement);
            sequences = restartSequences(statement);
        }
    }

    @Override
    public void beforeTestMethod(@NonNull TestContext testContext) throws Exception {
        clearDatabase(testContext);
    }

    /**
     * Чистим БД
     */
    private void clearDatabase(TestContext testContext) throws Exception {
        DataSource dataSource = TestContextTransactionUtils.retrieveDataSource(testContext, null);

        Connection connection = Objects.requireNonNull(dataSource).getConnection();
        connection.setAutoCommit(false);

        try (connection; Statement statement = connection.createStatement()) {
            for (String table : tables) {
                statement.addBatch(table);
            }
            for (String sequence : sequences) {
                statement.addBatch(sequence);
            }
            statement.executeBatch();
            connection.commit();
        }
    }

    private List<String> truncateTables(Statement statement) throws SQLException {
        Set<String> tables = new HashSet<>();

        ResultSet rs = statement.executeQuery("""
            SELECT schemaname, tablename FROM pg_catalog.pg_tables
            WHERE schemaname = 'public'""");
        while (rs.next()) {
            tables.add(rs.getString(1) + "." + rs.getString(2));
        }
        rs.close();

        List<String> result = new ArrayList<>();
        for (String table : tables) {
            result.add("ALTER TABLE " + table + " DISABLE TRIGGER ALL");
        }
        for (String table : tables) {
            result.add("DELETE FROM " + table + " CASCADE");
        }
        for (String table : tables) {
            result.add("ALTER TABLE " + table + " ENABLE TRIGGER ALL");
        }
        return result;
    }

    private List<String> restartSequences(Statement statement) throws SQLException {
        Set<String> sequences = new HashSet<>();

        ResultSet rs = statement.executeQuery("""
            SELECT sequence_schema, sequence_name FROM information_schema.sequences
            WHERE sequence_schema = 'public'""");
        while (rs.next()) {
            sequences.add(rs.getString(1) + "." + rs.getString(2));
        }
        rs.close();

        return sequences.stream()
            .map(sequence -> "ALTER SEQUENCE " + sequence + " RESTART WITH 1")
            .toList();
    }

}
