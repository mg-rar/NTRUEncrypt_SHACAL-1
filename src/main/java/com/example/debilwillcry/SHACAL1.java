package com.example.debilwillcry;

import java.math.BigInteger;
import java.util.Random;

public class SHACAL1 {
    public static final int SHACAL1_KEY_SIZE = 512;
    private static final int[] M = {0x5A827999, 0x6ED9EBA1, 0x8F1BBCDC, 0xCA62C1D6};

    private static int func1(int x, int y, int z) {
        return (x & y) | (~x & z);
    }

    private static int func2(int x, int y, int z) {
        return x ^ y ^ z;
    }

    private static int func3(int x, int y, int z) {
        return (x & y) | (x & z) | (y & z);
    }

    private static int shiftLeft(int value, int n) {

        return (value << n) | (value >>> (32 - n));
    }

    private static int shiftRight(int value, int n) {

        return (value >>> n) | (value << (32 - n));
    }

    public static BigInteger keyGenerator(int iterations) {
        BigInteger key = BigInteger.ZERO;

        boolean prime = false;
        while (!prime) {
            key = BigInteger.probablePrime(SHACAL1_KEY_SIZE, new Random());
            prime = FermatTest.isPrime(key, iterations) &&
                    MillerRabinTest.isPrime(key, iterations) &&
                    SolovayStrassenTest.isPrime(key, iterations);
        }
        return key;
    }

    public static int[] keyExtension(BigInteger k) {
        int[] key = new int[80];

        //split initial key
        for (int i = 0; i < 16; ++i) {
            key[i] = k.and(BigInteger.valueOf(0xFFFFFFFFL)).intValue();
            k = k.shiftRight(32);
        }
        //generate round keys
        for (int i = 16; i < 80; ++i)
            key[i] = shiftLeft(key[i - 3] ^ key[i - 8] ^ key[i - 14] ^ key[i - 16], 1);

        return key;
    }

    public static int[] encrypt(int[] m, int[] k) {
        for (int i = 0; i < 80; ++i) {
            int[] t = m.clone();

            if (i < 20)
                m[0] = k[i] + (shiftLeft(t[0], 5)) + func1(t[1], t[2], t[3]) + t[4] + (int) (M[0] % (long) Math.pow(2, 32));
            else if (i < 40)
                m[0] = k[i] + (shiftLeft(t[0], 5)) + func2(t[1], t[2], t[3]) + t[4] + (int) (M[1] % (long) Math.pow(2, 32));
            else if (i < 60)
                m[0] = k[i] + (shiftLeft(t[0], 5)) + func3(t[1], t[2], t[3]) + t[4] + (int) (M[2] % (long) Math.pow(2, 32));
            else
                m[0] = k[i] + (shiftLeft(t[0], 5)) + func2(t[1], t[2], t[3]) + t[4] + (int) (M[3] % (long) Math.pow(2, 32));

            m[1] = t[0];
            m[2] = shiftLeft(t[1], 30);
            m[3] = t[2];
            m[4] = t[3];
        }
        return m;
    }

    public static int[] decrypt(int[] c, int[] k) {
        for (int i = 79; i >= 0; --i) {
            int[] t = c.clone();

            c[0] = t[1];
            c[1] = shiftRight(t[2], 30);
            c[2] = t[3];
            c[3] = t[4];

            if (i < 20)
                c[4] = t[0] - (k[i] + (shiftLeft(c[0], 5)) + func1(c[1], c[2], c[3]) + (int) (M[0] % (long) Math.pow(2, 32)));
            else if (i < 40)
                c[4] = t[0] - (k[i] + (shiftLeft(c[0], 5)) + func2(c[1], c[2], c[3]) + (int) (M[1] % (long) Math.pow(2, 32)));
            else if (i < 60)
                c[4] = t[0] - (k[i] + (shiftLeft(c[0], 5)) + func3(c[1], c[2], c[3]) + (int) (M[2] % (long) Math.pow(2, 32)));
            else
                c[4] = t[0] - (k[i] + (shiftLeft(c[0], 5)) + func2(c[1], c[2], c[3]) + (int) (M[3] % (long) Math.pow(2, 32)));
        }
        return c;
    }
}
