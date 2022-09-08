package com.yf.transport.codec;

import com.yf.common.enums.CompressTypeEnum;
import com.yf.common.enums.SerializationTypeEnum;
import com.yf.common.extension.ExtensionLoader;
import com.yf.compress.Compress;
import com.yf.compress.gzip.GzipCompress;
import com.yf.remoting.constants.RpcConstants;
import com.yf.remoting.dto.RpcMessage;
import com.yf.serialize.Serializer;
import com.yf.serialize.kyro.KyroSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @description: TODO
 * @author: Fannsy
 * @date: 2022/9/3 9:17
 * @version: 1.0.0
 * @url:
 */
@Slf4j
public class RpcMessageEncoder extends MessageToByteEncoder<RpcMessage> {
    private static final AtomicInteger ATOMIC_INTEGER = new AtomicInteger(0);

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, RpcMessage rpcMessage, ByteBuf out) throws Exception {

        out.writeBytes(RpcConstants.MAGIC_NUMBER); // 4 B
        out.writeByte(RpcConstants.VERSION); // 1 B
        // leave a place to write the value of full length
        out.writerIndex(out.writerIndex() + 4);

        byte messageType = rpcMessage.getMessageType();
        out.writeByte(messageType);
        out.writeByte(rpcMessage.getCodec());
        out.writeByte(rpcMessage.getCompress());
        out.writeInt(ATOMIC_INTEGER.getAndIncrement());

        // full length
        byte[] bodyBytes = null;
        int fullLength = RpcConstants.HEAD_LENGTH;

        if (messageType != RpcConstants.HEARTBEAT_REQUEST_TYPE
        && messageType != RpcConstants.HEARTBEAT_RESPONSE_TYPE){
            String codecName = SerializationTypeEnum.getName(rpcMessage.getCodec());
            log.info("codec name: [{}] ", codecName);
            // Serialize
            ExtensionLoader<Serializer> extensionLoader = ExtensionLoader.getExtensionLoader(Serializer.class);
            Serializer serializer = extensionLoader.getExtension(codecName);
            bodyBytes = serializer.serialize(rpcMessage.getData());
            // compress
            String compressName = CompressTypeEnum.getName(rpcMessage.getCompress());
            log.info("compress name: [{}] ", compressName);
            ExtensionLoader<Compress> zipExtensionLoader = ExtensionLoader.getExtensionLoader(Compress.class);
            Compress compress = zipExtensionLoader.getExtension(compressName);
            bodyBytes = compress.compress(bodyBytes);
            fullLength += bodyBytes.length;
        }

        if (bodyBytes != null) {
            out.writeBytes(bodyBytes);
        }

        int writeIndex = out.writerIndex();
        out.writerIndex(writeIndex - fullLength + RpcConstants.MAGIC_NUMBER.length + 1);
        out.writeInt(fullLength);
        out.writerIndex(writeIndex);

    }
}
