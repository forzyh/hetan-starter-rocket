package com.hetan.rocket.base.config;

import com.hetan.rocket.base.annotation.EnableRocketMQ;
import com.hetan.rocket.base.config.init.RocketConsumerContainerInitConfig;
import com.hetan.rocket.base.config.init.RocketProducerContainerInitConfig;
import com.hetan.rocket.base.core.consumer.RocketConsumerListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.StandardEnvironment;

/**
 * 基础的自动配置
 *
 * @author 赵元昊
 * @date 2021/08/19 19:03
 */
@Configuration
@EnableConfigurationProperties(RocketProperties.class)
@ConditionalOnBean(annotation = EnableRocketMQ.class)
@Import({RocketSerializerConfiguration.class})
public class RocketAutoConfiguration {

	/**
	 * 生成提供者全局配置 会检索所有的提供者支持器 将提供者支持器进行排序等操作 在此之前需要实例化 消费者提供器 否则会抛出异常
	 *
	 * @return {@link RocketProducerContainerInitConfig}
	 */
	@Bean
	@ConditionalOnMissingBean(RocketProducerContainerInitConfig.class)
	public RocketProducerContainerInitConfig rocketProducerContainerInitConfig() {
		return new RocketProducerContainerInitConfig();
	}

	/**
	 * 消费者监听容器支持器 会检索系统中所有基于顶层接口{@link RocketConsumerListener}的bean
	 * <p>
	 * 并会搜索可初始化这些bean监听器的容器支持器 找到容器支持器后 将这些bean初始化成可使用的监听容器 并通过bean的形式注册到系统中
	 *
	 * @param rocketProperties 火箭的属性
	 * @param environment      环境
	 * @return {@link RocketConsumerContainerInitConfig}
	 */
	@Bean
	@ConditionalOnMissingBean(RocketConsumerContainerInitConfig.class)
	public RocketConsumerContainerInitConfig rocketConsumerContainerInitConfig(RocketProperties rocketProperties, StandardEnvironment environment) {
		return new RocketConsumerContainerInitConfig(rocketProperties, environment);
	}
}
