package com.example.debilwillcry;

import java.util.ArrayList;
import java.util.Random;

public class PolynomialModQN extends PolynomialModQ {
    private final int n;

    public PolynomialModQN(int[] a, int mod, int count) {
        if (count < a.length)
            System.out.println("wrong coeffs degree");

        coefficients = new int[count];
        n = count;
        degree = count - 1;
        q = mod;

        for (int i = 0; i < a.length; ++i)
            coefficients[i] = (a[i] + mod) % mod;
    }

    public static PolynomialModQN plus(PolynomialModQN polynom1, PolynomialModQN polynom2) {
        PolynomialModQ res = PolynomialModQ.plus(polynom1, polynom2);
        return new PolynomialModQN(res.coefficients, polynom1.q, polynom1.n);
    }

    public static PolynomialModQN minus(PolynomialModQN polynom1, PolynomialModQN polynom2) {
        PolynomialModQ res = PolynomialModQ.minus(polynom1, polynom2);
        return new PolynomialModQN(res.coefficients, polynom1.q, polynom1.n);
    }

    public static PolynomialModQN multiplication(PolynomialModQN polynom, int x) {
        int[] a = new int[polynom.n];
        for (int i = 0; i < polynom.n; ++i)
            a[i] = polynom.coefficients[i] * x;
        return new PolynomialModQN(a, polynom.q, polynom.n);
    }

    public static PolynomialModQN multiplication(PolynomialModQN polynom1, PolynomialModQN polynom2) {
        if (polynom1.n != polynom2.n)
            System.out.println("wrong degree n");

        int[] a = new int[polynom1.n];
        for (int i = 0; i < polynom1.n; ++i)
            for (int j = 0; j < polynom2.n; ++j)
                a[i] += polynom1.coefficients[j] * polynom2.coefficients[(polynom1.n + i - j) % polynom1.n];

        return new PolynomialModQN(a, polynom1.q, polynom1.n);
    }

    public static PolynomialModQN smallPolynom(int count, int countLess) {
        int[] newCoef = new int[Constants.N];
        Random rand = new Random();

        for (int i = 0; i < Constants.N; i++) {
            if (i < count)
                newCoef[i] = 1;
            else if (i < count + countLess)
                newCoef[i] = -1;
            else newCoef[i] = 0;
        }

        for (int i = Constants.N - 1; i >= 1; i--) {
            int j = rand.nextInt(i + 1);
            int tmp = newCoef[j];
            newCoef[j] = newCoef[i];
            newCoef[i] = tmp;
        }
        return new PolynomialModQN(newCoef, Constants.Q, Constants.N);
    }

    public void lower(PolynomialModQ balance, PolynomialModQ f, PolynomialModQ fMod, int invN) {

    }

    public PolynomialModQN inverse() {
        int range = 1000;
        int i = 0;
        ArrayList<PolynomialModQ> quotients = new ArrayList<>();

        if (q == Constants.Q)
            q = 2;
        int[] xNCoef = new int[n + 1];
        xNCoef[0] = -1;
        xNCoef[n] = 1;
        PolynomialModQ xN = new PolynomialModQ(xNCoef, q);
        PolynomialModQ balance = xN;
        PolynomialModQ f = new PolynomialModQ(coefficients, q);

        int[] fModCoef = new int[1];
        PolynomialModQ fMod = new PolynomialModQ(fModCoef, q);

        int invN = inverseIntMod(f.coefficients[f.coefficients.length - 1]);

        while (balance.degree >= f.degree && i < range) {
            int[] deltaNCoef = new int[balance.degree - f.degree + 1];
            deltaNCoef[deltaNCoef.length - 1] = balance.coefficients[balance.degree] * invN;
            PolynomialModQ deltaN = new PolynomialModQ(deltaNCoef, q);

            fMod = plus(fMod, deltaN);
            balance = minus(balance, multiplication(deltaN, f));
            i++;
        }
        quotients.add(fMod);

        while (notEquals(balance, new PolynomialModQ(new int[]{0}, q)) && i < range) {
            xN = f;
            f = balance;
            fMod = new PolynomialModQ(new int[n + 1], q);
            balance = xN;
            invN = inverseIntMod(f.coefficients[f.coefficients.length - 1]);

            while (balance.degree >= f.degree && notEquals(balance, new PolynomialModQ(new int[]{0}, q)) && i < range) {
                int[] deltaNCoef = new int[balance.degree - f.degree + 1];
                deltaNCoef[deltaNCoef.length - 1] = balance.coefficients[balance.degree] * invN;
                PolynomialModQ deltaN = new PolynomialModQ(deltaNCoef, q);

                fMod = plus(fMod, deltaN);
                balance = minus(balance, multiplication(deltaN, f));
                i++;
            }
            quotients.add(fMod);
            i++;
        }
        if (i >= range)
            System.out.println("too many iteraations");

        ArrayList<PolynomialModQ> x = new ArrayList<>();
        x.add(new PolynomialModQ(new int[]{0}, q));
        x.add(new PolynomialModQ(new int[]{1}, q));

        for (int j = 0; j < quotients.size(); j++)
            x.add(plus(multiplication(quotients.get(j), x.get(j + 1)), x.get(j)));

        if (q == 2) {
            int n = 2;
            q = Constants.Q;
            PolynomialModQN fInverse = new PolynomialModQN(x.get(x.size() - 2).coefficients, q, this.n);
            while (n <= Constants.Q) {
                fInverse = minus(multiplication(fInverse, 2), multiplication(multiplication(this, fInverse), fInverse));
                n *= 2;
            }
            return fInverse;
        }
        PolynomialModQN fInverse2 = new PolynomialModQN(x.get(x.size() - 2).coefficients, q, n);
        fInverse2 = minus(multiplication(fInverse2, q), multiplication(multiplication(this, fInverse2), fInverse2));
        return multiplication(fInverse2, 2);
    }

    private int inverseIntMod(int x) {
        for (int i = 1; i < q; i++)
            if ((x * i) % q == 1)
                return i;

        throw new ArithmeticException("inverse element doesnt exist");
    }


}
