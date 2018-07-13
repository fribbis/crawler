package http;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class DateTimeConverter {
    public static Date convertStringToDate(String dateString) {
        Date dateTime = new Date();
        try {
            if (dateString.length() == 10) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                dateTime = simpleDateFormat.parse(dateString);
            }
            else if (dateString.length() >= 19) {
                String[] arrDateTime;
                arrDateTime = dateString.split("[ T+]");
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                dateTime = simpleDateFormat.parse(arrDateTime[0] + " " + arrDateTime[1]);
            }
        }
        catch (ParseException e) {
            e.printStackTrace();
        }
        return dateTime;
    }

    public static String convertDateToString(Date date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return simpleDateFormat.format(date);
    }
}
