package org.example;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Properties;

public class SimpleHtml extends WebAnalyze{
    Properties properties = new Properties();
    //設定初始化參數
    SimpleHtml(Document currentPage){
        super(currentPage);
    }
    //開始實作爬蟲
    public void start(){
        int page = 1;
        String pageElementsCssQuery;
        Elements currentPageElements;
        Document nextCurrentPage;
        String changeCurrentPage;
        while(page<3){
            try {
                pageElementsCssQuery = properties.getProperty("BooksToScrapeElementsCssQuery");
                currentPageElements = getCurrentPageElements(pageElementsCssQuery);
                for(Element element: currentPageElements){
                    getElement(element);
                }
                changeCurrentPage = "http://books.toscrape.com/catalogue/category/books_1/page-"
                        + page + "html.";
                nextCurrentPage = Jsoup.connect(changeCurrentPage).get();
                setCurrentPage(nextCurrentPage);
                page+=1;
            }catch (Exception e){
//                logger.error(e);
            }
        }

    }
    //從爬到的頁面中取得規格所需的資料
    @Override
    public void getElement(Element element){
        String imgUrl = getImgUrl(element);
        String title = getTitle(element);
        BigDecimal price = getPrice(element);
        String priceType = getPriceType(element);
        String description = getDescription(element);
        writeDatabase(imgUrl, title, priceType, price, description, "Book To Scrape");
//        writeLog(imgUrl, title, priceType, price, description);
    }
    @Override
    public void getElement(Element rentElement, Element imgElement, Element nextUrlElement){
        //並未使用
    }
    //取得圖片網址
    @Override
    public String getImgUrl(Element element){
        String imgUrlCssQuery = "div>a>img";
        String imgUrl = "http://books.toscrape.com/" +
                element.select(imgUrlCssQuery).attr("src").substring(8);
        return imgUrl;
    }
    //取得標題文字
    @Override
    public String getTitle(Element element){
        String titleCssQuery = "h3>a";
        String title = element.select(titleCssQuery).text();
        return title;
    }
    //取得價錢
    @Override
    public BigDecimal getPrice(Element element){
        String priceCssQuery = ".price_color";
        String price = element.select(priceCssQuery).text();
        BigDecimal bigDecimal = new BigDecimal(
                price.substring(1).replace(",", ""));
        return bigDecimal;
    }
    //取得幣值
    @Override
    public String getPriceType(Element element){
        HashMap<String, String> currency = new HashMap<>();
        currency.put("£", "GBP");
        String priceCssQuery = ".price_color";
        String price = element.select(priceCssQuery).text();
        String priceType = currency.get(price.substring(0, 1));
        return priceType;
    }
    //取得產品介紹文字
    @Override
    public String getDescription(Element element){
        String titleCssQuery = "h3>a";
        String productDescriptionCssQuery = "#content_inner > article > p";
        String nextPageUrl =  "http://books.toscrape.com/catalogue/"  +
                element.select(titleCssQuery).attr("href").substring(6);
        String description="";
        try {
            setNextPage(Jsoup.connect(nextPageUrl).get());
            Elements nextPageElements = getNextPageElements(productDescriptionCssQuery);
            description = nextPageElements.text();
            timeSleep();
        }catch (Exception e){
//            logger.error(e);
        }
        return description;
    }
}
