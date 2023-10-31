package com.example.debilwillcry;

import java.math.BigInteger;
import java.util.Random;

class SolovayStrassenTest {

    static int calculateJacobian(BigInteger a, BigInteger b) {
        //checking mutual simplicity
        if (a.gcd(b).compareTo(BigInteger.ONE) != 0)
            return 0;

        //initialization
        int r = 1;

        //conversion to positive
        if (a.compareTo(BigInteger.ZERO) < 0) {
            a.negate();
            if (b.mod(BigInteger.valueOf(4)).compareTo(BigInteger.valueOf(3)) == 0)
                r *= -1;
        }

        while (a.compareTo(BigInteger.ZERO) != 0) {
            //getting rid of parity
            int t = 0;
            while (a.mod(BigInteger.TWO).compareTo(BigInteger.ZERO) == 0) {
                t += 1;
                a = a.divide(BigInteger.TWO);
            }
            if (t % 2 != 0)
                if (b.mod(BigInteger.valueOf(8)).compareTo(BigInteger.valueOf(3)) == 0 ||
                        b.mod(BigInteger.valueOf(8)).compareTo(BigInteger.valueOf(5)) == 0)
                    r *= -1;

            //the quadratic law of reciprocity
            if (a.mod(BigInteger.valueOf(4)).compareTo(b.mod(BigInteger.valueOf(4))) == 0 &&
                    a.mod(BigInteger.valueOf(4)).compareTo(BigInteger.valueOf(3)) == 0)
                r *= -1;

            BigInteger c = a;
            a = b.mod(c);
            b = c;
        }
        return r;
    }

    // To perform the Solovay-Strassen Primality Test
    static boolean isPrime(BigInteger n, int k) {
        for (int i = 0; i < k; ++i) {
            if (n.compareTo(BigInteger.TWO) == -1)
                return false;
            if (n.compareTo(BigInteger.TWO) != 0 && n.mod(BigInteger.TWO).compareTo(BigInteger.ZERO) == 0)
                return false;

            // Create Object for Random Class
            BigInteger a = new BigInteger(n.bitLength(), new Random());
            a = a.mod(n.subtract(BigInteger.ONE)).add(BigInteger.ONE);

            if (n.gcd(a).compareTo(BigInteger.ONE) > 0)
                return false;

            int j = calculateJacobian(a, n);
            BigInteger t = a.modPow((n.subtract(BigInteger.ONE)).divide(BigInteger.TWO), n);
            if (t.compareTo(BigInteger.valueOf(j).mod(n)) != 0)
                return false;
        }
        return true;
    }
}