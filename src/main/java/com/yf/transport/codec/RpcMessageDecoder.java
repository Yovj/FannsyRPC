package com.yf.transport.codec;

import com.yf.common.enums.CompressTypeEnum;
import com.yf.common.enums.RpcErrorMessageEnum;
import com.yf.common.enums.SerializationTypeEnum;
import com.yf.common.exception.RpcException;
import com.yf.common.extension.ExtensionLoader;
import com.yf.compress.Compress;
import com.yf.compress.gzip.GzipCompress;
import com.yf.remoting.constants.RpcConstants;
import com.yf.remoting.dto.RpcMessage;
import com.yf.remoting.dto.RpcRequest;
import com.yf.remoting.dto.RpcResponse;
import com.yf.serialize.Serializer;
import com.yf.serialize.kyro.KyroSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * @description: TODO
 * @author: Fannsy
 * @date: 2022/9/3 9:17
 * @version: 1.0.0
 * @url:
 */
public class RpcMessageDecoder extends LengthFieldBasedFrameDecoder {
    public RpcMessageDecoder(){
        this(RpcConstants.MAX_FRAME_LENGTH,5,4,-9,0);
    }

    public RpcMessageDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment, int initialBytesToStrip) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        Object decode =  super.decode(ctx, in);
        if (decode instanceof ByteBuf){
            Object o = runDecode((ByteBuf) decode);
            return o;
        }
        return decode;

    }

    private Object runDecode(ByteBuf in){
        checkMagicNumber(in);
        checkVersion(in);
        int fullLength = in.readInt();
        byte messageType = in.readByte();
        byte codecType = in.readByte();
        byte compressType = in.readByte();
        int requestId = in.readInt();
        RpcMessage rpcMessage = RpcMessage.builder()
                .codec(codecType)
                .compress(compressType)
                .requestId(requestId)
                .messageType(messageType)
                .build();
        if (messageType == RpcConstants.HEARTBEAT_REQUEST_TYPE){
            rpcMessage.setData(RpcConstants.PING);
            return rpcMessage;
        }
        if (messageType == RpcConstants.HEARTBEAT_RESPONSE_TYPE){
            rpcMessage.setData(RpcConstants.PONG);
            return rpcMessage;
        }
        int bodyLength = fullLength - RpcConstants.HEAD_LENGTH;
        if (bodyLength > 0){
            byte[] bs = new byte[bodyLength];
            in.readBytes(bs);

            String compressName = CompressTypeEnum.getName(compressType);
            ExtensionLoader<Compress> zipExtensionLoader = ExtensionLoader.getExtensionLoader(Compress.class);
            Compress compress = zipExtensionLoader.getExtension(compressName);
            bs = compress.decompress(bs);

            String codecName = SerializationTypeEnum.getName(codecType);
            ExtensionLoader<Serializer> extensionLoader = ExtensionLoader.getExtensionLoader(Serializer.class);
            Serializer serializer = extensionLoader.getExtension(codecName);
            if (messageType == RpcConstants.REQUEST_TYPE){
                RpcRequest rpcRequest = serializer.deserialize(bs, RpcRequest.class);
                rpcMessage.setData(rpcRequest);
            }else {
                RpcResponse rpcResponse = serializer.deserialize(bs, RpcResponse.class);
                rpcMessage.setData(rpcResponse);
            }
        }
        return rpcMessage;
    }



    private void checkVersion(ByteBuf in){
        // read the version and compare
        byte version = in.readByte();
        if (version != RpcConstants.VERSION) {
            throw new RuntimeException("version isn't compatible" + version);
        }
    }

    private void checkMagicNumber(ByteBuf in){
        int len = RpcConstants.MAGIC_NUMBER.length;
        byte[] tmp = new byte[len];
        in.readBytes(tmp);
        for (int i = 0; i < tmp.length; i++) {
            if (tmp[i] != RpcConstants.MAGIC_NUMBER[i]){
                throw new RpcException(RpcErrorMessageEnum.MAGIC_NUMBER_NOT_MATCH);
            }
        }
    }

}
