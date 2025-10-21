package org.mbc.czo.function.common.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.mbc.czo.function.apiChatRoom.dto.chatImageDBUpdateEvent.ChatImageDBUpdateEvent;
import org.mbc.czo.function.apiChatRoom.dto.chatRoomParticipantDBUpdateEvent.ChatRoomParticipantDBUpdateEvent;
import org.mbc.czo.function.apiChatRoom.dto.chatMessageDBUpdateEvent.ChatMessageDBUpdateEvent;
import org.mbc.czo.function.apiStockManagement.dto.stockDBUpdateEvent.StockDBUpdateEvent;
import org.mbc.czo.function.apiStockManagement.dto.stockReissedEvent.StockReissueEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    // ===================== Producer Config =====================
    private <T> ProducerFactory<String, T> producerFactory(Class<T> clazz) {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        return new DefaultKafkaProducerFactory<>(props);
    }

    // ===================== Consumer Config =====================
    private <T> ConsumerFactory<String, T> consumerFactory(Class<T> clazz, String groupId) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        JsonDeserializer<T> deserializer = new JsonDeserializer<>(clazz, false);
        deserializer.addTrustedPackages("*");  // 모든 패키지 허용
        deserializer.setUseTypeMapperForKey(false);

        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), deserializer);
    }

    private <T> ConcurrentKafkaListenerContainerFactory<String, T> listenerFactory(Class<T> clazz, String groupId, int concurrency) {
        ConcurrentKafkaListenerContainerFactory<String, T> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory(clazz, groupId));
        factory.setConcurrency(concurrency);
        return factory;
    }

    // ===================== StockDBUpdateEvent =====================
    @Bean
    public KafkaTemplate<String, StockDBUpdateEvent> stockDBUpdateKafkaTemplate() {
        return new KafkaTemplate<>(producerFactory(StockDBUpdateEvent.class));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, StockDBUpdateEvent> stockDBUpdateListenerFactory() {
        return listenerFactory(StockDBUpdateEvent.class, "stockDBUpdate", 10);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, StockDBUpdateEvent> stockDBUpdateDLQListenerFactory() {
        return listenerFactory(StockDBUpdateEvent.class, "stockDBUpdate-dlq", 1);
    }

    // ===================== StockReissueEvent =====================
    @Bean
    public KafkaTemplate<String, StockReissueEvent> stockReissueKafkaTemplate() {
        return new KafkaTemplate<>(producerFactory(StockReissueEvent.class));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, StockReissueEvent> stockReissueListenerFactory() {
        return listenerFactory(StockReissueEvent.class, "stockReissue", 1);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, StockReissueEvent> stockReissueDLQListenerFactory() {
        return listenerFactory(StockReissueEvent.class, "stockReissue-dlq", 1);
    }

    // ===================== ChatMessageDBUpdateEvent =====================
    @Bean
    public KafkaTemplate<String, ChatMessageDBUpdateEvent> chatMessageDBUpdateKafkaTemplate() {
        return new KafkaTemplate<>(producerFactory(ChatMessageDBUpdateEvent.class));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ChatMessageDBUpdateEvent> chatMessageDBUpdateListenerFactory() {
        return listenerFactory(ChatMessageDBUpdateEvent.class, "chatMessageDBUpdate", 3);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ChatMessageDBUpdateEvent> chatMessageDBUpdateDLQListenerFactory() {
        return listenerFactory(ChatMessageDBUpdateEvent.class, "chatMessageDBUpdate-dlq", 1);
    }

    // ===================== ChatRoomParticipantDBUpdateEvent =====================
    @Bean
    public KafkaTemplate<String, ChatRoomParticipantDBUpdateEvent> chatRoomParticipantDBUpdateKafkaTemplate() {
        return new KafkaTemplate<>(producerFactory(ChatRoomParticipantDBUpdateEvent.class));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ChatRoomParticipantDBUpdateEvent>chatRoomParticipantDBUpdateListenerFactory() {
        return listenerFactory(ChatRoomParticipantDBUpdateEvent.class, "chatRoomParticipantDBUpdate", 3);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ChatRoomParticipantDBUpdateEvent> chatRoomParticipantDBUpdateDLQListenerFactory() {
        return listenerFactory(ChatRoomParticipantDBUpdateEvent.class, "chatRoomParticipantDBUpdate-dlq", 1);
    }

    // ===================== ChatImageDBUpdateEvent =====================
    @Bean
    public KafkaTemplate<String, ChatImageDBUpdateEvent> chatImageDBUpdateKafkaTemplate() {
        return new KafkaTemplate<>(producerFactory(ChatImageDBUpdateEvent.class));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ChatImageDBUpdateEvent>chatImageDBUpdateListenerFactory() {
        return listenerFactory(ChatImageDBUpdateEvent.class, "chatImageDBUpdate", 3);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ChatImageDBUpdateEvent> chatImageDBUpdateDLQListenerFactory() {
        return listenerFactory(ChatImageDBUpdateEvent.class, "chatImageDBUpdate-dlq", 1);
    }
}
