package com.hetan.rocket.aliyun.support.producer;

import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.ONSFactory;
import com.aliyun.openservices.ons.api.order.OrderProducer;
import com.hetan.rocket.aliyun.annotation.send.OrderMessage;
import com.hetan.rocket.aliyun.util.PropertiesFactory;
import com.hetan.rocket.base.annotation.send.RocketMessageSend;
import com.hetan.rocket.base.config.RocketProperties;
import com.hetan.rocket.base.helper.MessageAttributes;
import com.hetan.rocket.base.support.AbstractProducerContainerSupport;
import com.hetan.rocket.base.utils.ProducerSignUtil;
import com.hetan.rocket.base.utils.ResolvePlaceholdersUtil;
import com.hetan.rocket.base.utils.RocketSpringUtil;
import com.hetan.rocket.base.utils.RocketThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 顺序消息提供者支持器
 *
 * @author 赵元昊
 * @date 2021/08/17 17:35
 **/
@Slf4j
public class OrderMessageProducerContainerSupport extends AbstractProducerContainerSupport {

	private final static List<Class<? extends Annotation>> SUPPORT_CLASS = Collections.singletonList(OrderMessage.class);

	private final Object lock = new Object();

	/**
	 * 提供者缓存
	 */
	public volatile Map<String, OrderProducer> producerMap = new ConcurrentHashMap<>();

	@Override
	public List<Class<? extends Annotation>> supportAnnotation() {
		return SUPPORT_CLASS;
	}

	@Override
	public void send(RocketMessageSend rocketMessage, String groupId, Annotation annotation, RocketProperties rocketProperties, Long deliverTime, byte[] body) {
		if (annotation instanceof OrderMessage) {
			groupId += RocketSpringUtil.getActiveSuffix();
			OrderMessage orderMessage = (OrderMessage) annotation;
			String sign = ProducerSignUtil.genSign(groupId, null, null);
			OrderProducer producer;
			if (producerMap.containsKey(sign)) {
				producer = producerMap.get(sign);
			} else {
				synchronized (lock) {
					if (producerMap.containsKey(sign)) {
						producer = producerMap.get(sign);
					} else {
						Properties producerProperties = PropertiesFactory.createProducerProperties(rocketProperties, groupId);
						producer = ONSFactory.createOrderProducer(producerProperties);
						producer.start();
						producerMap.put(sign, producer);
					}
				}
			}
			ResolvePlaceholdersUtil resolvePlaceholdersUtil = RocketSpringUtil.getBean(ResolvePlaceholdersUtil.class);
			String topic = resolvePlaceholdersUtil.resolvePlaceholders(orderMessage.exTopic(), orderMessage.topic(), "请配置Topic");
			String tag = resolvePlaceholdersUtil.resolvePlaceholders(orderMessage.exTag(), orderMessage.tag(), "请配置Tag");
			topic += RocketSpringUtil.getActiveSuffix();
			Message message = new Message(topic, tag, body);
			if (deliverTime != null) {
				message.setStartDeliverTime(deliverTime);
			}
			message.setKey(StringUtils.isEmpty(RocketThreadLocalUtil.getMessageKey()) ? "" : RocketThreadLocalUtil.getMessageKey());
			producer.send(message, orderMessage.shardingKey());
		}
	}

	@Override
	public MessageAttributes getAttributes(RocketMessageSend rocketMessage, String groupId, Annotation annotation, RocketProperties rocketProperties, Long deliverTime) {
		MessageAttributes messageAttributes = new MessageAttributes();
		messageAttributes.setGroupId(groupId);
		messageAttributes.setMessageType("order_send");
		if (annotation instanceof OrderMessage) {
			OrderMessage orderMessage = (OrderMessage) annotation;
			ResolvePlaceholdersUtil resolvePlaceholdersUtil = RocketSpringUtil.getBean(ResolvePlaceholdersUtil.class);
			String topic = resolvePlaceholdersUtil.resolvePlaceholders(orderMessage.exTopic(), orderMessage.topic());
			String tag = resolvePlaceholdersUtil.resolvePlaceholders(orderMessage.exTag(), orderMessage.tag());
			messageAttributes.setTags(tag);
			messageAttributes.setTopic(topic);
		}
		return messageAttributes;
	}
}
