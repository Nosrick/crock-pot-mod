package com.github.nosrick.crockpot.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public abstract class MathUtil {
    public static float sigFig(float value, int figures) {
        return new BigDecimal(value).setScale(figures, RoundingMode.HALF_UP).floatValue();
    }

    public static int goodRounding(float value, int figures) {
        return new BigDecimal(value).setScale(figures, RoundingMode.HALF_UP).intValue();
    }
}
