package com.hetan.rocket.aliyun.annotation.send;


import java.lang.annotation.*;

/**
 * 有序消息
 *
 * @author 赵元昊
 * @date 2021/08/23 10:57
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface OrderMessage {

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
	 * 分区顺序消息中区分不同分区的关键字段，sharding key 于普通消息的 key 是完全不同的概念。
	 * 全局顺序消息，该字段可以设置为任意非空字符串。
	 *
	 * @return String
	 */
	String shardingKey() default "";
}
