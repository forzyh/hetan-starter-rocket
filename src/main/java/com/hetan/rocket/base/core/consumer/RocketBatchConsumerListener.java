package com.hetan.rocket.base.core.consumer;


import java.util.List;

/**
 * 批处理消息监听器
 *
 * @author 赵元昊
 * @date 2021/08/19 03:47
 **/
public interface RocketBatchConsumerListener<T, A> extends RocketConsumerListener<T, A> {
	/**
	 * 批处理时默认禁止使用单消息处理
	 *
	 * @param message 消息
	 * @param args    arg游戏
	 * @return {@link A}
	 */
	@Override
	default A onMessage(T message, Object... args) {
		throw new IllegalStateException("该方法不允许访问");
	}

	/**
	 * 批处理消息
	 *
	 * @param message 消息
	 * @param args    arg游戏
	 * @return {@link A}
	 */
	A onBatchMessage(List<T> message, Object... args);
}
