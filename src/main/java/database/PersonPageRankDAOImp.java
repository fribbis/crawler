package database;

import http.DateTimeConverter;
import org.apache.commons.dbcp2.BasicDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PersonPageRankDAOImp implements PersonPageRankDAO {

    private BasicDataSource dataSource;

    public PersonPageRankDAOImp() {
        dataSource = DataSource.getDataSource();
    }

    @Override
    public void insert(PersonsPageRank personsPageRank) throws SQLException {
        final String request = "insert ignore into personspagerank (personID, pageID, rank) values (?, ?, ?) " +
                "on duplicate key update rank = case when rank != ? then ? else rank end";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(request)) {
            int rank = personsPageRank.getRank();
            preparedStatement.setInt(1, personsPageRank.getPersonId());
            preparedStatement.setInt(2, personsPageRank.getPageId());
            preparedStatement.setInt(3, rank);
            preparedStatement.setInt(4, rank);
            preparedStatement.setInt(5, rank);
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new SQLException();
        }
    }
}
