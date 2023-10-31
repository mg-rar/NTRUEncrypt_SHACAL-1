package com.example.debilwillcry;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

public class FileHandler {
    public static Optional<List<byte[]>> read(String path) {
        try {
            byte[] name = path.getBytes();
            byte[] content = Files.readAllBytes(Paths.get(path));
            return Optional.of(List.of(name, content));
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    public static void write(String filepath, byte[] content, String name) {
        Path path = Paths.get(filepath);
        String[] file = {path.getParent().toString(),
                path.getFileName().toString().split("\\.")[0],
                path.getFileName().toString().split("\\.")[1]};

        try {
            Files.write(Paths.get(file[0] + "\\" + file[1] + name + "." + file[2]), content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int[] toIntArray(byte[] bytes) {
        IntBuffer intBuffer = ByteBuffer.wrap(bytes).asIntBuffer();
        int[] ints = new int[bytes.length / 4];
        for (int i = 0; i < ints.length; ++i)
            ints[i] = intBuffer.get(i);
        return ints;
    }

    public static byte[] toByteArray(int[] ints) {
        ByteBuffer bytes = ByteBuffer.allocate(ints.length * 4);
        for (int i = 0; i < ints.length; ++i)
            bytes.putInt(ints[i]);
        return bytes.array();
    }
}
