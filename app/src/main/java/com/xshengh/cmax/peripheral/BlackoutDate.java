package com.xshengh.cmax.peripheral;

import java.util.Calendar;

/**
 * Created by xshengh on 2018/3/6.
 */

public class BlackoutDate {
    public boolean enabled;
    public Calendar start;
    public Calendar end;
    public BlackoutDate() {
        start = Calendar.getInstance();
        start.set(Calendar.YEAR, 2018);
        start.set(Calendar.MONTH, 1);
        start.set(Calendar.DATE, 1);
        end = Calendar.getInstance();
        end.set(Calendar.YEAR, 2018);
        end.set(Calendar.MONTH, 1);
        end.set(Calendar.DATE, 10);
    }
}
