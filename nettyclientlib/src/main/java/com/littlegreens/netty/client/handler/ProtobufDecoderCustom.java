package com.littlegreens.netty.client.handler;

import com.littlegreens.netty.client.protobuf.FollowersPlus;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

public class ProtobufDecoderCustom extends MessageToMessageDecoder<ByteBuf> {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in,
                          List<Object> out) throws Exception {
        int bodyLen = in.readableBytes();
        if (bodyLen < 4) return;
        ByteBuf frame = Unpooled.buffer(bodyLen - 4);
        in.readerIndex(4);
        in.readBytes(frame);

        try {
            byte[] inByte = frame.array();
            // 字节转成对象
            FollowersPlus.PBMessage msg = FollowersPlus.PBMessage.parseFrom(inByte);

            if (msg != null) {
                // 获取业务消息头
                out.add(msg);
            }
        } catch (Exception e) {
            System.out.println(e.getLocalizedMessage());
        }

    }
}
