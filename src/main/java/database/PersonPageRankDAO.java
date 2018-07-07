package database;

import java.sql.SQLException;

public interface PersonPageRankDAO {
    void insert(PersonsPageRank personsPageRank) throws SQLException;
}
