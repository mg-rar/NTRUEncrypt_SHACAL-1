package com.example.debilwillcry;

import java.util.Arrays;
import java.util.Random;

import static com.example.debilwillcry.FileHandler.toByteArray;
import static com.example.debilwillcry.FileHandler.toIntArray;


public class Mode {
    private static final Random rng = new Random();
    private static final int BLOCK_SIZE = 20;


    public static byte[] addPadding(byte[] content, int size) {
        byte count = (byte) (size - content.length % size);
        byte[] padded = new byte[content.length + count];
        System.arraycopy(content, 0, padded, 0, content.length);
        for (int i = content.length; i < padded.length; ++i)
            padded[i] = count;
        return padded;
    }

    public static byte[] removePadding(byte[] padded) {
        int paddingLength = padded[padded.length - 1];
        byte[] content = new byte[padded.length - paddingLength];
        System.arraycopy(padded, 0, content, 0, content.length);
        return content;
    }


    public static byte[] encryptECB(byte[] data, int[] key) {
        var dataI = toIntArray(addPadding(data, BLOCK_SIZE));
        int blocksLen = dataI.length / 5;
        for (int i = 0; i < blocksLen; ++i) {
            var block = Arrays.copyOfRange(dataI, i * 5, (i + 1) * 5);
            System.arraycopy(SHACAL1.encrypt(block, key), 0, dataI, i * 5, 5);
        }
        return toByteArray(dataI);
    }


    public static byte[] decryptECB(byte[] data, int[] key) {
        var dataI = toIntArray(data);
        int blocksLen = dataI.length / 5;
        for (int i = 0; i < blocksLen; ++i) {
            var block = Arrays.copyOfRange(dataI, i * 5, (i + 1) * 5);
            System.arraycopy(SHACAL1.decrypt(block, key), 0, dataI, i * 5, 5);
        }
        return removePadding(toByteArray(dataI));
    }


    public static byte[] encryptCBC(byte[] data, int[] key) {
        var dataDec = toIntArray(addPadding(data, BLOCK_SIZE));
        int blocksLen = dataDec.length / 5;
        var dataEnc = new int[(blocksLen + 1) * 5];
        for (int i = 0; i < 5; ++i) {
            dataEnc[i] = rng.nextInt();
        }
        for (int i = 0; i < blocksLen; ++i) {
            var block = Arrays.copyOfRange(dataDec, i * 5, (i + 1) * 5);
            for (int j = 0; j < 5; ++j) {
                block[j] ^= dataEnc[i * 5 + j];
            }
            System.arraycopy(SHACAL1.encrypt(block, key), 0, dataEnc, (i + 1) * 5, 5);
        }
        return toByteArray(dataEnc);
    }


    public static byte[] decryptCBC(byte[] data, int[] key) {
        var dataEnc = toIntArray(data);
        int blocksLen = dataEnc.length / 5 - 1;
        var dataDec = new int[blocksLen * 5];
        for (int i = 0; i < blocksLen; ++i) {
            var block = Arrays.copyOfRange(dataEnc, (i + 1) * 5, (i + 2) * 5);
            SHACAL1.decrypt(block, key);
            for (int j = 0; j < 5; ++j) {
                dataDec[i * 5 + j] = block[j] ^ dataEnc[i * 5 + j];
            }
        }
        return removePadding(toByteArray(dataDec));
    }


    public static byte[] encryptCFB(byte[] data, int[] key) {
        var dataDec = toIntArray(addPadding(data, BLOCK_SIZE));
        int blocksLen = dataDec.length / 5;
        var dataEnc = new int[(blocksLen + 1) * 5];

        for (int i = 0; i < 5; ++i) {
            dataEnc[i] = rng.nextInt();
        }
        for (int i = 0; i < blocksLen; ++i) {
            var prevBlockEnc = SHACAL1.encrypt(
                    Arrays.copyOfRange(dataEnc, i * 5, (i + 1) * 5), key
            );
            for (int j = 0; j < 5; ++j) {
                dataEnc[(i + 1) * 5 + j] = dataDec[i * 5 + j] ^ prevBlockEnc[j];
            }
        }
        return toByteArray(dataEnc);
    }


