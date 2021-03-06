package database;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;

public class SiteDAOImp implements SiteDAO {

    private BasicDataSource dataSource;
    private  static Logger logger = LogManager.getLogger(SiteDAOImp.class);

    public SiteDAOImp() {
        dataSource = DataSource.getDataSource();
    }

    @Override
    public LinkedList<Site> findAll() {
        LinkedList<Site> sites = new LinkedList<>();
        final String request = "select * from sites limit 1000;";
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(request)) {
            while (resultSet.next()) {
                Site site = new Site();
                site.setSiteId(resultSet.getInt("id"));
                site.setName(resultSet.getString("name").toLowerCase());
                site.setAddedBy(resultSet.getInt("addedBy"));
                sites.add(site);
            }
        } catch (SQLException e) {
            logger.error("SQLException:", e);
        }
        return sites;
    }
}
