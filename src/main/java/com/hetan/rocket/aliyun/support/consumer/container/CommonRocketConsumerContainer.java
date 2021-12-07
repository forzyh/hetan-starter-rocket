package com.hetan.rocket.aliyun.support.consumer.container;

import com.aliyun.openservices.ons.api.*;
import com.hetan.rocket.aliyun.core.consumer.AliyunCommonConsumerListener;
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
 * 常用消息消费者容器实现
 *
 * @author 赵元昊
 * @date 2021/08/19 01:49
 **/
@Slf4j
public class CommonRocketConsumerContainer<T> extends AbstractConsumerContainer<T, Action> {

	private final String messageState;
	private Consumer consumer;

	public CommonRocketConsumerContainer(RocketProperties rocketProperties,
                                         RocketSerializer serializer,
                                         AliyunCommonConsumerListener<T> actingListener,
                                         Class<T> messageType,
                                         Class<Action> returnType,
                                         String groupId,
                                         String topic,
                                         String tag,
                                         String messageState) {
		super(rocketProperties, serializer, actingListener, messageType, Action.class, groupId, topic, tag);
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
		consumer = ONSFactory.createConsumer(consumerProperties);
		MessageAttributes messageAttributes = new MessageAttributes();
		messageAttributes.setMessageType("common_listener");
		messageAttributes.setGroupId(groupId);
		messageAttributes.setTopic(topic);
		messageAttributes.setTags(tag);
		messageAttributes.setBusinessType(this.businessType);
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
	public Action getSuccessCommit() {
		return Action.CommitMessage;
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
