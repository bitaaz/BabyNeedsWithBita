package com.example.babyneedswithbita.model;


import com.google.firebase.Timestamp;

public class BabyNeedItem {

    private String itemName;
    private int itemQuantity;
    private int itemSize;
    private String itemColor;
    private String imageUrl;
    private String userId;
    private Timestamp dateCreated;

    public BabyNeedItem() {
    }

    public BabyNeedItem(String itemName, int itemQuantity, int itemSize, String itemColor, String imageUrl, String userId, Timestamp dateCreated) {
        this.itemName = itemName;
        this.itemQuantity = itemQuantity;
        this.itemSize = itemSize;
        this.itemColor = itemColor;
        this.imageUrl = imageUrl;
        this.userId = userId;
        this.dateCreated = dateCreated;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public int getItemQuantity() {
        return itemQuantity;
    }

    public void setItemQuantity(int itemQuantity) {
        this.itemQuantity = itemQuantity;
    }

    public int getItemSize() {
        return itemSize;
    }

    public void setItemSize(int itemSize) {
        this.itemSize = itemSize;
    }

    public String getItemColor() {
        return itemColor;
    }

    public void setItemColor(String itemColor) {
        this.itemColor = itemColor;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Timestamp getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Timestamp dateCreated) {
        this.dateCreated = dateCreated;
    }
}
