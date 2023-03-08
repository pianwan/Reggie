package com.example.reggie.util;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class ValidateCodeUtils {
    private static final Random random = ThreadLocalRandom.current();

    public static Integer generateCode(int length) {
        return random.nextInt(((int) Math.pow(10, length)));
    }
}
