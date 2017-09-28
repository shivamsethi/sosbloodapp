package com.shivam.sosblood.utils;

import java.util.Calendar;
import java.util.Date;

public class DateHandler {

    public String getSimplifiedDate(Date date)
    {
        StringBuilder builder=new StringBuilder("");
        Date now=new Date();
        long minutes_difference=(now.getTime()-date.getTime())/60000;
        if(minutes_difference==0)
        {
            builder.append("Just now");
        }
        else if(minutes_difference<60)
        {
            builder.append(minutes_difference);
            builder.append(" min. ago");
        }
        else if(minutes_difference<1440)
        {
            long hours=minutes_difference/60;
            if(minutes_difference%60>30)
                hours++;
            builder.append(hours);
            builder.append(" hr. ago");
        }
        else
        {
            Calendar date_calendar=Calendar.getInstance();
            Calendar now_calendar=Calendar.getInstance();
            now_calendar.setTime(now);
            date_calendar.setTime(date);
            if(now_calendar.get(Calendar.YEAR)==date_calendar.get(Calendar.YEAR))
            {
                builder.append(getDayString(date_calendar.get(Calendar.DAY_OF_WEEK)));
                builder.append(",");
                builder.append(date_calendar.get(Calendar.DAY_OF_MONTH));
                builder.append(" ");
                builder.append(this.getMonthName(date_calendar.get(Calendar.MONTH)));
                builder.append(", ");
                builder.append(makeTwoDigitNumberString(date_calendar.get(Calendar.HOUR)));
                builder.append(":");
                builder.append(makeTwoDigitNumberString(date_calendar.get(Calendar.MINUTE)));
                builder.append(" ");
                builder.append(makeAmPmString(date_calendar.get(Calendar.AM_PM)));
            }
            else
            {
                builder.append(date_calendar.get(Calendar.DAY_OF_MONTH));
                builder.append(" ");
                builder.append(this.getMonthName(date_calendar.get(Calendar.MONTH)));
                builder.append(",");
                builder.append(date_calendar.get(Calendar.YEAR));
                builder.append(", ");
                builder.append(makeTwoDigitNumberString(date_calendar.get(Calendar.HOUR)));
                builder.append(":");
                builder.append(makeTwoDigitNumberString(date_calendar.get(Calendar.MINUTE)));
                builder.append(" ");
                builder.append(makeAmPmString(date_calendar.get(Calendar.AM_PM)));
            }
        }

        return builder.toString();
    }


    private String makeAmPmString(int am_pm)
    {
        if(am_pm==0)
            return "am";
        else return "pm";
    }


    private String makeTwoDigitNumberString(int num)
    {
        String number;
        if(num<9)
            number="0"+num;
        else number=Integer.toString(num);
        return number;
    }


    private String getDayString(int day)
    {
        String day_string;

        switch (day)
        {
            case Calendar.SUNDAY:
                day_string="Sunday";
                break;

            case Calendar.MONDAY:
                day_string="Monday";
                break;

            case Calendar.TUESDAY:
                day_string="Tuesday";
                break;

            case Calendar.WEDNESDAY:
                day_string="Wednesday";
                break;

            case Calendar.THURSDAY:
                day_string="Thursday";
                break;

            case Calendar.FRIDAY:
                day_string="Friday";
                break;

            case Calendar.SATURDAY:
                day_string="Saturday";
                break;

            default:day_string="";
        }

        return day_string;
    }


    private String getMonthName(int month)
    {
        String month_name="";

        switch (month)
        {
            case Calendar.JANUARY:
                month_name="January";
                break;

            case Calendar.FEBRUARY:
                month_name="February";
                break;

            case Calendar.MARCH:
                month_name="March";
                break;

            case Calendar.APRIL:
                month_name="April";
                break;

            case Calendar.MAY:
                month_name="May";
                break;

            case Calendar.JUNE:
                month_name="June";
                break;

            case Calendar.JULY:
                month_name="July";
                break;

            case Calendar.AUGUST:
                month_name="August";
                break;

            case Calendar.SEPTEMBER:
                month_name="September";
                break;

            case Calendar.OCTOBER:
                month_name="October";
                break;

            case Calendar.NOVEMBER:
                month_name="November";
                break;

            case Calendar.DECEMBER:
                month_name="December";
                break;
        }
        return month_name;
    }
}
