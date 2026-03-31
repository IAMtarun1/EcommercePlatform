package com.ecommerce.ecommerceplatform.dto;

import lombok.Data;

@Data
public class PaymentResponse {
    private boolean success;
    private String message;
    private String paymentIntentId;
    private String clientSecret;
    private String paymentStatus;
    private String orderNumber;
    private String receiptUrl;
    
    public static PaymentResponse success(String message, String paymentIntentId, 
                                          String clientSecret, String orderNumber) {
        PaymentResponse response = new PaymentResponse();
        response.setSuccess(true);
        response.setMessage(message);
        response.setPaymentIntentId(paymentIntentId);
        response.setClientSecret(clientSecret);
        response.setOrderNumber(orderNumber);
        response.setPaymentStatus("SUCCEEDED");
        return response;
    }
    
    public static PaymentResponse error(String message) {
        PaymentResponse response = new PaymentResponse();
        response.setSuccess(false);
        response.setMessage(message);
        response.setPaymentStatus("FAILED");
        return response;
    }
}