    public static byte[] decryptCFB(byte[] data, int[] key) {
        var dataEnc = toIntArray(data);
        int blocksLen = dataEnc.length / 5 - 1;
        var dataDec = new int[blocksLen * 5];

        for (int i = 0; i < blocksLen; ++i) {
            var prevBlockEnc = SHACAL1.encrypt(
                    Arrays.copyOfRange(dataEnc, i * 5, (i + 1) * 5), key
            );
            for (int j = 0; j < 5; ++j) {
                dataDec[i * 5 + j] = dataEnc[(i + 1) * 5 + j] ^ prevBlockEnc[j];
            }
        }
        return removePadding(toByteArray(dataDec));
    }


    public static byte[] encryptOFB(byte[] data, int[] key) {
        var dataDec = toIntArray(addPadding(data, BLOCK_SIZE));
        int blocksLen = dataDec.length / 5;
        var dataEnc = new int[(blocksLen + 1) * 5];
        var iv = new int[5];

        for (int i = 0; i < 5; ++i) {
            iv[i] = rng.nextInt();
            dataEnc[i] = iv[i];
        }
        for (int i = 0; i < blocksLen; ++i) {
            SHACAL1.encrypt(iv, key);
            for (int j = 0; j < 5; ++j) {
                dataEnc[(i + 1) * 5 + j] = dataDec[i * 5 + j] ^ iv[j];
            }
        }
        return toByteArray(dataEnc);
    }


    public static byte[] decryptOFB(byte[] data, int[] key) {
        var dataEnc = toIntArray(data);
        var iv = Arrays.copyOfRange(dataEnc, 0, 5);
        dataEnc = Arrays.copyOfRange(dataEnc, 5, dataEnc.length);
        int blocksLen = dataEnc.length / 5;
        var dataDec = new int[blocksLen * 5];

        for (int i = 0; i < blocksLen; ++i) {
            SHACAL1.encrypt(iv, key);
            for (int j = 0; j < 5; ++j) {
                dataDec[i * 5 + j] = dataEnc[i * 5 + j] ^ iv[j];
            }
        }
        return removePadding(toByteArray(dataDec));
    }


    public static byte[] encryptCTR(byte[] data, int[] key) {
        var dataDec = toIntArray(addPadding(data, BLOCK_SIZE));
        int blocksLen = dataDec.length / 5;
        var dataEnc = new int[(blocksLen + 1) * 5];
        var iv = new int[5];
        for (int i = 0; i < 5; i++) {
            iv[i] = rng.nextInt();
            dataEnc[i] = iv[i];
        }
        for (int i = 0; i < blocksLen; i++) {
            int finalI = i;
            var ivCurrent = SHACAL1.encrypt(Arrays.stream(iv).map(e -> e ^ finalI).toArray(), key);
            for (int j = 0; j < 5; j++) {
                dataEnc[(i + 1) * 5 + j] = dataDec[i * 5 + j] ^ ivCurrent[j];
            }
        }
        return toByteArray(dataEnc);
    }


    public static byte[] decryptCTR(byte[] data, int[] key) {
        var dataEnc = toIntArray(data);
        var iv = Arrays.copyOfRange(dataEnc, 0, 5);
        dataEnc = Arrays.copyOfRange(dataEnc, 5, dataEnc.length);
        int blocksLen = dataEnc.length / 5;
        var dataDec = new int[blocksLen * 5];
        for (int i = 0; i < blocksLen; i++) {
            int finalI = i;
            var ivCurrent = SHACAL1.encrypt(Arrays.stream(iv).map(e -> e ^ finalI).toArray(), key);
            for (int j = 0; j < 5; j++) {
                dataDec[i * 5 + j] = dataEnc[i * 5 + j] ^ ivCurrent[j];
            }
        }
        return removePadding(toByteArray(dataDec));
    }


