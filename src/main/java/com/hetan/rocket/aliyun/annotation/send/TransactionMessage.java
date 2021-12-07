package com.hetan.rocket.aliyun.annotation.send;

import com.aliyun.openservices.ons.api.transaction.LocalTransactionChecker;
import com.aliyun.openservices.ons.api.transaction.LocalTransactionExecuter;
import com.hetan.rocket.aliyun.support.producer.defaults.executer.DefaultLocalTransactionExecuter;
import com.hetan.rocket.aliyun.support.producer.defaults.checker.DefaultLocalCommitTransactionChecker;

import java.lang.annotation.*;

/**
 * 事务消息
 *
 * @author 赵元昊
 * @date 2021/08/23 10:57
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface TransactionMessage {
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
	 * 触发消息回查后的操作：默认提交（最终一致性）
	 * 未收到消息确认时，会触发消息回查
	 * 例如消费长时间未发送
	 * 注意，正常情况下不会触发消息回查
	 * <p>
	 * 自定义LocalTransactionChecker类
	 *
	 * @return checker类对象
	 */
	Class<? extends LocalTransactionChecker> checker() default DefaultLocalCommitTransactionChecker.class;

	/**
	 * 自定义LocalTransactionExecuter类
	 *
	 * @return executer类对象
	 */
	Class<? extends LocalTransactionExecuter> executer() default DefaultLocalTransactionExecuter.class;
}
