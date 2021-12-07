package com.hetan.rocket.base.support;

import java.lang.annotation.Annotation;
import java.util.List;

import com.hetan.rocket.base.helper.MessageAttributes;
import com.hetan.rocket.base.annotation.send.RocketMessageSend;
import com.hetan.rocket.base.config.RocketProperties;

/**
 * 抽象支持类
 *
 * @author 赵元昊
 * @date 2021/08/16 18:20
 **/
public abstract class AbstractProducerContainerSupport implements Comparable<AbstractProducerContainerSupport> {

	/**
	 * 支持的注释
	 *
	 * @return {@link List<Annotation>}
	 */
	public abstract List<Class<? extends Annotation>> supportAnnotation();

	/**
	 * 是否是可解析的
	 *
	 * @param annotation 注释
	 * @return boolean
	 */
	public boolean isSupport(Annotation annotation) {
		return annotation != null
			&& supportAnnotation() != null
			&& !supportAnnotation().isEmpty()
			&& supportAnnotation().stream().anyMatch(ann -> ann.equals(annotation.annotationType()));
	}

	/**
	 * 发送
	 *
	 * @param rocketMessage    火箭的消息
	 * @param annotation       注释
	 * @param rocketProperties 火箭的属性
	 * @param deliverTime      交付时间
	 * @param body             身体
	 * @param groupId          组id
	 */
	public abstract void send(RocketMessageSend rocketMessage,
							  String groupId,
							  Annotation annotation,
							  RocketProperties rocketProperties,
							  Long deliverTime,
							  byte[] body);

	/**
	 * 得到属性
	 *
	 * @param rocketMessage    火箭的消息
	 * @param groupId          组id
	 * @param annotation       注释
	 * @param rocketProperties 火箭的属性
	 * @param deliverTime      交付时间
	 * @return {@link MessageAttributes}
	 */
	public abstract MessageAttributes getAttributes(RocketMessageSend rocketMessage,
													String groupId,
													Annotation annotation,
													RocketProperties rocketProperties,
													Long deliverTime);

	/**
	 * 排序序号
	 *
	 * @return int
	 */
	public int getOrder() {
		return Integer.MAX_VALUE;
	}

	@Override
	public int compareTo(AbstractProducerContainerSupport otherSupport) {
		return otherSupport.getOrder() - getOrder();
	}
}
