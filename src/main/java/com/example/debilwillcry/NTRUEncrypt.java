package com.example.debilwillcry;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

import static com.example.debilwillcry.PolynomialModQN.multiplication;
import static com.example.debilwillcry.PolynomialModQN.plus;


public class NTRUEncrypt {
    public static final int DEC_BLOCK_SIZE = 30;
    public static final int ENC_BLOCK_SIZE = 220;
    public static final int PUB_KEY_SIZE = 220;
    public PolynomialModQN h;
    private PolynomialModQN f;
    private PolynomialModQN g;
    private PolynomialModQN fQ;
    private PolynomialModQN fP;


    public NTRUEncrypt() {
        while (fQ == null || fP == null) {
            f = PolynomialModQN.smallPolynom(Constants.DF, Constants.DF - 1);
            g = PolynomialModQN.smallPolynom(Constants.DG, Constants.DG);
            PolynomialModQN f2 = new PolynomialModQN(f.rangeCoefficients().coef, Constants.P, Constants.N);

            fQ = f.inverse();
            fP = f2.inverse();
        }
        h = multiplication(multiplication(fQ, Constants.P), g);
    }

    public static int[] byteArrayToCoefficients(byte[] bytes) {
        int[] coef = new int[Constants.N];
        for (int i = 0; i < bytes.length * 8; i++)
            coef[i] = (bytes[i / 8] >> (i % 8)) & 1;
        coef[bytes.length * 8] = 1;
        return coef;
    }

    public static byte[] polynomialToByteArray(PolynomialModQN poly) {
        int bitCount = Integer.toBinaryString(Constants.Q - 1).length();
        byte[] res = new byte[poly.coefficients.length * bitCount / 8 + 1];
        for (int i = 0; i < poly.coefficients.length * bitCount; i++) {
            byte x = (byte) (((poly.coefficients[i / bitCount] >> (i % bitCount)) & 1) << (i % 8));
            res[i / 8] = (byte) (res[i / 8] | x);
        }
        return res;
    }

    public static byte[] coefficientsToByteArray(int[] array) {
        int index = array.length - 1;
        while (array[index] != 1)
            index--;
        if (index % 8 != 0)
            throw new RuntimeException("invalid array for conversion");
        byte[] bytes = new byte[index / 8];
        for (int i = 0; i < index; i++)
            bytes[i / 8] = (byte) (bytes[i / 8] & 0xff | (array[i] << (i % 8)));
        return bytes;
    }

    public static PolynomialModQN byteArrayToPolynomial(byte[] bytes) {
        int bitCount = Integer.toBinaryString(Constants.Q - 1).length();
        int[] ints = new int[bytes.length * 8 / bitCount];
        for (int i = 0; i < ints.length * bitCount; i++) {
            int x = ((bytes[i / 8] >> (i % 8)) & 1) << (i % bitCount);
            ints[i / bitCount] = ints[i / bitCount] | x;
        }
        return new PolynomialModQN(ints, Constants.Q, Constants.N);
    }

    public static void sendSymmetricKey(DataInputStream in, DataOutputStream out, byte[] key) throws IOException {
        NTRUEncrypt ntru = new NTRUEncrypt();
        ntru.h = byteArrayToPolynomial(in.readNBytes(PUB_KEY_SIZE));
        byte[] keyPadded = Mode.addPadding(key, DEC_BLOCK_SIZE);
        int blocksLen = keyPadded.length / DEC_BLOCK_SIZE;
        out.writeInt(blocksLen);
        for (int i = 0; i < blocksLen; ++i) {
            byte[] block = Arrays.copyOfRange(keyPadded, i * DEC_BLOCK_SIZE, (i + 1) * DEC_BLOCK_SIZE);
            out.write(ntru.encryption(block));
        }
    }

    public static byte[] receiveSymmetricKey(DataInputStream in, DataOutputStream out) throws IOException {
        NTRUEncrypt ntru = new NTRUEncrypt();
        out.write(polynomialToByteArray(ntru.h));
        int blocksLen = in.readInt();
        byte[] keyDec = new byte[blocksLen * DEC_BLOCK_SIZE];
        for (int i = 0; i < blocksLen; ++i) {
            byte[] blockDec = ntru.decryption(in.readNBytes(ENC_BLOCK_SIZE));
            System.arraycopy(blockDec, 0, keyDec, i * DEC_BLOCK_SIZE, DEC_BLOCK_SIZE);
        }
        return Mode.removePadding(keyDec);
    }

    public byte[] encryption(byte[] bytes) {
        if (h == null)
            throw new RuntimeException("no key provided");
        PolynomialModQN r = PolynomialModQN.smallPolynom(Constants.DR, Constants.DR);
        PolynomialModQN m = new PolynomialModQN(
                byteArrayToCoefficients(bytes), Constants.Q, Constants.N);
        PolynomialModQN e = plus(multiplication(r, h), m);
        return polynomialToByteArray(e);
    }

    public byte[] decryption(byte[] bytes) {
        if (f == null || fP == null)
            throw new RuntimeException("no decryption key available");
        PolynomialModQN e = byteArrayToPolynomial(bytes);
        PolynomialModQN a = multiplication(f, e);
        PolynomialModQN newA = new PolynomialModQN(a.rangeCoefficients().coef, fP.q, fP.degree + 1);
        PolynomialModQN m = multiplication(fP, newA);
        return coefficientsToByteArray(m.rangeCoefficients().coef);
    }
}