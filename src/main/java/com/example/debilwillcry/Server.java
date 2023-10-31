package com.example.debilwillcry;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;


public class Server {
    private static final Logger logger = Logger.getLogger(Server.class.getName());
    final String dir;
    private final String host;
    private final Integer port;


    public Server(String host, Integer port, String dir) {
        this.host = host;
        this.port = port;
        this.dir = dir;
    }

    public static void main(String[] args) {
        BasicConfigurator.configure();
        new Server("127.0.0.1", 42069, "C:\\Users\\homyaveli\\Desktop\\test\\storage").start();
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port, 64, InetAddress.getByName(host))) {
            Files.createDirectories(Path.of(dir));
            logger.info("server started" + serverSocket);
            while (true) {
                Socket client = serverSocket.accept();
                logger.info("new client: " + client.toString());
                ClientHandler clientHandler = new ClientHandler(client, this);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            logger.error("problem with the server", e);
        }
    }
}
