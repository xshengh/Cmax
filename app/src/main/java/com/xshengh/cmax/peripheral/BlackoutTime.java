package com.xshengh.cmax.peripheral;

import java.util.Calendar;

/**
 * Created by xshengh on 2018/3/5.
 */

public class BlackoutTime {
    public Calendar start;
    public Calendar end;
    public int weekday;
    public boolean enabled;

    public BlackoutTime() {
        start = Calendar.getInstance();
        start.set(Calendar.HOUR_OF_DAY, 10);
        start.set(Calendar.MINUTE, 0);
        end = Calendar.getInstance();
        end.set(Calendar.HOUR_OF_DAY, 11);
        end.set(Calendar.MINUTE, 0);
        weekday = 0b0111110;
    }
}
