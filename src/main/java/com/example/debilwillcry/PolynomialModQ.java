package com.example.debilwillcry;

import java.util.Arrays;

public class PolynomialModQ {
    public int[] coefficients;
    public int degree;
    public int q; //mod a

    public PolynomialModQ() {
    }

    public PolynomialModQ(int[] a, int mod) {
        int i = a.length - 1;
        while (a[i] % mod == 0 && i > 0)
            i--;

        coefficients = new int[i + 1];
        degree = i;
        q = mod;

        for (i = 0; i < coefficients.length; ++i)
            coefficients[i] = (a[i] + mod) % mod;
    }


    public static PolynomialModQ plus(PolynomialModQ polynom1, PolynomialModQ polynom2) {
        if (polynom1.q != polynom2.q)
            System.out.println("mods are unequal");

        int[] newPolynom = new int[Math.max(polynom1.coefficients.length, polynom2.coefficients.length)];

        if (polynom1.degree > polynom2.degree) {
            System.arraycopy(polynom1.coefficients, 0, newPolynom, 0, polynom1.coefficients.length);

            for (int i = 0; i < polynom2.coefficients.length; i++)
                newPolynom[i] += polynom2.coefficients[i];
        } else {
            System.arraycopy(polynom2.coefficients, 0, newPolynom, 0, polynom2.coefficients.length);

            for (int i = 0; i < polynom1.coefficients.length; i++)
                newPolynom[i] += polynom1.coefficients[i];
        }
        return new PolynomialModQ(newPolynom, polynom1.q);
    }

    public static boolean equals(PolynomialModQ polynom1, PolynomialModQ polynom2) {
        return Arrays.equals(polynom1.coefficients, polynom2.coefficients);
    }

    public static boolean notEquals(PolynomialModQ polynom1, PolynomialModQ polynom2) {
        return !equals(polynom1, polynom2);
    }

    public static PolynomialModQ minus(PolynomialModQ polynom) {
        int[] newPolynom = new int[polynom.coefficients.length];

        for (int i = 0; i < newPolynom.length; ++i)
            newPolynom[i] = -polynom.coefficients[i];

        return new PolynomialModQ(newPolynom, polynom.q);
    }

    public static PolynomialModQ minus(PolynomialModQ polynom1, PolynomialModQ polynom2) {
        return plus(polynom1, minus(polynom2));
    }

    public static PolynomialModQ multiplication(PolynomialModQ polynom1, PolynomialModQ polynom2) {
        if (polynom1.q != polynom2.q)
            System.out.println("mods arent equal");

        int[] newPolynom = new int[polynom1.coefficients.length + polynom2.coefficients.length];

        for (int i = 0; i < polynom1.coefficients.length; i++)
            for (int j = 0; j < polynom2.coefficients.length; j++)
                newPolynom[i + j] += polynom1.coefficients[i] * polynom2.coefficients[j];

        return new PolynomialModQ(newPolynom, polynom1.q);
    }

    public static PolynomialModQ multiplication(PolynomialModQ polynom1, int polynom2) {
        int[] newPolynom = new int[polynom1.coefficients.length];

        for (int i = 0; i < polynom1.coefficients.length; i++)
            newPolynom[i] += polynom1.coefficients[i] * polynom2;

        return new PolynomialModQ(newPolynom, polynom1.q);
    }

    public basePolynom rangeCoefficients() {
        int[] newPolynom = new int[coefficients.length];

        for (int i = 0; i < newPolynom.length; i++) {
            if (coefficients[i] > q / 2.0)
                newPolynom[i] = coefficients[i] - q;
            else
                newPolynom[i] = coefficients[i];
        }
        return new basePolynom(newPolynom, newPolynom.length);
    }


}
