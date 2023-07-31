package org.example;

import org.jsoup.nodes.*;
import org.jsoup.select.*;

import java.math.BigDecimal;

public abstract class WebAnalyze {
    private Document currentPage;
    private Document nextPage;
    DataBase db = new DataBase();
    //設定當前頁面以及下一個要爬取的頁面
    public WebAnalyze(Document document) {
        this.currentPage = document;
        this.nextPage = null;
    }
    //重新設定當前頁面
    public void setCurrentPage(Document currentPage){
        this.currentPage = currentPage;
    }
    //設定該產品的下一個頁面
    public void setNextPage(Document nextPage) {
        this.nextPage = nextPage;
    }
    //取得當前頁面所需的Elements
    public Elements getCurrentPageElements(String cssQuery){
        Elements currentPageElements = this.currentPage.select(cssQuery);
        return currentPageElements;
    }
    //取得產品下一頁所需的Elements
    public Elements getNextPageElements(String cssQuery){
        Elements nextPageElements = this.nextPage.select(cssQuery);
        return nextPageElements;
    }
    //取得規格所需的Element
    abstract void getElement(Element element);
    abstract void getElement(Element rentElement, Element imgElement, Element nextUrlElement);
    //取得各種資料
    abstract String getImgUrl(Element element);
    abstract String getTitle(Element element);
    abstract String getPriceType(Element element);
    abstract BigDecimal getPrice(Element element);
    abstract String getDescription(Element element);
    //寫入資料庫
    public void writeDatabase(String title, String imgUrl, String priceType,
                              BigDecimal price, String productDescription, String sources){
        db.insertProductTable(title, imgUrl, priceType, price, sources);
        db.insertSynopsisTable(title, productDescription);
    }
    //寫入Log檔
//    public void writeLog(String title, String imgUrl, String priceType,
//                         BigDecimal price, String productDescription){
//        logger.info(INFO_MARKER,"書名：" + title);
//        logger.info(INFO_MARKER,"圖片：" + imgUrl);
//        logger.info(INFO_MARKER,"幣別：" + priceType);
//        logger.info(INFO_MARKER,"價錢：" + price);
//        logger.info(INFO_MARKER,"介紹：" + productDescription);
//    }
    //控制爬取頁面時間
    public void timeSleep(){
        try {
            Thread.sleep(3000);
        }catch(Exception e){

        }
    }
}
