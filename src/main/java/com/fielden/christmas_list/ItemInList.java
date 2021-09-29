package com.fielden.christmas_list;

public class ItemInList {
    private String product;
    private double price;
    private String location;
    private String url;
    private String additionalInfo;


    public ItemInList(String product, double price, String location, String url, String additionalInfo) {
        this.product = product;
        this.price = price;
        this.location = location;
        this.url = url;
        this.additionalInfo = additionalInfo;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    @Override
    public String toString() {
        return "com.fielden.christmas_list.ItemInList{" +
                "product='" + product + '\'' +
                ", price=" + price +
                ", location='" + location + '\'' +
                ", url='" + url + '\'' +
                ", additionalInfo='" + additionalInfo + '\'' +
                '}';
    }
}
