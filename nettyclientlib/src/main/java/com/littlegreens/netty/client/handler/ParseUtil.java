package com.littlegreens.netty.client.handler;

import com.google.protobuf.ByteString;
import com.google.protobuf.MessageLite;
import com.littlegreens.netty.client.protobuf.FollowersPlus;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public abstract class ParseUtil {

    private final static ConcurrentMap<String, Method> methodCache = new ConcurrentHashMap<>();

    static {
        //找到指定包下所有protobuf实体类
        List<Class> classes = ClassUtil.getAllClassBySubClass(MessageLite.class, true, "com.littlegreens.netty.client.protobuf");
        for (Class c : classes) {
            if (!Objects.equals(c, FollowersPlus.PBMessage.class)){
                try {
                    //反射获取parseFrom方法并缓存到map
                    methodCache.put(c.getSimpleName(), c.getMethod("parseFrom", ByteString.class));
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            }
        }
//        classes.stream()
//                .filter(protoClass -> !Objects.equals(protoClass, FollowersPlus.PBMessage.class))
//                .forEach(protoClass -> {
//                    try {
//                        //反射获取parseFrom方法并缓存到map
//                        methodCache.put(protoClass.getSimpleName(), protoClass.getMethod("parseFrom", ByteString.class));
//                    } catch (NoSuchMethodException e) {
//                        throw new RuntimeException(e);
//                    }
//                });
    }

    /**
     * 根据Frame类解析出其中的body
     *
     * @param msg
     * @return
     */
    public static MessageLite parse(FollowersPlus.PBMessage msg) throws InvocationTargetException, IllegalAccessException {
        String type = "";
        ByteString body = msg.getPayload();

        Method method = methodCache.get(type);
        if (method == null) {
            throw new RuntimeException("unknown Message type :" + type);
        }
        return (MessageLite) method.invoke(null, body);
    }
}

