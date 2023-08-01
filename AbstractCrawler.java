package org.example;

import org.apache.logging.log4j.Marker;
import org.jsoup.nodes.*;
import org.jsoup.select.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.math.BigDecimal;

public abstract class AbstractCrawler {
    private final Logger logger = LogManager.getLogger(AirbnbCrawler.class);
    private Document currentPage;
    private Document nextPage;
    DataBase db = new DataBase();

    //設定當前頁面以及下一個要爬取的頁面
    public AbstractCrawler(Document document) {
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
    abstract void getImgUrl(Element element);
    abstract void getTitle(Element element);
    abstract void getPriceType(Element element);
    abstract void getPrice(Element element);
    abstract void getDescription(Element element);
    //寫入資料庫
    public void writeDatabase(ProductTable productTable, DescriptionTable descriptionTable){
        String title = productTable.getTitle();
        int productId;
        db.insertProductTable(productTable);
        productId= db.getProductId(title);
        descriptionTable.setProductId(productId);
        db.insertSynopsisTable(descriptionTable);
    }
    //寫入Log檔
    public void writeLog(ProductTable productTable, DescriptionTable descriptionTable, Marker INFO_MARKER){
        int sourcesId = productTable.getSourcesId();
        String title = productTable.getTitle();
        String img = productTable.getImg();
        String priceType = productTable.getPriceType();
        BigDecimal price = productTable.getPrice();
        String description = descriptionTable.getDescription();
        logger.info(INFO_MARKER,"書名：" + title);
        logger.info(INFO_MARKER,"圖片：" + img);
        logger.info(INFO_MARKER,"幣別：" + priceType);
        logger.info(INFO_MARKER,"價錢：" + price);
        logger.info(INFO_MARKER,"介紹：" + description);
    }
    //控制爬取頁面時間
    public void timeSleep(){
        try {
            Thread.sleep(3000);
        }catch(Exception e){

        }
    }
}
