package com.hetan.rocket.base.serializer;

import com.hetan.rocket.base.serializer.base.RocketSerializer;

/**
 * 默认引用的类
 *
 * @author 赵元昊
 * @date 2021/08/19 13:02
 **/
public interface DefaultSerializerClazz {

	/**
	 * 默认的序列化器clazz
	 */
	Class<? extends RocketSerializer> DEFAULT_SERIALIZER_CLAZZ = DefaultJsonSerializer.class;
}
