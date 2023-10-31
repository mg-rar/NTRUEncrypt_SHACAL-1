package com.example.debilwillcry;

import java.math.BigInteger;
import java.util.Random;

public class FermatTest {

    public static boolean isPrime(BigInteger n, int k) {
        if (n.equals(BigInteger.ONE)) {
            return false;
        }

        if (n.equals(BigInteger.TWO)) {
            return true;
        }

        Random random = new Random();

        for (int i = 0; i < k; i++) {
            BigInteger a = new BigInteger(n.bitLength(), random);
            a = a.mod(n.subtract(BigInteger.TWO)).add(BigInteger.TWO);

            if (!(a.modPow(n.subtract(BigInteger.ONE), n).equals(BigInteger.ONE))) {
                return false;
            }
        }

        return true;
    }
}
