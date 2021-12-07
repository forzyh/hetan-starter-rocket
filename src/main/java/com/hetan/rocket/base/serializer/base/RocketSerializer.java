package com.hetan.rocket.base.serializer.base;

/**
 * 消息序列化器接口
 *
 * @author 赵元昊
 * @date 2021/08/23 10:54
 */
public interface RocketSerializer {

	/**
	 * 序列化为byte[]
	 *
	 * @param object 对象
	 * @param <T>    泛型
	 * @return 二进制数据
	 */
	<T> byte[] serialize(T object);

	/**
	 * byte[]反序列化为对象
	 *
	 * @param bytes 序列化的二进制数据
	 * @param clazz 反序列化后的对象
	 * @param <T>   T
	 * @return 对象
	 */
	<T> T deSerialize(byte[] bytes, Class<T> clazz);
}
