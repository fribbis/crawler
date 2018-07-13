package database;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;

public class PersonDAOImp implements PersonDAO {
    private BasicDataSource dataSource;
    private  static Logger logger = LogManager.getLogger(PersonDAOImp.class);

    public PersonDAOImp() {
        dataSource = DataSource.getDataSource();
    }

    @Override
    public LinkedList<Person> findAll() {
        LinkedList<Person> persons = new LinkedList<>();
        final String request = "select * from persons limit 1000;";
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(request)) {
            while (resultSet.next()) {
                Person person = new Person();
                person.setPersonId(resultSet.getInt("id"));
                person.setName(resultSet.getString("name").toLowerCase());
                person.setAddedBy(resultSet.getInt("addedBy"));
                persons.add(person);
            }
        } catch (SQLException e) {
            logger.error("SQLException:", e);
        }
        return persons;
    }

    @Override
    public Person findById(int id) {
        Person person = null;
        final String request = String.format("select * from persons where id = %d;", id);
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(request)) {
            if (resultSet.next()) {
                person = new Person();
                person.setPersonId(id);
                person.setName(resultSet.getString("name").toLowerCase());
                person.setAddedBy(resultSet.getInt("addedBy"));
            }
        } catch (SQLException e) {
            logger.error("SQLException:", e);
        }
        return person;
    }
}
