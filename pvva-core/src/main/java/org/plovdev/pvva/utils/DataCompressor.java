package org.plovdev.pvva.utils;

import org.jspecify.annotations.NonNull;

import java.io.ByteArrayOutputStream;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

@SuppressWarnings("resource")
public final class DataCompressor {
    public static byte @NonNull [] compress(byte[] src) {
        Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION);
        deflater.setInput(src);
        deflater.finish();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[512];
        while (!deflater.finished()) {
            int compressedSize = deflater.deflate(buffer);
            outputStream.write(buffer, 0, compressedSize);
        }

        deflater.end();
        return outputStream.toByteArray();
    }

    public static byte @NonNull [] decompress(byte[] src) {
        try {
            Inflater inflater = new Inflater();
            inflater.setInput(src);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[512];
            while (!inflater.finished()) {
                int compressedSize = inflater.inflate(buffer);
                outputStream.write(buffer, 0, compressedSize);
            }

            inflater.end();
            return outputStream.toByteArray();
        } catch (DataFormatException e) {
            throw new IllegalArgumentException("Input data contains illegal(not compressed dy deflater) format");
        }
    }
}