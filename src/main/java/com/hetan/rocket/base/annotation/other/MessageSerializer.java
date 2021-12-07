package com.hetan.rocket.base.annotation.other;

import com.hetan.rocket.base.serializer.DefaultJsonSerializer;
import com.hetan.rocket.base.serializer.base.RocketSerializer;

import java.lang.annotation.*;

/**
 * 消息序列化器
 *
 * @author 赵元昊
 * @date 2021/08/17 14:05
 **/
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface MessageSerializer {

	/**
	 * 序列化器
	 *
	 * @return {@link Class<? extends RocketSerializer>}
	 */
	Class<? extends RocketSerializer> serializer() default DefaultJsonSerializer.class;

	/**
	 * 反序列化器
	 *
	 * @return {@link Class<? extends RocketSerializer>}
	 */
	Class<? extends RocketSerializer> deSerializer() default DefaultJsonSerializer.class;

}
