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
    private final String DB_URL = "jdbc:mysql://127.0.0.1:3307/FINAL_PROJECT";
    private final String USER = "app_user";
    private final String PASS = "root";
    private Connection conn;
    public PreparedStatement preparedStatement;

    DataBase() {
        try {
            Class.forName(JDBC_DRIVER);
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

    public void insertProductTable(ProductTable productTable) {
        String sql = "INSERT INTO product (`sources_id`, `title`, `img`, `currency`, `price`)" +
                " VALUES ( ?, ?, ?, ?, ?) WHERE NOT EXISTS (SELECT * FROM product WHERE `title`= ?)";
        int sourcesId = productTable.getSourcesId();
        String title = productTable.getTitle();
        String img = productTable.getImg();
        String priceType = productTable.getPriceType();
        BigDecimal price = productTable.getPrice();
        try {
            preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setInt(1, sourcesId);
            preparedStatement.setString(2, title);
            preparedStatement.setString(3, img);
            preparedStatement.setString(4, priceType);
            preparedStatement.setBigDecimal(5, price);
            preparedStatement.setString(6, title);
            preparedStatement.execute();
            logger.info("insert productTable success");
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

    public void insertSynopsisTable(DescriptionTable descriptionTable) {
        int productId = descriptionTable.getProductId();
        String sql = "INSERT INTO synopsis (`product_id`, `description`) VALUES (?, ?) " +
                "WHERE NOT EXISTS (SELECT * FROM synopsis WHERE `product_id` = ?)";
        String description = descriptionTable.getDescription();
        try {
            preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setInt(1, productId);
            preparedStatement.setString(2, description);
            preparedStatement.setInt(3, productId);
            preparedStatement.execute();
            logger.info("insert synopsisTable success product_id: " + productId);
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

    public void close() {
        try {
            this.conn.close();
            this.preparedStatement.close();
        } catch (Exception e) {
            logger.error(e);
        }
    }
}
