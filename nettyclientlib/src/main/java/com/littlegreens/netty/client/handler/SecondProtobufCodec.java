package com.littlegreens.netty.client.handler;

import com.google.protobuf.MessageLite;
import com.littlegreens.netty.client.protobuf.FollowersPlus;

import java.util.Arrays;
import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.MessageToMessageCodec;

public class SecondProtobufCodec extends MessageToMessageCodec<FollowersPlus.PBMessage, MessageLite> {
    public SecondProtobufCodec() {
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, MessageLite messageLite, List<Object> out) throws Exception {
        out.add(FollowersPlus.PBMessage.newBuilder()
                .setPayload(messageLite.toByteString())
                .build());
        System.out.println("******************" + messageLite.toString());
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, FollowersPlus.PBMessage pbMessage, List<Object> list) throws Exception {
        //todo
        System.out.println("------------------" + pbMessage.toString());
    }


//    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out) throws Exception {
//        int bodyLen = msg.readableBytes();
//        byte[] head = intToByte4L(bodyLen);
//        out.ensureWritable(bodyLen + head.length);
//        out.writeBytes(head);
//        out.writeBytes(msg);
//    }


    /**
     * 转换成大端模式-高字节在前(java为高字节在前,内存数组第0位表示最前)
     *
     * 有关高字节与低字节的说明:
     * 大端模式: Big-Endian    就是高位字节(书写顺序的最左边)排放在内存的低地址端(数组第0位)，低位字节排放在内存的高地址端
     * 小端模式: Little-Endian 就是低位字节(书写顺序的最左边)排放在内存的低地址端(数组第0位)，高位字节排放在内存的高地址端
     * 记忆方法: 就是看谁先放于低位地址(数组第0位)，如果是高位字节就是大端；如果是低位字节就是小端。
     *
     * 所以在TCP/IP协议规定了在网络上必须采用网络字节顺序，也就是大端模式。对于char型数据只占一个字节，无所谓大端和小端。
     * 存储量大于1字节，如int，float等，要考虑字节的顺序问题了.
     * java由于虚拟机的关系,屏蔽了大小端问题
     * 1. 网络字节序是, 大端模式, 也就是高字节在前
     * 2. C++是主机字节序（高、低都有可能）
     * 3. JAVA是网络字节序,大端模式,也就是高字节在前
     *
     * 例子:
     * 10进制: 1111111111
     * 二进制: 01000010 00111010 00110101 11000111
     * 16进制: 42 3A 35 C7
     * 内存地址数组: 第0位,第1位,第2位,第3位 (第0位是起始地址,第3位是结束地址,起始地址当然是最低位)
     * 大端模式存储结果:42 3A 35 C7 (42是高位字节,存储在起始地址,也是第0位地址,也是低位地址, 与书写顺序一样, 高字节在前, 低字节在后)
     * 小端模式存储结果:C7 35 3A 42 (C7是低位字节,存储在起始地址,也是第0位地址,也是低位地址, 与书写顺序相反, 低字节在前, 高字节在后)
     */
    public static byte[] intToByte4B(int n) {
        byte[] b = new byte[4];
        b[0] = (byte) (n >> 24 & 0xff); //数据组起始位,存放内存起始位, 即:高字节在前
        b[1] = (byte) (n >> 16 & 0xff); //高字节在前是与java存放内存一样的, 与书写顺序一样
        b[2] = (byte) (n >> 8 & 0xff);
        b[3] = (byte) (n & 0xff);
        return b;
    }

    /**
     * 转换成小端模式-高字节在后(java为高字节在前,内存数组第0位表示最前)
     */
    public static byte[] intToByte4L(int n) {
        byte[] b = new byte[4];
        b[0] = (byte) (n & 0xff);
        b[1] = (byte) (n >> 8 & 0xff);
        b[2] = (byte) (n >> 16 & 0xff);//高字节在后是与java存放内存相反, 与书写顺序相反
        b[3] = (byte) (n >> 24 & 0xff);//数据组结束位,存放内存起始位, 即:高字节在后
        return b;
    }

    /**
     * 整数到字节数组转换,java为高字节在前(数组0表示最前)
     */
    public static int byte4ToIntB(byte b[]) {
        int s = 0;
        s = ((((b[0] & 0xff) << 24 | (b[1] & 0xff)) << 16) | (b[2] & 0xff)) << 8| (b[3] & 0xff);
        return s;
    }

    /**
     * 整数到字节数组转换,java为高字节在后(数组3表示最前)
     */
    public static int byte4ToIntL(byte b[]) {
        int s = 0;
        s = ((((b[3] & 0xff) << 24 | (b[2] & 0xff)) << 16) | (b[1] & 0xff)) << 8 | (b[0] & 0xff);
        return s;
    }
}
