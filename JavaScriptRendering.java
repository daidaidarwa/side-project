package org.example;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.HashMap;
import java.util.Properties;

public class JavaScriptRendering extends WebAnalyze{
    Properties properties = new Properties();
    ChromeDriver webDriver = null;
    int index =0;
    // 初始化chromeDriver設定, 載入設定檔
    JavaScriptRendering(Document document){
        super(document);
        System.getProperties().setProperty("webdriver.chrome.driver", "D:\\webDriver\\chromedriver.exe");
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-gpu");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("blink-settings=imagesEnabled=false");
        options.setBinary("C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe");
        ChromeDriver webDriver = new ChromeDriver(options);
        String airbnbUrl = properties.getProperty("airbnb");
        webDriver.get(airbnbUrl);
    }
    //進行爬蟲程式
    public void start(){
        int page = 1;
        while(page<3){
            try {
                waitJavaScriptRendering();
                seleniumToJsoup();
                getElements(page);
                sildePage(page);
                page+=1;
            }catch (Exception e){
//                logger.error(e);
            }
        }

    }
    //等待JavaScript渲染
    public void waitJavaScriptRendering(){
        String loadingJavaScriptXpath = "//img[contains(@class, 'itu7ddv i1mla2as i1cqnm0r dir dir-ltr')]";
        Duration timeout = Duration.ofSeconds(20);
        WebDriverWait wait = new WebDriverWait(webDriver, timeout);
        WebElement htmlElement = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.xpath(loadingJavaScriptXpath)));
    }
    //將selenium轉為hemlString給Jsoup
    public void seleniumToJsoup(){
        String htmlString = webDriver.getPageSource();
        Document doc = Jsoup.parse(htmlString);
        setCurrentPage(doc);
    }
    //取出規格所需的三種Elements
    public void getElements(int page){
        String nextUrlCssQuery = "main#site-content .c14whb16.dir.dir-ltr";
        String rentInformationCssQuery =".g1qv1ctd.c1v0rf5q.dir.dir-ltr";
        String imgUrlCssQuery = ".itu7ddv.i1mla2as.i1cqnm0r.dir.dir-ltr";
        Elements imgUrlElements = getCurrentPageElements(imgUrlCssQuery);
        Elements rentInformationElements = getCurrentPageElements(rentInformationCssQuery);
        Elements nextUrlElements = getCurrentPageElements(nextUrlCssQuery);
        for(; index<12*page;index++){
            getElement(imgUrlElements.get(index), rentInformationElements.get(index), nextUrlElements.get(index));
        }
    }
    @Override
    void getElement(Element element){
        //並未使用
    }
    //從三種Elements中取得規格所需的資料並寫入資料庫
    @Override
    void getElement(Element rentElement, Element imgElement, Element nextUrlElement) {
        String imgUrl = getImgUrl(imgElement);
        String title = getTitle(rentElement);
        String priceType = getPriceType(rentElement);
        BigDecimal price = getPrice(rentElement);
        String description = getDescription(nextUrlElement);
        db.insertProductTable(title, imgUrl, priceType, price, "airbnb");
        db.insertSynopsisTable(title, description);
    }
    //取得圖片的URL
    @Override
    public String getImgUrl(Element element){
        String imgUrlCssQuery = "data-original-uri";
        String imgUrl = element.attr(imgUrlCssQuery);
        return imgUrl;
    }
    //取得標題文字
    @Override
    public String getTitle(Element element){
        String landLordCssQuery = ".fb4nyux.s1cjsi4j.dir.dir-ltr";
        String areaCssQuery = ".t1jojoys.dir.dir-ltr";
        String area = element.select(areaCssQuery).text();
        String landLord = element.select(landLordCssQuery).first().text();
        String title = area + " " + landLord;
        return title;
    }
    //取得價錢
    @Override
    public BigDecimal getPrice(Element element){
        String priceCssQuery = ".a8jt5op.dir.dir-ltr";
        String[] price = element.select(priceCssQuery).text().split(" ");
        BigDecimal bigDecimal = new BigDecimal(
                price[1].substring(1).replace(",", ""));
        return bigDecimal;
    }
    //取得幣值
    @Override
    public String getPriceType(Element element){
        String priceCssQuery = ".a8jt5op.dir.dir-ltr";
        String[] price = element.select(priceCssQuery).text().split(" ");
        String priceType = price[1];
        return priceType;
    }
    //透過一個新的webdriver來實作點擊下一頁並取得新視窗的所需資料
    public void newWindow(Element element){
        String loadingJsXpath = "//h1[@class='hpipapi i1pmzyw7 dir dir-ltr']";
        String nextUrl = element.select("a[href]").get(0).attr("href");
        ChromeOptions newOptions = new ChromeOptions();
        newOptions.setBinary("C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe");
        ChromeDriver nextWebDriver = new ChromeDriver(newOptions);
        Duration timeOut = Duration.ofSeconds(20);
        WebDriverWait wait = new WebDriverWait(nextWebDriver, timeOut);
        nextWebDriver.get("https:/www.airbnb.com.tw" + nextUrl);
        WebElement htmlElement = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.xpath(loadingJsXpath)));
        Document doc = Jsoup.parse(nextWebDriver.getPageSource());
        setNextPage(doc);
        timeSleep();
        nextWebDriver.quit();
    }
    //取得介紹文字
    @Override
    public String getDescription(Element element){
        newWindow(element);
        String descriptionCssQuery = "div._b8stb0 h1";
        Elements descriptionElements = getNextPageElements(descriptionCssQuery);
        String description = descriptionElements.text();
        return description;
    }
    //滑動頁面每0.05秒30點像素
    public void sildePage(int page){
        int verticalOffset = 3000;
        try {
            for(int i=0; i<=100; i++) {
                String jsScript = "window.scrollBy(0, " + (verticalOffset * page)/100 + ")";
                JavascriptExecutor jsExecutor = (JavascriptExecutor) webDriver;
                jsExecutor.executeScript(jsScript);
                Thread.sleep(50);
            }

        }catch(Exception e){
//            logger.error(e);
        }
    }
}
