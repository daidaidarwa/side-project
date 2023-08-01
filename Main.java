package org.example;

import org.jsoup.nodes.Document;

import java.util.Timer;
import java.util.TimerTask;
import java.util.Calendar;
import java.util.Date;

public class Main {
    public static void main(String[] args) {
        Timer timer = new Timer();
        Document document = null;
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 3);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        Date scheduledTime = calendar.getTime();
        TimerTask task = new TimerTask() {
            public void run() {
                // 呼叫你想要執行的方法
                AirbnbCrawler airbnbCrawler = new AirbnbCrawler(document);
                BooksToScrapeCrawler booksToScrapeCrawler = new BooksToScrapeCrawler(document);
                airbnbCrawler.start();
                booksToScrapeCrawler.start();
            }
        };
        timer.schedule(task, scheduledTime, 24 * 60 * 60 * 1000); // 每24小時執行一次

    }
}
