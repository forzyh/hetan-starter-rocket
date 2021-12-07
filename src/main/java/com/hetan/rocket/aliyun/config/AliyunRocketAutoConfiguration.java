package com.hetan.rocket.aliyun.config;

import com.hetan.rocket.aliyun.support.AliRocketMqTemplate;
import com.hetan.rocket.aliyun.support.consumer.DefaultConsumerContainerSupport;
import com.hetan.rocket.aliyun.support.producer.defaults.callback.DefaultSendCallback;
import com.hetan.rocket.aliyun.support.producer.defaults.checker.DefaultLocalCommitTransactionChecker;
import com.hetan.rocket.aliyun.support.producer.defaults.checker.DefaultLocalRollbackTransactionChecker;
import com.hetan.rocket.aliyun.support.producer.defaults.checker.DefaultLocalUnknowTransactionChecker;
import com.hetan.rocket.aliyun.support.producer.defaults.executer.DefaultLocalTransactionExecuter;
import com.hetan.rocket.base.annotation.EnableRocketMQ;
import com.hetan.rocket.base.config.RocketAutoConfiguration;
import com.hetan.rocket.base.config.RocketProperties;
import com.hetan.rocket.base.serializer.DefaultJsonSerializer;
import com.hetan.rocket.base.utils.ResolvePlaceholdersUtil;
import com.hetan.rocket.aliyun.support.producer.CommonMessageProducerContainerSupport;
import com.hetan.rocket.aliyun.support.producer.OrderMessageProducerContainerSupport;
import com.hetan.rocket.aliyun.support.producer.TransactionMessageProducerContainerSupport;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.StandardEnvironment;

/**
 * 自动配置 需要在{@link RocketAutoConfiguration}之前进行初始化 为其初始化容器支持者数据
 *
 * @author 赵元昊
 * @date 2021/08/19 11:40
 **/
@Configuration
@ConditionalOnBean(annotation = EnableRocketMQ.class)
@AutoConfigureBefore(RocketAutoConfiguration.class)
@ConditionalOnProperty(prefix = RocketProperties.ROCKET_PREFIX, value = "enable-aliyun", havingValue = "true", matchIfMissing = true)
public class AliyunRocketAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public AliRocketMqTemplate aliRocketMqTemplate(RocketProperties rocketProperties, DefaultJsonSerializer defaultJsonSerializer) {
		return new AliRocketMqTemplate(rocketProperties, defaultJsonSerializer);
	}

	/**
	 * 容器默认消费者支持
	 *
	 * @return {@link DefaultConsumerContainerSupport}
	 */
	@Bean
	@ConditionalOnMissingBean(DefaultConsumerContainerSupport.class)
	public DefaultConsumerContainerSupport defaultConsumerContainerSupport() {
		return new DefaultConsumerContainerSupport();
	}

	/**
	 * 公共消息生产者容器支持
	 *
	 * @return {@link CommonMessageProducerContainerSupport}
	 */
	@Bean
	@ConditionalOnMissingBean(CommonMessageProducerContainerSupport.class)
	public CommonMessageProducerContainerSupport commonMessageProducerContainerSupport(AliRocketMqTemplate aliRocketMqTemplate) {
		return new CommonMessageProducerContainerSupport(aliRocketMqTemplate);
	}

	/**
	 * 订单消息生成方容器支持
	 *
	 * @return {@link OrderMessageProducerContainerSupport}
	 */
	@Bean
	@ConditionalOnMissingBean(OrderMessageProducerContainerSupport.class)
	public OrderMessageProducerContainerSupport orderMessageProducerContainerSupport() {
		return new OrderMessageProducerContainerSupport();
	}

	/**
	 * 事务消息生成容器支持
	 *
	 * @return {@link TransactionMessageProducerContainerSupport}
	 */
	@Bean
	@ConditionalOnMissingBean(TransactionMessageProducerContainerSupport.class)
	public TransactionMessageProducerContainerSupport transactionMessageProducerContainerSupport() {
		return new TransactionMessageProducerContainerSupport();
	}

	@Bean
	@ConditionalOnMissingBean(DefaultLocalCommitTransactionChecker.class)
	public DefaultLocalCommitTransactionChecker defaultLocalCommitTransactionChecker() {
		return new DefaultLocalCommitTransactionChecker();
	}

	@Bean
	@ConditionalOnMissingBean(DefaultLocalRollbackTransactionChecker.class)
	public DefaultLocalRollbackTransactionChecker defaultLocalRollbackTransactionChecker() {
		return new DefaultLocalRollbackTransactionChecker();
	}

	@Bean
	@ConditionalOnMissingBean(DefaultLocalUnknowTransactionChecker.class)
	public DefaultLocalUnknowTransactionChecker defaultLocalUnknowTransactionChecker() {
		return new DefaultLocalUnknowTransactionChecker();
	}

	/**
	 * 默认本地事务实行
	 *
	 * @return {@link DefaultLocalTransactionExecuter}
	 */
	@Bean
	@ConditionalOnMissingBean(DefaultLocalTransactionExecuter.class)
	public DefaultLocalTransactionExecuter defaultLocalTransactionExecuter() {
		return new DefaultLocalTransactionExecuter();
	}

	/**
	 * 默认发送回调
	 *
	 * @return {@link DefaultSendCallback}
	 */
	@Bean
	@ConditionalOnMissingBean(DefaultSendCallback.class)
	public DefaultSendCallback defaultSendCallback() {
		return new DefaultSendCallback();
	}

	/**
	 * 解决占位符跑龙套
	 *
	 * @param environment 环境
	 * @return {@link ResolvePlaceholdersUtil}
	 */
	@Bean
	@ConditionalOnMissingBean(ResolvePlaceholdersUtil.class)
	public ResolvePlaceholdersUtil resolvePlaceholdersUtil(StandardEnvironment environment) {
		return new ResolvePlaceholdersUtil(environment);
	}
}
