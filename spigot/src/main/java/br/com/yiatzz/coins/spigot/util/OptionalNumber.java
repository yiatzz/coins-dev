package br.com.yiatzz.coins.spigot.util;

import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

public class OptionalNumber {

    public static OptionalDouble tryParseDouble(String value) {
        try {
            double i = Double.parseDouble(value);
            return OptionalDouble.of(i);
        } catch (NumberFormatException e) {
            return OptionalDouble.empty();
        }
    }

    public static OptionalInt tryParseInt(String value) {
        try {
            int i = Integer.parseInt(value);
            return OptionalInt.of(i);
        } catch (NumberFormatException e) {
            return OptionalInt.empty();
        }
    }

    public static OptionalLong tryParseLong(String value) {
        try {
            long i = Long.parseLong(value);
            return OptionalLong.of(i);
        } catch (NumberFormatException e) {
            return OptionalLong.empty();
        }
    }
}
