package com.yf.serialize.kyro;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.yf.remoting.dto.RpcRequest;
import com.yf.remoting.dto.RpcResponse;
import com.yf.serialize.Serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * @description: TODO
 * @author: Fannsy
 * @date: 2022/9/2 14:50
 * @version: 1.0.0
 * @url:
 */
public class KyroSerializer implements Serializer {
    private final ThreadLocal<Kryo> kryoThreadLocal = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();
        kryo.register(RpcResponse.class);
        kryo.register(RpcRequest.class);
        return kryo;
    });

    @Override
    public byte[] serialize(Object object) {
        // 获取输出流
        Output output = new Output(new ByteArrayOutputStream());
        Kryo kryo = kryoThreadLocal.get();
        // 对象序列化
        kryo.writeObject(output,object);
        kryoThreadLocal.remove();
        return output.toBytes();
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        Input input = new Input(new ByteArrayInputStream(bytes));
        Kryo kryo = kryoThreadLocal.get();
        Object o = kryo.readObject(input,clazz);
        kryoThreadLocal.remove();
        return clazz.cast(o);
    }
}
