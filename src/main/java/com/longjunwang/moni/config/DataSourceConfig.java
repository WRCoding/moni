package com.longjunwang.moni.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@Configuration
@Slf4j
public class DataSourceConfig {

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Bean
    public DataSource dataSource() {
        String dbPath = dbUrl.replace("jdbc:sqlite:", "");
        File dbFile = new File(dbPath);

        // 如果数据库文件不存在，创建它
        if (!dbFile.exists()) {
            try {
                log.info("dbPath不存在,将自动创建, dbPath: {}", dbPath);
                dbFile.getParentFile().mkdirs();
                dbFile.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException("无法创建数据库文件", e);
            }
        }
        initializeDatabase(dbPath);
        return DataSourceBuilder.create()
                .url(dbUrl)
                .driverClassName("org.sqlite.JDBC")
                .build();
    }

    private void initializeDatabase(String dbPath) {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
             Statement stmt = conn.createStatement()) {
            for (String statementSql : loadSqlStatements("sql/db.sql")) {
                stmt.executeUpdate(statementSql);
            }
            log.info("数据库初始化完成");
        } catch (IOException | SQLException e) {
            throw new RuntimeException("初始化数据库失败", e);
        }
    }

    private List<String> loadSqlStatements(String resourcePath) throws IOException {
        ClassPathResource resource = new ClassPathResource(resourcePath);
        if (resource.exists()) {
            try (InputStream inputStream = resource.getInputStream()) {
                return splitSqlStatements(new String(inputStream.readAllBytes(), StandardCharsets.UTF_8));
            }
        }

        Path path = Paths.get(resourcePath);
        if (!Files.exists(path)) {
            path = Paths.get(System.getProperty("user.dir"), resourcePath);
        }
        if (!Files.exists(path)) {
            throw new IOException("SQL 资源文件不存在: " + resourcePath);
        }

        return splitSqlStatements(Files.readString(path, StandardCharsets.UTF_8));
    }

    private List<String> splitSqlStatements(String sqlContent) {
        List<String> statements = new ArrayList<>();
        for (String statement : sqlContent.split(";")) {
            String trimmed = statement.trim();
            if (!trimmed.isEmpty()) {
                statements.add(trimmed);
            }
        }
        return statements;
    }
}