    public static byte[] encryptRD(byte[] data, int[] key) {
        var dataDec = toIntArray(addPadding(data, BLOCK_SIZE));
        int blocksLen = dataDec.length / 5;

        var iv = new int[5];
        for (int i = 0; i < 5; i++)
            iv[i] = rng.nextInt();
        int delta = iv[4] >> 16;
        var dataEnc = new int[(blocksLen + 1) * 5];
        System.arraycopy(SHACAL1.encrypt(iv.clone(), key), 0, dataEnc, 0, 5);

        for (int i = 0; i < blocksLen; i++) {
            int finalI = i;
            var ivCurrent = Arrays.stream(iv).map(e -> e + delta * finalI).toArray();
            var block = Arrays.copyOfRange(dataDec, i * 5, i * 5 + 5);
            for (int j = 0; j < 5; j++) {
                block[j] ^= ivCurrent[j];
            }
            System.arraycopy(SHACAL1.encrypt(block.clone(), key), 0, dataEnc, (i + 1) * 5, 5);
        }
        return toByteArray(dataEnc);
    }


    public static byte[] decryptRD(byte[] data, int[] key) {
        var dataEnc = toIntArray(data);
        var iv = SHACAL1.decrypt(Arrays.copyOfRange(dataEnc, 0, 5), key);

        dataEnc = Arrays.copyOfRange(dataEnc, 5, dataEnc.length);
        int blocksLen = dataEnc.length / 5;
        int delta = iv[4] >> 16;
        var dataDec = new int[blocksLen * 5];

        for (int i = 0; i < blocksLen; i++) {
            int finalI = i;
            var ivCurrent = Arrays.stream(iv).map(e -> e + delta * finalI).toArray();
            var block = SHACAL1.decrypt(
                    Arrays.copyOfRange(dataEnc, i * 5, (i + 1) * 5), key
            );
            for (int j = 0; j < 5; j++) {
                block[j] ^= ivCurrent[j];
            }
            System.arraycopy(block, 0, dataDec, i * 5, 5);
        }
        return removePadding(toByteArray(dataDec));
    }


    public static byte[] encryptRDH(byte[] data, int[] key) {
        var dataDec = toIntArray(addPadding(data, BLOCK_SIZE));
        int blocksLen = dataDec.length / 5;
        var dataEnc = new int[(blocksLen + 2) * 5];

        int hash = Arrays.hashCode(dataDec);
        var hv = new int[5];
        for (int i = 0; i < 5; ++i)
            hv[i] = hash;

        var iv = new int[5];
        for (int i = 0; i < 5; i++)
            iv[i] = rng.nextInt();
        int delta = iv[4] >> 16;

        System.arraycopy(SHACAL1.encrypt(iv.clone(), key), 0, dataEnc, 0, 5);
        System.arraycopy(SHACAL1.encrypt(hv.clone(), key), 0, dataEnc, 5, 5);

        for (int i = 0; i < blocksLen; i++) {
            int finalI = i;
            var ivCurrent = Arrays.stream(iv).map(e -> e + delta * finalI).toArray();
            var block = Arrays.copyOfRange(dataDec, i * 5, i * 5 + 5);
            for (int j = 0; j < 5; j++) {
                block[j] ^= ivCurrent[j];
            }
            System.arraycopy(SHACAL1.encrypt(block.clone(), key), 0, dataEnc, (i + 2) * 5, 5);
        }
        return toByteArray(dataEnc);
    }


    public static byte[] decryptRDH(byte[] data, int[] key) {
        var dataEnc = toIntArray(data);
        var iv = SHACAL1.decrypt(Arrays.copyOfRange(dataEnc, 0, 5), key);
        var hv = SHACAL1.decrypt(Arrays.copyOfRange(dataEnc, 5, 10), key);

        dataEnc = Arrays.copyOfRange(dataEnc, 10, dataEnc.length);
        int blocksLen = dataEnc.length / 5;
        int delta = iv[4] >> 16;
        var dataDec = new int[blocksLen * 5];

        for (int i = 0; i < blocksLen; i++) {
            int finalI = i;
            var ivCurrent = Arrays.stream(iv).map(e -> e + delta * finalI).toArray();
            var block = SHACAL1.decrypt(
                    Arrays.copyOfRange(dataEnc, i * 5, (i + 1) * 5), key
            );
            for (int j = 0; j < 5; j++) {
                block[j] ^= ivCurrent[j];
            }
            System.arraycopy(block, 0, dataDec, i * 5, 5);
        }
        int hashNew = Arrays.hashCode(dataDec);
        assert hashNew == hv[0];
        return removePadding(toByteArray(dataDec));
    }

}
