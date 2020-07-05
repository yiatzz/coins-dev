package br.com.yiatzz.coins.spigot.util;

import java.util.OptionalDouble;

public class OptionalNumber {

    public static OptionalDouble tryParseDouble(String value) {
        try {
            double i = Double.parseDouble(value);
            return OptionalDouble.of(i);
        } catch (NumberFormatException e) {
            return OptionalDouble.empty();
        }
    }
}
