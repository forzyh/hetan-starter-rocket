package com.hetan.rocket.base.core.consumer;

/**
 * 监听器 结合官方starter与阿里sdk抽象实现 顶层抽象接口 所有的上下文解析最顶层为此
 *
 * @author 赵元昊
 * @date 2021/08/19 00:29
 */
public interface RocketConsumerListener<T, A> {

	/**
	 * 消费消息
	 *
	 * @param message 消息
	 * @param args    args 可变参数为保留位 在Aliyun sdk中 会传入一个aliyun sdk 保留的上下文参数
	 * @return {@link A}
	 */
	A onMessage(T message, Object... args);
}
