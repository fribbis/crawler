package http;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateTimeConverter {
    static Date convertStringToDate(String dateString) {
        Date dateTime = new Date();
        try {
            if (dateString.length() == 10) dateTime = new SimpleDateFormat("yyyy-MM-dd").parse(dateString);
            else if (dateString.length() >= 19) {
                String[] arrDateTime;
                arrDateTime = dateString.split("[ T+]");
                dateTime = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(arrDateTime[0] + " " + arrDateTime[1]);
            }
        }
        catch (ParseException e) {
            e.printStackTrace();
        }
        return dateTime;
    }
}
