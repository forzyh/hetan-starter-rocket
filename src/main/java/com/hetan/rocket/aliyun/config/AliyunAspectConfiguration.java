package com.hetan.rocket.aliyun.config;

import com.hetan.rocket.aliyun.aspect.AliyunRocketSendAspect;
import com.hetan.rocket.base.config.RocketAutoConfiguration;
import com.hetan.rocket.base.config.RocketProperties;
import com.hetan.rocket.base.config.init.RocketProducerContainerInitConfig;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.StandardEnvironment;

/**
 * 切面自动配置 应在base包{@link RocketAutoConfiguration}生效之后生效
 *
 * @author 赵元昊
 * @date 2021/08/19 11:54
 **/
@Configuration
@AutoConfigureAfter(RocketAutoConfiguration.class)
public class AliyunAspectConfiguration {

	/**
	 * 加了基于阿里sdk实现的注解的切面
	 *
	 * @param rocketProducerContainerInitConfig 火箭生产商容器初始化配置
	 * @param rocketProperties                  火箭的属性
	 * @param standardEnvironment               标准环境
	 * @return {@link AliyunRocketSendAspect}
	 */
	@Bean
	@ConditionalOnMissingBean(AliyunRocketSendAspect.class)
	@ConditionalOnBean({RocketProperties.class, RocketProducerContainerInitConfig.class})
	public AliyunRocketSendAspect aliyunRocketSendAspect(RocketProducerContainerInitConfig rocketProducerContainerInitConfig,
														 RocketProperties rocketProperties,
														 StandardEnvironment standardEnvironment) {
		return new AliyunRocketSendAspect(rocketProducerContainerInitConfig, rocketProperties, standardEnvironment);
	}
}
