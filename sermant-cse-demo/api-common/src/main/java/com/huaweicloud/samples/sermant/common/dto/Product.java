package com.huaweicloud.samples.sermant.common.dto;

public class Product {
    private Long id;
    private String name;
    private Double price;
    private String source;

    public Product() {
    }

    public Product(Long id, String name, Double price, String source) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.source = source;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
}
