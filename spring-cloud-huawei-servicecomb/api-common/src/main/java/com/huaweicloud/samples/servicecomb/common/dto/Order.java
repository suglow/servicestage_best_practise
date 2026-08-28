package com.huaweicloud.samples.servicecomb.common.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class Order {
    private String orderId;
    private Long productId;
    private String productName;
    private Double amount;
    private String status;
    private String servicePort;
    private String serviceVersion;
    private LocalDateTime timestamp;

    public Order() {
        this.orderId = UUID.randomUUID().toString().substring(0, 8);
        this.status = "CREATED";
        this.timestamp = LocalDateTime.now();
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getServicePort() {
        return servicePort;
    }

    public void setServicePort(String servicePort) {
        this.servicePort = servicePort;
    }

    public String getServiceVersion() {
        return serviceVersion;
    }

    public void setServiceVersion(String serviceVersion) {
        this.serviceVersion = serviceVersion;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "Order{" +
                "orderId='" + orderId + '\'' +
                ", productId=" + productId +
                ", productName='" + productName + '\'' +
                ", amount=" + amount +
                ", status='" + status + '\'' +
                ", servicePort='" + servicePort + '\'' +
                ", serviceVersion='" + serviceVersion + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
