package pw.fusionmine.fusionantirelog.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class TimeUtils {

    public static String formatTimeUnit(String ed, String a, String b, int n) {
        if (n < 0) {
            n = -n;
        }
        int last = n % 100;
        if (last > 10 && last < 21) {
            return ed;
        }
        last = n % 10;
        if (last == 0 || last > 4) {
            return ed;
        }
        if (last == 1) {
            return ed + a;
        }
        if (last < 5) {
            return ed + b;
        }
        return ed;
    }

    public static String replaceTime(String message, int time) {
        return message.replace("%time%", Integer.toString(time)).replace("%formated-sec%",
                formatTimeUnit("секунд", "у", "ы", time));
    }

}
