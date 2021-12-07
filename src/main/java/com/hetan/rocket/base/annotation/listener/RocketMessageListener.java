package com.hetan.rocket.base.annotation.listener;

import com.hetan.rocket.base.state.MessageModelState;

import java.lang.annotation.*;

/**
 * 消息侦听器
 *
 * @author 赵元昊
 * @date 2021/08/23 10:55
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface RocketMessageListener {

	/**
	 * 您在控制台创建的 Group ID
	 *
	 * @return String
	 */
	String groupID() default "";

	/**
	 * 外部GroupId配置
	 *
	 * @return {@link String}
	 */
	String exGroupID() default "";

	/**
	 * Message 所属的 Topic
	 *
	 * @return String
	 */
	String topic() default "";

	/**
	 * 外部Topic配置
	 *
	 * @return {@link String}
	 */
	String exTopic() default "";

	/**
	 * 订阅指定 Topic 下的 Tags：
	 * 1. * 表示订阅所有消息
	 * 2. TagA || TagB || TagC 表示订阅 TagA 或 TagB 或 TagC 的消息
	 *
	 * @return String
	 */
	String tag() default "";

	/**
	 * 外部tag配置路径
	 *
	 * @return {@link String}
	 */
	String exTag() default "";

	/**
	 * 消费模式，默认集群消费
	 *
	 * @return String
	 */
	String messageModel() default MessageModelState.CLUSTERING;

	/**
	 * 消费者模型 默认忽略 由类型判断
	 *
	 * @return {@link String}
	 */
	String consumerModel() default "";

	/**
	 * 设置批量消费最大消息数量,当指定Topic的消息数量已经攒够128条,SDK立即执行回调进行消费.默认值：32,取值范围：1~1024.
	 */
	int consumeMessageBatchMaxSize() default 32;

	/**
	 * 设置批量消费最大等待时长,当等待时间达到10秒,SDK立即执行回调进行消费.默认值：0,取值范围：0~450,单位：秒.
	 */
	int batchConsumeMaxAwaitDurationInSeconds() default 0;

	/**
	 * 接收的参数类型同泛型 T
	 *
	 * @return {@link Class<?>}
	 */
	Class<?> messageType();

	/**
	 * 返回的消息状态类型 A alisdk中目前暂不处理 故使用object占位
	 *
	 * @return {@link Class<?>}
	 */
	Class<?> returnType() default Object.class;
}
