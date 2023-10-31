package com.example.debilwillcry;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

class ApplicationTest {
    static final String testFile = "C://Users//homyaveli//Desktop//test//test.txt";
    static final String outputFile = "C://Users//homyaveli//Desktop//test//test_output.txt";
    int[] primes = {11, 19, 29, 37, 43, 47, 59, 73, 89, 97};
    int[] odds = {8, 9, 16, 55, 77, 100, 102, 121, 169, 200};
    int iterations = 5;

    @Test
    void fermatTest() {
        for (int i = 0; i < primes.length; ++i) {
            Assertions.assertTrue(FermatTest.isPrime(BigInteger.valueOf(primes[i]), iterations));
            Assertions.assertFalse(FermatTest.isPrime(BigInteger.valueOf(odds[i]), iterations));
        }
    }

    @Test
    void millerRabinTest() {
        for (int i = 0; i < primes.length; ++i) {
            Assertions.assertTrue(MillerRabinTest.isPrime(BigInteger.valueOf(primes[i]), iterations));
            Assertions.assertFalse(MillerRabinTest.isPrime(BigInteger.valueOf(odds[i]), iterations));
        }
    }

    @Test
    void solovayStrassenTest() {
        for (int i = 0; i < primes.length; ++i) {
            Assertions.assertTrue(SolovayStrassenTest.isPrime(BigInteger.valueOf(primes[i]), iterations));
            Assertions.assertFalse(SolovayStrassenTest.isPrime(BigInteger.valueOf(odds[i]), iterations));
        }
    }


    @Test
    void SHACAL1_MODES_test() {
        SHACAL1_ECB_test();
        SHACAL1_CBC_test();
        SHACAL1_CFB_test();
        SHACAL1_OFB_test();
        SHACAL1_CTR_test();
        SHACAL1_RD_test();
        SHACAL1_RDH_test();
    }

    @Test
    void SHACAL1_ECB_test() {
        BigInteger key = SHACAL1.keyGenerator(4);
        var keys = SHACAL1.keyExtension(key);
        var rng = new Random();
        var data = new byte[1024 * 1024];
        for (int i = 0; i < data.length; ++i) {
            data[i] = (byte) rng.nextInt();
        }
        var new_data = Mode.decryptECB(Mode.encryptECB(data.clone(), keys), keys);
        assert Arrays.equals(data, new_data);
    }

    @Test
    void SHACAL1_CBC_test() {
        BigInteger key = SHACAL1.keyGenerator(4);
        var keys = SHACAL1.keyExtension(key);
        var rng = new Random();
        var data = new byte[1024 * 1024];
        for (int i = 0; i < data.length; ++i) {
            data[i] = (byte) rng.nextInt();
        }
        var new_data = Mode.decryptCBC(Mode.encryptCBC(data.clone(), keys), keys);
        assert Arrays.equals(data, new_data);
    }

    @Test
    void SHACAL1_CFB_test() {
        BigInteger key = SHACAL1.keyGenerator(4);
        var keys = SHACAL1.keyExtension(key);
        var rng = new Random();
        var data = new byte[1024 * 1024];
        for (int i = 0; i < data.length; ++i) {
            data[i] = (byte) rng.nextInt();
        }
        var new_data = Mode.decryptCFB(Mode.encryptCFB(data.clone(), keys), keys);
        assert Arrays.equals(data, new_data);
    }

    @Test
    void SHACAL1_OFB_test() {
        BigInteger key = SHACAL1.keyGenerator(4);
        var keys = SHACAL1.keyExtension(key);
        var rng = new Random();
        var data = new byte[1024 * 1024];
        for (int i = 0; i < data.length; ++i) {
            data[i] = (byte) rng.nextInt();
        }
        var new_data = Mode.decryptOFB(Mode.encryptOFB(data.clone(), keys), keys);
        assert Arrays.equals(data, new_data);
    }

    @Test
    void SHACAL1_CTR_test() {
        BigInteger key = SHACAL1.keyGenerator(4);
        var keys = SHACAL1.keyExtension(key);
        var rng = new Random();
        var data = new byte[1024 * 1024];

        for (int i = 0; i < data.length; ++i) {
            data[i] = (byte) rng.nextInt();
        }

        var new_data = Mode.decryptCTR(Mode.encryptCTR(data.clone(), keys), keys);
        assert Arrays.equals(data, new_data);
    }

    @Test
    void SHACAL1_RD_test() {
        BigInteger key = SHACAL1.keyGenerator(4);
        var keys = SHACAL1.keyExtension(key);
        var rng = new Random();
        var data = new byte[1024 * 1024];

        for (int i = 0; i < data.length; ++i) {
            data[i] = (byte) rng.nextInt();
        }

        var new_data = Mode.decryptRD(Mode.encryptRD(data.clone(), keys), keys);
        assert Arrays.equals(data, new_data);
    }

    @Test
    void SHACAL1_RDH_test() {
        BigInteger key = SHACAL1.keyGenerator(4);
        var keys = SHACAL1.keyExtension(key);
        var rng = new Random();
        var data = new byte[1024 * 1024];

        for (int i = 0; i < data.length; ++i) {
            data[i] = (byte) rng.nextInt();
        }

        var new_data = Mode.decryptRDH(Mode.encryptRDH(data.clone(), keys), keys);
        assert Arrays.equals(data, new_data);
    }

    @Test
    void NTRUEncryptTest() {
        var rng = new Random();
        var data = new byte[64];
        for (int i = 0; i < data.length; ++i)
            data[i] = (byte) rng.nextInt();

        NTRUEncrypt recv = new NTRUEncrypt();
        byte[] pubKey = NTRUEncrypt.polynomialToByteArray(recv.h);

        NTRUEncrypt ntru = new NTRUEncrypt();
        ntru.h = NTRUEncrypt.byteArrayToPolynomial(pubKey);

        byte[] keyPadded = Mode.addPadding(data, 30);
        List<Byte> keyEnc = new ArrayList<>();
        int blocksLen = keyPadded.length / 30;
        for (int i = 0; i < blocksLen; ++i) {
            byte[] block = Arrays.copyOfRange(keyPadded, i * 30, (i + 1) * 30);
            for (byte b : ntru.encryption(block))
                keyEnc.add(b);
        }
        byte[] keyEncT = new byte[keyEnc.size()];
        for (int i = 0; i < keyEnc.size(); i++) {
            keyEncT[i] = keyEnc.get(i);
        }

        List<Byte> keyDec = new ArrayList<>();
        for (int i = 0; i < blocksLen; ++i) {
            byte[] block = Arrays.copyOfRange(keyEncT, i * 30, (i + 1) * 30);
            System.arraycopy(ntru.decryption(block), 0, keyDec, i * 30, 30);
        }
        byte[] keyDecT = new byte[keyDec.size()];
        for (int i = 0; i < keyDec.size(); i++) {
            keyDecT[i] = keyDec.get(i);
        }

        assert Arrays.equals(data, Mode.removePadding(keyDecT));
    }
}