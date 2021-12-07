package com.hetan.rocket.base.config;

import com.hetan.rocket.base.serializer.DefaultJsonSerializer;
import com.hetan.rocket.base.serializer.DefaultProtoBufSerializer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 序列化器自动配置
 *
 * @author 赵元昊
 * @date 2021/08/19 12:03
 **/
@Configuration
public class RocketSerializerConfiguration {

	/**
	 * 默认的json序列化器
	 *
	 * @return {@link DefaultJsonSerializer}
	 */
	@Bean
	@ConditionalOnMissingBean(DefaultJsonSerializer.class)
	public DefaultJsonSerializer defaultJsonSerializer() {
		return new DefaultJsonSerializer();
	}

	/**
	 * 默认的原型buf序列化器
	 *
	 * @return {@link DefaultProtoBufSerializer}
	 */
	@Bean
	@ConditionalOnMissingBean(DefaultProtoBufSerializer.class)
	public DefaultProtoBufSerializer defaultProtoBufSerializer() {
		return new DefaultProtoBufSerializer();
	}
}
