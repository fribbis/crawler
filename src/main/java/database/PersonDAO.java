package database;

import java.util.LinkedList;

public interface PersonDAO {
    LinkedList<Person> findAll();
    Person findById(int id);
}
