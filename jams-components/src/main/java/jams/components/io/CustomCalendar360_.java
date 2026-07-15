package jams.components.io;

import java.time.LocalDate;
import java.time.ZoneOffset;

public class CustomCalendar360_ {

    private static final int DAYS_PER_MONTH = 30;
    private static final int MONTHS_PER_YEAR = 12;
    private static final int DAYS_PER_YEAR = DAYS_PER_MONTH * MONTHS_PER_YEAR;
    private static final int BASE_YEAR = 1970;
    private static final LocalDate CUSTOM_EPOCH = LocalDate.of(BASE_YEAR, 1, 1); // Reference date

    private int year;
    private int month;
    private int day;

    public CustomCalendar360_(int year, int month, int day) {
        if (month < 1 || month > MONTHS_PER_YEAR || day < 1 || day > DAYS_PER_MONTH) {
            throw new IllegalArgumentException("Invalid date in custom calendar");
        }
        this.year = year;
        this.month = month;
        this.day = day;
    }

    public static CustomCalendar360_ fromGregorian(LocalDate date) {
        long daysSinceEpoch = date.toEpochDay() - CUSTOM_EPOCH.toEpochDay(); // Days since the custom epoch
        int customYear = (int) (daysSinceEpoch / DAYS_PER_YEAR) + BASE_YEAR;
        int remainingDays = (int) (daysSinceEpoch % DAYS_PER_YEAR);
        if (remainingDays < 0) {
            customYear--;
            remainingDays += DAYS_PER_YEAR;
        }
        int customMonth = remainingDays / DAYS_PER_MONTH + 1;
        int customDay = remainingDays % DAYS_PER_MONTH + 1;

        return new CustomCalendar360_(customYear, customMonth, customDay);
    }

    public LocalDate toGregorian() {
        long totalDays = (long) (year - BASE_YEAR) * DAYS_PER_YEAR + (month - 1) * DAYS_PER_MONTH + (day - 1);
        return CUSTOM_EPOCH.plusDays(totalDays);
    }

    public void addDays(int days) {
        long totalDays = (long) (year - BASE_YEAR) * DAYS_PER_YEAR + (month - 1) * DAYS_PER_MONTH + (day - 1) + days;
        year = (int) (totalDays / DAYS_PER_YEAR) + BASE_YEAR;
        int remainingDays = (int) (totalDays % DAYS_PER_YEAR);
        if (remainingDays < 0) {
            year--;
            remainingDays += DAYS_PER_YEAR;
        }
        month = remainingDays / DAYS_PER_MONTH + 1;
        day = remainingDays % DAYS_PER_MONTH + 1;
    }

    public long getTimeInMillis() {
        // Convert the custom calendar date to a Gregorian date
        LocalDate gregorianDate = toGregorian();

        // Convert the Gregorian date to milliseconds since epoch
        return gregorianDate.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli();
    }

    public long calculateOffset(CustomCalendar360_ other) {
        // Calculate the total days for both instances
        long totalDaysThis = (long) (this.year - BASE_YEAR) * DAYS_PER_YEAR 
                           + (this.month - 1) * DAYS_PER_MONTH 
                           + (this.day - 1);

        long totalDaysOther = (long) (other.year - BASE_YEAR) * DAYS_PER_YEAR 
                            + (other.month - 1) * DAYS_PER_MONTH 
                            + (other.day - 1);

        // Return the difference
        return totalDaysThis - totalDaysOther;
    }

    public String toCustomDateString() {
        return String.format("%04d-%02d-%02d", year, month, day);
    }

    public static void main(String[] args) {
        // Example usage
        CustomCalendar360_ customDate1 = new CustomCalendar360_(1975, 2, 1);
        CustomCalendar360_ customDate2 = new CustomCalendar360_(1974, 12, 15);

        System.out.println("Custom Date 1: " + customDate1.toCustomDateString());
        System.out.println("Custom Date 2: " + customDate2.toCustomDateString());

        // Calculate offset
        long offset = customDate1.calculateOffset(customDate2);
        System.out.println("Offset (days) between Date 1 and Date 2: " + offset);

        // Convert to Gregorian
        LocalDate gregorianDate1 = customDate1.toGregorian();
        System.out.println("Equivalent Gregorian Date 1: " + gregorianDate1);

        // Add days
        customDate1.addDays(-10);
        System.out.println("Custom Date 1 after subtracting 10 days: " + customDate1.toCustomDateString());

        // Get time in milliseconds
        System.out.println("Time in milliseconds for Date 1: " + customDate1.getTimeInMillis());
    }
}
