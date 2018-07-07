package database;

public class Keyword {
    private int keywordId;
    private String name;
    private int personId;

    public Keyword() {}

    public Keyword(int keywordId, String name, int personId) {
        this.keywordId = keywordId;
        this.name = name;
        this.personId = personId;
    }

    public int getKeywordId() {
        return keywordId;
    }

    public void setKeywordId(int keywordId) {
        this.keywordId = keywordId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPersonId() {
        return personId;
    }

    public void setPersonId(int personId) {
        this.personId = personId;
    }

    @Override
    public String toString() {
        return String.format("id: %d, name: %s, personId: %d", keywordId, name, personId);
    }
}
