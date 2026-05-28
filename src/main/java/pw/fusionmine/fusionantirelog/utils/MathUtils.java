package pw.fusionmine.fusionantirelog.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class MathUtils {

    public int millisToTicks(long millis) {
        return (int) Math.ceil(millis / 50.0);
    }

}
