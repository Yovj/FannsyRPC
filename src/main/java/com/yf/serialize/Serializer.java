package com.yf.serialize;

import com.yf.common.extension.SPI;

@SPI
public interface Serializer {

    public byte[] serialize(Object object);

    public <T>T deserialize(byte[] bytes, Class<T> clazz);

}
