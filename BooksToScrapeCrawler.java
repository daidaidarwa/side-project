package org.example;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.FileReader;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Properties;

public class BooksToScrapeCrawler extends AbstractCrawler{
    private final Logger logger = LogManager.getLogger(AirbnbCrawler.class);
    private static final Marker INFO_MARKER = MarkerManager.getMarker("infoBooksToScrape");
    Properties properties = new Properties();
    String configFile = "config.properties";
    ProductTable productTable = new ProductTable();
    DescriptionTable descriptionTable = new DescriptionTable();

    //設定初始化參數
    BooksToScrapeCrawler(Document doc){
        super(doc);
        String bookToScrapeCategoryTag;
        String targetUrl;
        Document currentDocument;
        String bookToScrapeCategoryPath;
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(configFile));
            properties.load(bufferedReader);
            bookToScrapeCategoryPath = properties.getProperty("bookToScrapeCategoryPath");
            bookToScrapeCategoryTag = properties.getProperty("bookToScrapeCategoryTag");
            targetUrl = bookToScrapeCategoryPath + "/category/" + bookToScrapeCategoryTag + "/index.html";
            currentDocument = Jsoup.connect(targetUrl).get();
            setCurrentPage(currentDocument);
        } catch (Exception e) {
            logger.error(e);
        }

    }
    //開始實作爬蟲
    public void start(){
        int page = 1;
        String pageElementsCssQuery;
        Elements currentPageElements;
        Document nextCurrentPage;
        String changeCurrentPage;
        String bookToScrapeCategoryPath;
        String bookToScrapeCategoryTag;
        while(page<3){
            try {
                pageElementsCssQuery = properties.getProperty("bookToScrapePageElementsCssQuery");
                bookToScrapeCategoryPath = properties.getProperty("bookToScrapeCategoryPath");
                bookToScrapeCategoryTag = properties.getProperty("bookToScrapeCategoryTag");
                currentPageElements = getCurrentPageElements(pageElementsCssQuery);
                for(Element element: currentPageElements){
                    getElement(element);
                }
                changeCurrentPage = bookToScrapeCategoryPath + bookToScrapeCategoryTag + "/page-" + page + ".html";
                nextCurrentPage = Jsoup.connect(changeCurrentPage).get();
                setCurrentPage(nextCurrentPage);
                timeSleep();
                page+=1;
            }catch (Exception e){
                logger.error(INFO_MARKER, ""+e);
            }
        }

    }
    //從爬到的頁面中取得規格所需的資料
    @Override
    public void getElement(Element element){
        getImgUrl(element);
        getTitle(element);
        getPriceType(element);
        getPrice(element);
        getDescription(element);
        productTable.setSourcesId(1);
//        writeDatabase(productTable, descriptionTable);
        writeLog(productTable, descriptionTable, INFO_MARKER);
    }
    @Override
    public void getElement(Element rentElement, Element imgElement, Element nextUrlElement){
        //並未使用
    }
    //取得圖片網址
    @Override
    public void getImgUrl(Element element){
        String imgUrlCssQuery = "div>a>img";
        String bookToScrapeHomePagePath = properties.getProperty("bookToScrapeHomePagePath");
        String imgUrl = bookToScrapeHomePagePath +
                element.select(imgUrlCssQuery).attr("src").substring(8);
        productTable.setImg(imgUrl);
    }
    //取得標題文字
    @Override
    public void getTitle(Element element){
        String titleCssQuery = "h3>a";
        String title = element.select(titleCssQuery).text();
        productTable.setTitle(title);
    }
    //取得價錢
    @Override
    public void getPrice(Element element){
        String priceCssQuery = ".price_color";
        String price = element.select(priceCssQuery).text();
        BigDecimal bigDecimal = new BigDecimal(
                price.substring(1).replace(",", ""));
        productTable.setPrice(bigDecimal);
    }
    //取得幣值
    @Override
    public void getPriceType(Element element){
        HashMap<String, String> currency = new HashMap<>();
        currency.put("£", "GBP");
        String priceCssQuery = ".price_color";
        String price = element.select(priceCssQuery).text();
        String priceType = currency.get(price.substring(0, 1));
        productTable.setPriceType(priceType);
    }
    //取得產品介紹文字
    @Override
    public void getDescription(Element element){
        String titleCssQuery = "h3>a";
        String productDescriptionCssQuery = "#content_inner > article > p";
        String bookToScrapeCategoryPath = properties.getProperty("bookToScrapeCategoryPath");
        String nextPageUrl =  bookToScrapeCategoryPath +
                element.select(titleCssQuery).attr("href").substring(6);
        String description="";
        try {
            setNextPage(Jsoup.connect(nextPageUrl).get());
            Elements nextPageElements = getNextPageElements(productDescriptionCssQuery);
            description = nextPageElements.text();
            timeSleep();
        }catch (Exception e){
            logger.error(INFO_MARKER, ""+e);
        }
        descriptionTable.setDescription(description);
    }
}
