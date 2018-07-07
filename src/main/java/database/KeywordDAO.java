package database;

import java.util.List;

public interface KeywordDAO {
    List<Keyword> findByPersonId(int personId);
}
