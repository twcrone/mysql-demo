package com.newrelic.mysqldemo;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.*;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class JdbcContentService {

    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/spring_app_db?useSSL=false&user=root&password=root";
    private static final String SQL = "insert into content(id, content) values(?, ?)";
    private final AtomicInteger idGenerator = new AtomicInteger(100);
    private final AtomicInteger preparedIdGenerator = new AtomicInteger(1000);

    private final JdbcTemplate jdbcTemplate;

    public JdbcContentService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int createContentViaPreparedStatement(String content) throws IOException {
        int id = preparedIdGenerator.incrementAndGet();
        DataSource dataSource = Objects.requireNonNull(jdbcTemplate.getDataSource());
        try(PreparedStatement statement = dataSource.getConnection().prepareStatement(SQL)) {
            statement.setInt(1, id);
            statement.setString(2, content);
            statement.execute();
        } catch (SQLException ex) {
            // handle any errors
            printSQLException(ex);
            return -1;
        }
        return id;
    }

    public int createContentViaStatementAndManualSqlConcatenation(String content) throws IOException {
        int id = idGenerator.incrementAndGet();
        DataSource dataSource = Objects.requireNonNull(jdbcTemplate.getDataSource());
        try(Statement statement = dataSource.getConnection().createStatement()) {
            String replacement = "\\'";
            System.out.println("REPLACEMENT ***" + replacement + "***");
            content = content.replace("'", replacement);
            System.out.println("NEWCONTENT=[" + content + "]");
            statement.execute("insert into content(id, content) values (" + id + ", '" + content + "')");
        } catch (SQLException ex) {
            // handle any errors
            printSQLException(ex);
            return -1;
        }
        return id;
    }

    private static void printSQLException(SQLException ex) {
        System.err.println("SQLException: " + ex.getMessage());
        System.err.println("SQLState: " + ex.getSQLState());
        System.err.println("VendorError: " + ex.getErrorCode());
    }

}
