package com.hetan.rocket.base.serializer;

import com.hetan.rocket.base.serializer.base.RocketSerializer;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtobufIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

/**
 * 默认的原型buf序列化器
 *
 * @author 赵元昊
 * @date 2021/08/23 10:54
 */
@Deprecated
public class DefaultProtoBufSerializer implements RocketSerializer {

	@Override
	@SuppressWarnings("unchecked")
	public <T> byte[] serialize(T object) {
		Schema<T> schema = (Schema<T>) RuntimeSchema.getSchema(object.getClass());
		return ProtobufIOUtil.toByteArray(object, schema, LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE));
	}

	@Override
	public <T> T deSerialize(byte[] bytes, Class<T> clazz) {
		RuntimeSchema<T> runtimeSchema = RuntimeSchema.createFrom(clazz);
		T object = runtimeSchema.newMessage();
		ProtobufIOUtil.mergeFrom(bytes, object, runtimeSchema);
		return object;
	}
}
