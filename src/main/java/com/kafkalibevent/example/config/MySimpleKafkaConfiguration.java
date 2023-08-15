package com.kafkalibevent.example.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.config.TopicBuilder;

@Profile("local")
@Configuration
public class MySimpleKafkaConfiguration {

    @Value("${spring.kafka.template.default-topic}")
    String topic;

    @Bean
    public NewTopic libraryEvent() {
        return TopicBuilder
                .name(topic)
                .partitions(3)
                .replicas(3)
                .build();
    }

}
