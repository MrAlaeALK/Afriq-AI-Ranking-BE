package com.pfa.pfaproject.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Utils {
    public static Double round(Double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
