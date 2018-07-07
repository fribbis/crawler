package database;

import org.apache.commons.dbcp2.BasicDataSource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class KeywordDAOImp implements KeywordDAO {
    private BasicDataSource dataSource;

    public KeywordDAOImp() {
        dataSource = DataSource.getDataSource();
    }

    @Override
    public List<Keyword> findByPersonId(int personId) {
        List<Keyword> keywords = new ArrayList<>();
        final String request = String.format("select * from keywords where PersonID = %d;", personId);
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(request)) {
            while (resultSet.next()) {
                Keyword keyword = new Keyword();
                keyword.setKeywordId(resultSet.getInt("id"));
                keyword.setName(resultSet.getString("name").toLowerCase());
                keyword.setPersonId(resultSet.getInt("PersonID"));
                keywords.add(keyword);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return keywords;
    }
}
