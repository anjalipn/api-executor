package com.example.api.executor.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.jdbc.store.JdbcChannelMessageStore;
import org.springframework.integration.jdbc.store.channel.ChannelMessageStoreQueryProvider;
import org.springframework.integration.jdbc.store.channel.H2ChannelMessageStoreQueryProvider;
import org.springframework.integration.jdbc.store.channel.OracleChannelMessageStoreQueryProvider;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
@EnableIntegration
public class IntegrationConfig {

    @Value("${app.message-store.provider}")
    private String messageStoreProvider;

    @Bean
    public QueueChannel taskExecutionQueue() {
        return new QueueChannel();
    }

    @Bean
    public JdbcChannelMessageStore taskExecutionMessageStore(DataSource dataSource) {
        JdbcChannelMessageStore messageStore = new JdbcChannelMessageStore(dataSource);
        messageStore.setChannelMessageStoreQueryProvider(channelMessageStoreQueryProvider());
        messageStore.setTablePrefix("TASK_EXECUTION_");
        return messageStore;
    }

    @Bean
    public ChannelMessageStoreQueryProvider channelMessageStoreQueryProvider() {
        return switch (messageStoreProvider.toLowerCase()) {
            case "h2" -> new H2ChannelMessageStoreQueryProvider();
            case "oracle" -> new OracleChannelMessageStoreQueryProvider();
            default -> throw new IllegalArgumentException("Unsupported message store provider: " + messageStoreProvider);
        };
    }
} 