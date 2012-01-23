/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jams.workspace.plugins.waterml2;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;

/**
 *
 * @author chris
 */
public class Common {
    public static SimpleDateFormat WML2_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

    public static GregorianCalendar parseTime(String time) throws ParseException{
        time = time.replaceAll(":\\d\\d$", "00");

        //time="2011-06-08T12:23:31GMT+07:00";
        //System.out.println(time);
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(WML2_TIME_FORMAT.parse(time));

        return cal;
    }
}
