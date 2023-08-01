package org.example;

import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.BufferedReader;
import java.io.FileReader;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.Properties;

public class AirbnbCrawler extends AbstractCrawler {
    private final Logger logger = LogManager.getLogger(AirbnbCrawler.class);
    Properties properties = new Properties();
    private static final Marker INFO_MARKER = MarkerManager.getMarker("infoAirbnb");
    String configFile = "config.properties";
    ChromeDriver webDriver = null;
    ProductTable productTable = new ProductTable();
    DescriptionTable descriptionTable = new DescriptionTable();
    int index = 0;

    // 初始化chromeDriver設定, 載入設定檔
    AirbnbCrawler(Document document) {
        super(document);
        String airbnbUrl = "";
        String chromeDriver = "";
        String chromeDriverPath = "";
        String optionArgumentGpu = "";
        String optionArgumentDev = "";
        String optionArgumentImagesEnabled = "";
        String optionSetBinary = "";
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(configFile));
            properties.load(bufferedReader);
            airbnbUrl = properties.getProperty("airbnbUrl");
            chromeDriver = properties.getProperty("chromeDriver");
            chromeDriverPath = properties.getProperty("chromeDriverPath");
            optionArgumentGpu = properties.getProperty("optionArgumentGpu");
            optionArgumentDev = properties.getProperty("optionArgumentDev");
            optionArgumentImagesEnabled = properties.getProperty("optionArgumentImagesEnabled");
            optionSetBinary = properties.getProperty("optionSetBinary");
        } catch (Exception e) {
            logger.error(e);
        }
        System.getProperties().setProperty(chromeDriver, chromeDriverPath);
        ChromeOptions options = new ChromeOptions();
        options.addArguments(optionArgumentGpu);
        options.addArguments(optionArgumentDev);
        options.addArguments(optionArgumentImagesEnabled);
        options.setBinary(optionSetBinary);
        webDriver = new ChromeDriver(options);

        webDriver.get(airbnbUrl);
    }

    //進行爬蟲程式
    public void start() {
        int page = 1;
        while (page < 3) {
            try {
                waitJavaScriptRendering();
                seleniumToJsoup();
                getElements(page);
                slidePage(page);
                page += 1;
            } catch (Exception e) {
                logger.error(e);
            }
        }
    }

    //等待JavaScript渲染
    public void waitJavaScriptRendering() {
        String loadingJavaScriptXpath = "//img[contains(@class, 'itu7ddv i1mla2as i1cqnm0r dir dir-ltr')]";
        Duration timeout = Duration.ofSeconds(20);
        WebDriverWait wait = new WebDriverWait(webDriver, timeout);
        WebElement htmlElement = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.xpath(loadingJavaScriptXpath)));
    }

    //將selenium轉為hemlString給Jsoup
    public void seleniumToJsoup() {
        String htmlString = webDriver.getPageSource();
        Document doc = Jsoup.parse(htmlString);
        setCurrentPage(doc);
    }

    //取出規格所需的三種Elements
    public void getElements(int page) {
        String nextUrlCssQuery = "main#site-content .c14whb16.dir.dir-ltr";
        String rentInformationCssQuery = ".g1qv1ctd.c1v0rf5q.dir.dir-ltr";
        String imgUrlCssQuery = ".itu7ddv.i1mla2as.i1cqnm0r.dir.dir-ltr";
        Elements imgUrlElements = getCurrentPageElements(imgUrlCssQuery);
        Elements rentInformationElements = getCurrentPageElements(rentInformationCssQuery);
        Elements nextUrlElements = getCurrentPageElements(nextUrlCssQuery);
        for (; index < 12 * page; index++) {
            getElement(imgUrlElements.get(index), rentInformationElements.get(index), nextUrlElements.get(index));
        }
    }

    @Override
    void getElement(Element element) {
        //並未使用
    }

    //從三種Elements中取得規格所需的資料並寫入資料庫
    @Override
    void getElement(Element imgElement, Element rentElement, Element nextUrlElement) {
        getImgUrl(imgElement);
        getTitle(rentElement);
        getPriceType(rentElement);
        getPrice(rentElement);
        getDescription(nextUrlElement);
        productTable.setSourcesId(1);
//        writeDatabase(productTable, descriptionTable);
        writeLog(productTable, descriptionTable, INFO_MARKER);
    }

    //取得圖片的URL
    @Override
    public void getImgUrl(Element element) {
        String imgUrlCssQuery = "data-original-uri";
        String imgUrl = element.attr(imgUrlCssQuery);
        productTable.setImg(imgUrl);
    }

    //取得標題文字
    @Override
    public void getTitle(Element element) {
        try {
            String landLordCssQuery = ".fb4nyux.s1cjsi4j.dir.dir-ltr";
            String areaCssQuery = ".t1jojoys.dir.dir-ltr";
            String area = element.select(areaCssQuery).text();
            String landLord = element.select(landLordCssQuery).first().text();
            String title = area + " " + landLord;
            productTable.setTitle(title);
        } catch (Exception e) {
            logger.error(e);
        }

    }

    //取得價錢
    @Override
    public void getPrice(Element element) {
        String priceCssQuery = ".a8jt5op.dir.dir-ltr";
        String[] price = element.select(priceCssQuery).text().split(" ");
        BigDecimal bigDecimal = new BigDecimal(
                price[1].substring(1).replace(",", ""));
        productTable.setPrice(bigDecimal);
    }

    //取得幣值
    @Override
    public void getPriceType(Element element) {
        String priceCssQuery = ".a8jt5op.dir.dir-ltr";
        String[] price = element.select(priceCssQuery).text().split(" ");
        String priceType = price[2];
        productTable.setPriceType(priceType);
    }

    //透過一個新的webdriver來實作點擊下一頁並取得新視窗的所需資料
    public void newWindow(Element element) {
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
    public void getDescription(Element element) {
        newWindow(element);
        String descriptionCssQuery = "div._b8stb0 h1";
        Elements descriptionElements = getNextPageElements(descriptionCssQuery);
        String description = descriptionElements.text();
        descriptionTable.setDescription(description);
    }

    //滑動頁面每0.05秒30點像素
    public void slidePage(int page) {
        int verticalOffset = 3000;
        try {
            for (int i = 0; i <= 100; i++) {
                String jsScript = "window.scrollBy(0, " + (verticalOffset * page) / 100 + ")";
                JavascriptExecutor jsExecutor = (JavascriptExecutor) webDriver;
                jsExecutor.executeScript(jsScript);
                Thread.sleep(50);
            }

        } catch (Exception e) {
            logger.error(e);
        }
    }
}
