package com.example.debilwillcry;

import org.apache.log4j.Logger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public class ClientHandler implements Runnable {
    private static final Logger logger = Logger.getLogger(ClientHandler.class.getName());
    private final Socket client;
    private final Server server;
    private final DataOutputStream outStream;
    private final DataInputStream inStream;
    private final Path dir;


    public ClientHandler(Socket client, Server server) throws IOException {
        this.client = client;
        this.server = server;
        this.inStream = new DataInputStream(client.getInputStream());
        this.outStream = new DataOutputStream(client.getOutputStream());
        this.dir = Path.of(server.dir);
        logger.info("Created");
    }

    @Override
    public void run() {
        logger.info("Started");
        try {
            while (true) {
                String[] args = inStream.readUTF().split(" ", 2);
                logger.info("Received client request: " + args[0]);
                switch (args[0]) {
                    case "upload":
                        uploadFile(args[1]);
                        break;
                    case "download":
                        downloadFile(args[1]);
                        break;
                    case "files":
                        getFileList();
                        break;
                    case "delete":
                        deleteFile(args[1]);
                        break;
                    default:
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
        } finally {
            logger.info("Finishing");
            try {
                inStream.close();
            } catch (IOException ignored) { /**/ }
            try {
                outStream.close();
            } catch (IOException ignored) { /**/ }
            try {
                client.close();
            } catch (IOException ignored) { /**/ }
        }
    }


    private void uploadFile(String fileName) throws IOException {
        /* Протокол при скачивании клиентом файла:
         * 1. (S -> C) String - публичный ключ NTRU
         * 2. (C -> S) byte[80] - зашифрованный ключ SHACAL-1
         * 3. (C -> S) int n + byte[n] - зашифрованный файл
         * */

        byte[] key = NTRUEncrypt.receiveSymmetricKey(inStream, outStream);
        Files.write(dir.resolve(fileName + "_key"), key);
        byte[] file = receiveFile();
        Files.write(dir.resolve(fileName), file);
    }

    private void downloadFile(String fileName) throws IOException {
        /* Протокол при скачивании клиентом файла:
         * 1. (C -> S) String - публичный ключ NTRU
         * 2. (S -> C) byte[90] - зашифрованный ключ SHACAL-1
         * 3. (S -> C) int n + byte[n] - зашифрованный файл
         * Завершение коммуникаций
         * */

        byte[] key = Files.readAllBytes(dir.resolve(fileName + "_key"));
        NTRUEncrypt.sendSymmetricKey(inStream, outStream, key);
        byte[] file = Files.readAllBytes(dir.resolve(fileName));
        sendFile(file);
    }

    private void deleteFile(String filename) throws IOException {
        /* Протокол при удалении файла:
         * 1. (S -> C) "success" / "fail"
         * Завершение коммуникаций
         * */

        try {
            Files.deleteIfExists(Path.of(server.dir + "//" + filename));
            Files.deleteIfExists(Path.of(server.dir + "//" + filename + "_key"));
            outStream.writeUTF("success");
        } catch (IOException e) {
            outStream.writeUTF("fail");
        }
    }

    private void getFileList() throws IOException {
        /* Протокол при получении files:
         * 1. (S -> C) "success" / "fail"
         * 1.1 "fail" -> завершение коммуникаций обеими сторонами
         * 1.2 "success" -> продолжение коммуникаций
         * 2. (S -> C) int n- количество файлов
         * 3. (S -> C) String[n] - имена файлов
         * Завершение коммуникаций
         * */

        File[] files = new File(server.dir).listFiles();
        if (files == null) {
            outStream.writeUTF("fail");
            return;
        } else {
            outStream.writeUTF("success");
        }

        List<String> fileList = Stream.of(files)
                .filter(File::isFile)
                .map(File::getName)
                .toList();
        outStream.writeInt(fileList.size());
        for (String file : fileList) {
            outStream.writeUTF(file);
        }
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
