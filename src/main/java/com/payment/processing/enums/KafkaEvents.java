package com.payment.processing.enums;

public enum KafkaEvents {
    PAYMENT_EVENTS("payment-events");

    private String kafkaEvent;

    KafkaEvents(String kafkaEvent){
        this.kafkaEvent = kafkaEvent;
    }

    public String getValue(){
        return this.kafkaEvent.toString();
    }
}
