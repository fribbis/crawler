package database;

public class Person {
    private int personId;
    private String name;
    private int addedBy;

    public Person() {}

    public Person(String name, int addedBy) {
        this.name = name;
        this.addedBy = addedBy;
    }

    public int getPersonId() {
        return personId;
    }

    public void setPersonId(int personId) {
        this.personId = personId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAddedBy() {
        return addedBy;
    }

    public void setAddedBy(int addedBy) {
        this.addedBy = addedBy;
    }

    @Override
    public String toString() {
        return String.format("id: %d, name: %s, addedBy: %d", personId, name, addedBy);
    }
}
