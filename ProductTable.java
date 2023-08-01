package org.example;

import java.math.BigDecimal;

public class ProductTable {
    private int sourcesId;
    private String title;
    private String img;
    private String priceType;
    private BigDecimal price;
    ProductTable(){
        title = null;
        img = null;
        priceType = null;
        price = null;
    }
    public void setSourcesId(int id){
        this.sourcesId =id;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public void setImg(String img){
        this.img = img;
    }
    public void setPriceType(String priceType){
        this.priceType = priceType;
    }
    public void setPrice(BigDecimal price){
        this.price = price;
    }

    public int getSourcesId() {
        return sourcesId;
    }
    public String getTitle(){
        return title;
    }
    public String getImg(){
        return img;
    }
    public String getPriceType(){
        return priceType;
    }
    public BigDecimal getPrice(){
        return price;
    }
}
