package com.jpmc.midascore.component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.jpmc.midascore.foundation.Transaction;

@Component
public class KafkaConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaConsumer.class);

    @KafkaListener(topics = "${general.kafka-topic}")
    public void consume(Transaction transaction) {
        LOG.info("--> TRANSACTION RECEIVED - Amount: {}", transaction.getAmount());
    }
}