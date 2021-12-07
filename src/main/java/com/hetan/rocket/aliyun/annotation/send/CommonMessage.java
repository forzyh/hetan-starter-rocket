package com.hetan.rocket.aliyun.annotation.send;

import com.aliyun.openservices.ons.api.SendCallback;
import com.hetan.rocket.aliyun.support.producer.defaults.callback.DefaultSendCallback;
import com.hetan.rocket.aliyun.support.producer.enums.MessageSendType;

import java.lang.annotation.*;

/**
 * 公共消息
 *
 * @author 赵元昊
 * @date 2021/08/23 10:57
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface CommonMessage {
	/**
	 * Message 所属的 Topic
	 *
	 * @return String
	 */
	String topic() default "";

	/**
	 * 外部配置topic
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
	 * 外部配置tag
	 *
	 * @return {@link String}
	 */
	String exTag() default "";

	/**
	 * 消息发送类型 默认异步
	 *
	 * @return MessageSendType
	 */
	MessageSendType messageSendType() default MessageSendType.SEND_ASYNC;

	/**
	 * 自定义SendCallback类
	 *
	 * @return callback
	 */
	Class<? extends SendCallback> callback() default DefaultSendCallback.class;
}
