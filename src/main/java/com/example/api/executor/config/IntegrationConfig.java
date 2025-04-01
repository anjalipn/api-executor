package com.example.api.executor.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.http.outbound.HttpRequestExecutingMessageHandler;
import org.springframework.integration.jdbc.store.JdbcChannelMessageStore;
import org.springframework.integration.jdbc.store.channel.ChannelMessageStoreQueryProvider;
import org.springframework.integration.jdbc.store.channel.H2ChannelMessageStoreQueryProvider;
import org.springframework.integration.jdbc.store.channel.OracleChannelMessageStoreQueryProvider;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.client.RestTemplate;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.messaging.MessageChannel;
import javax.sql.DataSource;

@Configuration
@EnableIntegration
public class IntegrationConfig {

    @Value("${app.message-store.provider}")
    private String messageStoreProvider;

    @Bean
    public ChannelMessageStoreQueryProvider channelMessageStoreQueryProvider() {
        return switch (messageStoreProvider.toLowerCase()) {
            case "h2" -> new H2ChannelMessageStoreQueryProvider();
            case "oracle" -> new OracleChannelMessageStoreQueryProvider();
            default -> throw new IllegalArgumentException("Unsupported message store provider: " + messageStoreProvider);
        };
    }

    @Bean
    public HttpRequestExecutingMessageHandler httpOutboundGateway(RestTemplate restTemplate) {
        HttpRequestExecutingMessageHandler handler = new HttpRequestExecutingMessageHandler("http://placeholder");
        handler.setRestTemplate(restTemplate);
        handler.setExpectedResponseType(String.class);
        return handler;
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    // Task Execution Queue
    @Bean
    public QueueChannel taskExecutionQueue(JdbcChannelMessageStore taskExecutionMessageStore) {
        QueueChannel queueChannel = new QueueChannel();
        queueChannel.setBeanName("taskExecutionQueue");
        queueChannel.setMessageStore(taskExecutionMessageStore);
        return queueChannel;
    }

    @Bean
    public JdbcChannelMessageStore taskExecutionMessageStore(DataSource dataSource) {
        JdbcChannelMessageStore messageStore = new JdbcChannelMessageStore(dataSource);
        messageStore.setChannelMessageStoreQueryProvider(channelMessageStoreQueryProvider());
        messageStore.setTablePrefix("TASK_EXECUTION_");
        return messageStore;
    }

    // Due By Queue
    @Bean
    public QueueChannel dueByOutputChannel(JdbcChannelMessageStore dueByMessageStore) {
        QueueChannel queueChannel = new QueueChannel();
        queueChannel.setBeanName("taskExecutionQueue");
        queueChannel.setMessageStore(dueByMessageStore);
        return queueChannel;
    }
    @Bean
    public JdbcChannelMessageStore dueByMessageStore(DataSource dataSource) {
        JdbcChannelMessageStore messageStore =  new JdbcChannelMessageStore(dataSource);
        messageStore.setChannelMessageStoreQueryProvider(channelMessageStoreQueryProvider());
        messageStore.setTablePrefix("DUE_BY_");
        return messageStore;
    }

    @Bean
    public MessageChannel dueByInputChannel() {
        return new DirectChannel();
    }
} 