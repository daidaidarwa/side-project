package org.example;

public class DescriptionTable {
    private int productId;
    private String description;
    DescriptionTable(){
        this.description=null;
    }
    public void setProductId(int id){
        this.productId = id;
    }
    public void setDescription(String description){
        this.description = description;
    }
    public int getProductId(){
        return productId;
    }
    public String getDescription(){
        return description;
    }
}
