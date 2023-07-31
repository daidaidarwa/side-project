package org.example;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.sql.*;
import java.util.Arrays;
import java.util.HashMap;

public class DataBase {
    private static final Logger logger = LogManager.getLogger(DataBase.class);
    private final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    private final String DB_URL = "jdbc:mysql://127.0.0.1:3307/FINAL_PROJECT?serverTimezone=UTC&useSSL=false";
    private final String USER = "app_user";
    private final String PASS = "root";
    private Connection conn;
    public PreparedStatement preparedStatement;

    DataBase() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            logger.info("Db initialize success");
        } catch (Exception e) {
            logger.error("Db initialize error :" + e);
        }
    }

    public int getProductId(String title) {
        int productId = 0;
        try {
            preparedStatement = conn.prepareStatement("SELECT `id` FROM product WHERE `title`=?");
            preparedStatement.setString(1, title);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                productId = resultSet.getInt("id");
            }
        } catch (Exception e) {
            logger.error("cant find title in db");
        }
        return productId;
    }

    public void insertProductTable(String title, String img, String moneyType, BigDecimal price, String sourcesName) {
        String sql = "INSERT INTO product (`sources_id`, `title`, `img`, `currency`, `price`) VALUES ( ?, ?, ?, ?, ?)";
        HashMap<String, Integer> sourcesId = new HashMap<>();
        sourcesId.put("Books to Scrape", 1);
        sourcesId.put("airbnb", 2);
        try {
            int productId = getProductId(title);
            boolean func = checkDataInDataBase(productId);
            if (func) {
                logger.info("Data exist");
            } else {
                preparedStatement = conn.prepareStatement(sql);
                preparedStatement.setInt(1, sourcesId.get(sourcesName));
                preparedStatement.setString(2, title);
                preparedStatement.setString(3, img);
                preparedStatement.setString(4, moneyType);
                preparedStatement.setBigDecimal(5, price);
                preparedStatement.execute();
                logger.info("insert db success");
            }
        } catch (Exception e) {
            logger.error("SQL Syntax error：" + e);
        } finally {
            try {
                preparedStatement.close();

            } catch (Exception e) {
                logger.error(e);
            }
        }
    }

    public void insertSynopsisTable(String title, String description) {
        String sql = "INSERT INTO synopsis (`product_id`, `description`) VALUES (?, ?)";
        char[] separatorLine = new char[200];
        Arrays.fill(separatorLine, '-');
        String spearatorString = new String(separatorLine);
        try {
            int productId = getProductId(title);
            boolean func = checkDataInDataBase(productId);
            if (func) {
                logger.info("Data exist");
            } else {
                preparedStatement = conn.prepareStatement(sql);
                preparedStatement.setInt(1, productId);
                preparedStatement.setString(2, description);
                preparedStatement.execute();
                logger.info("insert db success");
            }

        } catch (Exception e) {
            logger.error("SQL Syntax error：" + e);
        } finally {
            try {
                preparedStatement.close();
                logger.info(spearatorString);
            } catch (Exception e) {
                logger.error(e);
            }
        }
    }

    public boolean checkDataInDataBase(int productId) {
        boolean func = false;
        try {
            preparedStatement = conn.prepareStatement("SELECT `id` FROM synopsis WHERE `product_id`=?");
            preparedStatement.setInt(1, productId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                func = true;
            } else {
                func = false;
            }
        } catch (Exception e) {
            logger.error("cant find title in db");
        } finally {
            try {
                preparedStatement.close();
            } catch (Exception e) {
                logger.error(e);
            }
        }
        return func;
    }

    public ResultSet getSearchData(int id) {
        ResultSet resultSet = null;
        try {
            preparedStatement = conn.prepareStatement("SELECT sources.sourceName, product.title," +
                    "product.img, product.currency, product.price, synopsis.`description`" +
                    "FROM product LEFT JOIN sources ON product.sources_id = sources.id " +
                    "LEFT JOIN synopsis ON product.id = synopsis.`product_id`" +
                    "WHERE product.`sources_id`=?;");
            preparedStatement.setInt(1, id);
            resultSet = preparedStatement.executeQuery();
        } catch (Exception e) {
            logger.error(e);
        }
        return resultSet;
    }

    public void close() {
        try {
            this.conn.close();
            this.preparedStatement.close();
        } catch (Exception e) {
            logger.error(e);
        }
    }
}
