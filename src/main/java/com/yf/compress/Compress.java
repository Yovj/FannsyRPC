package com.yf.compress;

import com.yf.common.extension.SPI;

import java.io.IOException;
@SPI
public interface Compress {

    byte[] compress(byte[] bytes) throws IOException;

    byte[] decompress(byte[] bytes);
}
