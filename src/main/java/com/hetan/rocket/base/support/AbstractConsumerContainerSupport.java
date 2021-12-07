package com.hetan.rocket.base.support;

import com.hetan.rocket.base.annotation.listener.RocketMessageListener;
import com.hetan.rocket.base.config.RocketProperties;
import com.hetan.rocket.base.support.container.AbstractConsumerContainer;
import com.hetan.rocket.base.core.consumer.RocketConsumerListener;
import com.hetan.rocket.base.serializer.base.RocketSerializer;

/**
 * mq容器实现支持者
 *
 * @author 赵元昊
 * @date 2021/08/19 00:32
 **/
public abstract class AbstractConsumerContainerSupport implements Comparable<AbstractConsumerContainerSupport> {

	/**
	 * 判断是否可处理
	 *
	 * @param rocketProperties 火箭的属性
	 * @param annotation       注释
	 * @param serializer       序列化器
	 * @param groupId          组id
	 * @param topic            主题
	 * @param tag              标签
	 * @param clazz            clazz
	 * @param messageType      消息类型
	 * @param returnType       返回类型
	 * @param actingListener   代理侦听器
	 * @return boolean
	 */
	public boolean isSupport(RocketProperties rocketProperties,
							 RocketMessageListener annotation,
							 RocketSerializer serializer,
							 Class<? extends RocketConsumerListener<?, ?>> clazz,
							 Class<?> messageType,
							 Class<?> returnType,
							 RocketConsumerListener<?, ?> actingListener,
							 String groupId,
							 String topic,
							 String tag) {
		return true;
	}

	/**
	 * 生成容器
	 *
	 * @param rocketProperties 火箭的属性
	 * @param annotation       注释
	 * @param serializer       序列化器
	 * @param groupId          组id
	 * @param topic            主题
	 * @param tag              标签
	 * @param clazz            clazz
	 * @param messageType      消息类型
	 * @param returnType       返回类型
	 * @param actingListener   代理侦听器
	 * @return {@link AbstractConsumerContainer}
	 */
	public abstract AbstractConsumerContainer generateContainer(RocketProperties rocketProperties,
																RocketMessageListener annotation,
																RocketSerializer serializer,
																Class<? extends RocketConsumerListener<?, ?>> clazz,
																Class<?> messageType,
																Class<?> returnType,
																RocketConsumerListener<?, ?> actingListener,
																String groupId,
																String topic,
																String tag);

	/**
	 * 排序序号
	 *
	 * @return int
	 */
	public int getOrder() {
		return Integer.MAX_VALUE;
	}

	@Override
	public int compareTo(AbstractConsumerContainerSupport otherSupport) {
		return otherSupport.getOrder() - getOrder();
	}
}
