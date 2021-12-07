package com.hetan.rocket.aliyun.support.consumer.container;

import com.aliyun.openservices.ons.api.*;
import com.aliyun.openservices.ons.api.order.OrderAction;
import com.aliyun.openservices.ons.api.order.OrderConsumer;
import com.hetan.rocket.aliyun.core.consumer.AliyunOrderConsumerListener;
import com.hetan.rocket.aliyun.util.PropertiesFactory;
import com.hetan.rocket.base.config.RocketProperties;
import com.hetan.rocket.base.helper.MessageAttributes;
import com.hetan.rocket.base.serializer.base.RocketSerializer;
import com.hetan.rocket.base.state.MessageModelState;
import com.hetan.rocket.base.support.container.AbstractConsumerContainer;
import com.hetan.rocket.base.utils.RocketThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Properties;

/**
 * 顺序消息容器的实现
 *
 * @author 赵元昊
 * @date 2021/08/19 01:49
 **/
@Slf4j
public class OrderRocketConsumerContainer<T> extends AbstractConsumerContainer<T, OrderAction> {

	private final String messageState;
	private OrderConsumer consumer;

	public OrderRocketConsumerContainer(RocketProperties rocketProperties,
                                        RocketSerializer serializer,
                                        AliyunOrderConsumerListener<T> actingListener,
                                        Class<T> messageType,
                                        Class<OrderAction> returnType,
                                        String groupId,
                                        String topic,
                                        String tag,
                                        String messageState) {
		super(rocketProperties, serializer, actingListener, messageType, OrderAction.class, groupId, topic, tag);
		this.messageState = messageState;
	}

	@Override
	public void start() {
		String messageModel = null;
		if (MessageModelState.CLUSTERING.equals(messageState)) {
			messageModel = PropertyValueConst.CLUSTERING;
		}
		if (MessageModelState.BROADCASTING.equals(messageState)) {
			messageModel = PropertyValueConst.BROADCASTING;
		}
		Properties consumerProperties = PropertiesFactory.createConsumerProperties(rocketProperties, groupId, messageModel);
		MessageAttributes messageAttributes = new MessageAttributes();
		messageAttributes.setMessageType("order_listener");
		messageAttributes.setGroupId(groupId);
		messageAttributes.setTopic(topic);
		messageAttributes.setTags(tag);
		messageAttributes.setBusinessType(this.businessType);
		consumer = ONSFactory.createOrderedConsumer(consumerProperties);
		consumer.subscribe(topic, tag, (message, context) -> {
			try {
				RocketThreadLocalUtil.setMessageKey(message.getKey());
				log.info("接收到消息,开始消费:{}", message);
				if (Message.class.getName().equals(messageType.getName())) {
					return onMessage(messageAttributes, (T) message, context);
				} else {
					return onMessage(messageAttributes, message.getBody(), context);
				}
			} finally {
				RocketThreadLocalUtil.clearAll();
			}
		});
		consumer.start();
	}

	@Override
	public OrderAction getSuccessCommit() {
		return OrderAction.Success;
	}

	@Override
	public boolean isRunning() {
		if (consumer == null) {
			return false;
		} else {
			return consumer.isStarted();
		}
	}

	@Override
	public void destroy() throws Exception {
		consumer.shutdown();
	}
}
