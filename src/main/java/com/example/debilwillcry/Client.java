package com.example.debilwillcry;

import org.apache.log4j.Logger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class Client {
    private static final Logger logger = Logger.getLogger(Client.class.getName());
    private final Socket server;
    private DataOutputStream outStream;
    private DataInputStream inStream;


    public Client(String host, Integer port) throws IOException {
        try {
            server = new Socket(host, port);
            this.inStream = new DataInputStream(server.getInputStream());
            this.outStream = new DataOutputStream(server.getOutputStream());
            logger.info("Connection established");
        } catch (IOException e) {
            logger.error("Failed to connect to the server", e);
            throw e;
        }
    }


    public static byte[] symmetricEncrypt(byte[] data, int[] key, String mode) {
        switch (mode) {
            case "ECB" -> {
                return Mode.encryptECB(data, key);
            }
            case "CBC" -> {
                return Mode.encryptCBC(data, key);
            }
            case "CFB" -> {
                return Mode.encryptCFB(data, key);
            }
            case "CTR" -> {
                return Mode.encryptCTR(data, key);
            }
            case "OFB" -> {
                return Mode.encryptOFB(data, key);
            }
            case "RD" -> {
                return Mode.encryptRD(data, key);
            }
            case "RDH" -> {
                return Mode.encryptRDH(data, key);
            }
            default -> {
                return new byte[]{};
            }
        }
    }


    public static byte[] symmetricDecrypt(String filePath, String keyPath, String mode) throws IOException {
        byte[] data = Files.readAllBytes(Path.of(filePath));
        BigInteger bigInteger = new BigInteger(Files.readAllBytes(Path.of(keyPath)));
        int[] key = SHACAL1.keyExtension(bigInteger);
        switch (mode) {
            case "ECB" -> {
                return Mode.decryptECB(data, key);
            }
            case "CBC" -> {
                return Mode.decryptCBC(data, key);
            }
            case "CFB" -> {
                return Mode.decryptCFB(data, key);
            }
            case "CTR" -> {
                return Mode.decryptCTR(data, key);
            }
            case "OFB" -> {
                return Mode.decryptOFB(data, key);
            }
            case "RD" -> {
                return Mode.decryptRD(data, key);
            }
            case "RDH" -> {
                return Mode.decryptRDH(data, key);
            }
            default -> {
                return new byte[]{};
            }
        }
    }


    public void uploadFile(String fileName, String mode) throws IOException {
        /* Протокол при скачивании клиентом файла:
         * 1. (S -> C) String - публичный ключ NTRU
         * 2. (C -> S) byte[80] - зашифрованный ключ SHACAL-1
         * 3. (C -> S) int n + byte[n] - зашифрованный файл
         * */
        logger.info("requesting upload");
        Path path = Path.of(fileName);
        outStream.writeUTF("upload " + path.getFileName());
        byte[] key = Arrays.copyOfRange(SHACAL1.keyGenerator(8).toByteArray(), 0, 64);
        NTRUEncrypt.sendSymmetricKey(inStream, outStream, key);
        int[] keys = SHACAL1.keyExtension(new BigInteger(key));
        sendFile(symmetricEncrypt(Files.readAllBytes(path), keys, mode));
    }


    public void downloadFile(String fileName, String path) throws IOException {
        /* Протокол при скачивании клиентом файла:
         * 1. (C -> S) String - публичный ключ NTRU
         * 2. (S -> C) byte[80] - зашифрованный ключ SHACAL-1
         * 3. (S -> C) int n + byte[n] - зашифрованный файл
         * Завершение коммуникаций
         * */
        logger.info("requesting download");
        Path path1 = Path.of(path);
        outStream.writeUTF("download " + fileName);
        byte[] key = NTRUEncrypt.receiveSymmetricKey(inStream, outStream);
        Files.write(path1.resolve(fileName + "_key"), key);
        byte[] file = receiveFile();
        Files.write(path1.resolve(fileName), file);
    }


    public void deleteFile(String fileName) throws IOException {
        /* Протокол при удалении файла:
         * 1. (S -> C) "success" / "fail"
         * Завершение коммуникаций
         * */
        outStream.writeUTF("delete " + fileName);
        inStream.readUTF();
    }


    public List<String> getFileList() throws IOException {
        /* Протокол при получении files:
         * 1. (S -> C) "success" / "fail"
         * 1.1 "fail" -> завершение коммуникаций обеими сторонами
         * 1.2 "success" -> продолжение коммуникаций
         * 2. (S -> C) int n- количество файлов
         * 3. (S -> C) String[n] - имена файлов
         * Завершение коммуникаций
         * */
        logger.info("requesting file list");
        outStream.writeUTF("files");
        List<String> files = new ArrayList<>();
        if (inStream.readUTF().equals("success")) {
            int count = inStream.readInt();
            for (int i = 0; i < count; ++i)
                files.add(inStream.readUTF());
        }
        files.removeIf(file -> file.contains("_key"));
        return files;
    }


    public void sendFile(byte[] file) throws IOException {
        outStream.writeInt(file.length);
        outStream.write(file);
    }


    public byte[] receiveFile() throws IOException {
        byte[] file = new byte[inStream.readInt()];
        inStream.readFully(file);
        return file;
    }
}