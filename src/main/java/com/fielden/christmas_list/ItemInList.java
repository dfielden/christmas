package com.fielden.christmas_list;

public class ItemInList {
    private String product;
    private double price;
    private String location;
    private String url;
    private String additionalInfo;
    private boolean selected;
    private int selectedBy;



    public ItemInList(String product, double price, String location, String url, String additionalInfo, boolean selected, int selectedBy) {
        this.product = product;
        this.price = price;
        this.location = location;
        this.url = url;
        this.additionalInfo = additionalInfo;
        this.selected = selected;
        this.selectedBy = selectedBy;
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

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public int getSelectedBy() {
        return selectedBy;
    }

    public void setSelectedBy(int selectedBy) {
        this.selectedBy = selectedBy;
    }

    @Override
    public String toString() {
        return "ItemInList{" +
                "product='" + product + '\'' +
                ", price=" + price +
                ", location='" + location + '\'' +
                ", url='" + url + '\'' +
                ", additionalInfo='" + additionalInfo + '\'' +
                ", selected=" + selected +
                ", selectedBy=" + selectedBy +
                '}';
    }
}

class ListHeader {
    private String title;
    private long timeCreated;

    public ListHeader(String title, long timeCreated) {
        this.title = title;
        this.timeCreated = timeCreated;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getTimeCreated() {
        return timeCreated;
    }

    public void setTimeCreated(long timeCreated) {
        this.timeCreated = timeCreated;
    }

    @Override
    public String toString() {
        return "ListHeader{" +
                "title='" + title + '\'' +
                ", timeCreated=" + timeCreated +
                '}';
    }
}

