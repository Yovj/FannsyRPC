package com.yf.compress.gzip;

import com.yf.compress.Compress;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @description: TODO
 * @author: Fannsy
 * @date: 2022/9/3 8:56
 * @version: 1.0.0
 * @url:
 */
public class GzipCompress implements Compress {
    private static final int BUFFER_SIZE = 1024 * 4;


    @Override
    public byte[] compress(byte[] bytes) {
        checkNull(bytes);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        GZIPOutputStream gzip = null;
        try {
            gzip = new GZIPOutputStream(byteArrayOutputStream);
            gzip.write(bytes);
            gzip.flush();
            gzip.finish();
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("gzip compress error",e);
        }
    }

    @Override
    public byte[] decompress(byte[] bytes) {
        checkNull(bytes);
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            GZIPInputStream gunzip = new GZIPInputStream(new ByteArrayInputStream(bytes));
            byte[] buffer = new byte[BUFFER_SIZE];
            int n;
            while ( (n = gunzip.read(buffer)) > -1){
                byteArrayOutputStream.write(buffer,0,n);
            }
            return byteArrayOutputStream.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("gzip decompress error",e);
        }

    }

    private void checkNull(byte[] bytes){
        if (bytes == null){
            throw new NullPointerException("bytes is null");
        }
    }

}
