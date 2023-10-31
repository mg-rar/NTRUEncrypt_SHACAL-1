package com.example.debilwillcry;

public class basePolynom {
    public int[] coef;
    private int n;

    public basePolynom(int[] a, int count) {
        if (a.length < count)
            System.out.println("Invalid length of coefficients");

        n = count;
        this.coef = a;
    }
}
