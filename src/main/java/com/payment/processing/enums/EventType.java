package com.payment.processing.enums;

public enum EventType {
    PAYMENT_CREATED("PaymentCreated"); 

    private final String eventType;
    
    EventType(String eventType){
        this.eventType = eventType;
    }

    public String getValue(){
        return this.eventType;
    }

}
