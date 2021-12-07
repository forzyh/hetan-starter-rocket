package com.hetan.rocket.base.serializer;

import com.alibaba.fastjson.JSON;
import com.hetan.rocket.base.serializer.base.RocketSerializer;

/**
 * json 序列化器 会跳过空数据的序列化与反序列化 会跳过字节数组的序列化与反序列化 其他都由fastJson进行序列化
 *
 * @author 赵元昊
 * @date 2021/08/17 19:01
 **/
public class DefaultJsonSerializer implements RocketSerializer {

	@Override
	public <T> byte[] serialize(T object) {
		if (object == null) {
			return null;
		}
		if (byte[].class.equals(object.getClass())) {
			return (byte[]) object;
		}
		return JSON.toJSONBytes(object);
	}

	@Override
	public <T> T deSerialize(byte[] bytes, Class<T> clazz) {
		if (bytes == null) {
			return null;
		}
		if (byte[].class.equals(clazz)) {
			return (T) bytes;
		}
		return JSON.parseObject(bytes, clazz);
	}
}
