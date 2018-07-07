package database;

import java.util.List;

public class PersonAndKeywords {
    private int personId;
    private List<String> keywords;

    public PersonAndKeywords(int personId, List<String> keywords) {
        this.personId = personId;
        this.keywords = keywords;
    }

    public int getPersonId() {
        return personId;
    }

    public void setPersonId(int personId) {
        this.personId = personId;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    @Override
    public String toString() {
        return String.format("id: %d, keywords: %s", personId, keywords);
    }
}
