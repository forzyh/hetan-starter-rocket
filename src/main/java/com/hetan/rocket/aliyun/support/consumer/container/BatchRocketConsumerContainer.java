package com.hetan.rocket.aliyun.support.consumer.container;

import com.aliyun.openservices.ons.api.*;
import com.aliyun.openservices.ons.api.batch.BatchConsumer;
import com.hetan.rocket.aliyun.core.consumer.AliyunBatchConsumerListener;
import com.hetan.rocket.aliyun.util.PropertiesFactory;
import com.hetan.rocket.base.annotation.listener.RocketMessageListener;
import com.hetan.rocket.base.config.RocketProperties;
import com.hetan.rocket.base.serializer.base.RocketSerializer;
import com.hetan.rocket.base.state.MessageModelState;
import com.hetan.rocket.base.support.container.AbstractConsumerContainer;
import com.hetan.rocket.base.utils.RocketThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopProxyUtils;

import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * 批处理消费者容器实现
 *
 * @author 赵元昊
 * @date 2021/08/19 01:49
 **/
@Slf4j
public class BatchRocketConsumerContainer<T> extends AbstractConsumerContainer<T, Action> {

	private final String messageState;
	private BatchConsumer consumer;

	public BatchRocketConsumerContainer(RocketProperties rocketProperties,
                                        RocketSerializer serializer,
                                        AliyunBatchConsumerListener<T> actingListener,
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
		Class<?> targetClass = AopProxyUtils.ultimateTargetClass(this.actingListener);
		RocketMessageListener rocketMessageListener = targetClass.getAnnotation(RocketMessageListener.class);
		if (rocketMessageListener != null) {
			consumerProperties.setProperty(PropertyKeyConst.ConsumeMessageBatchMaxSize,
				Integer.toString(rocketMessageListener.consumeMessageBatchMaxSize()));
			consumerProperties.setProperty(PropertyKeyConst.BatchConsumeMaxAwaitDurationInSeconds,
				Integer.toString(rocketMessageListener.batchConsumeMaxAwaitDurationInSeconds()));
		}
		consumer = ONSFactory.createBatchConsumer(consumerProperties);
		consumer.subscribe(topic, tag, (messages, context) -> {
			try {
				List<String> messageKeys = messages.stream().map(Message::getKey).collect(Collectors.toList());
				RocketThreadLocalUtil.setBatchMessageKeys(messageKeys);
				log.info("接收到消息,开始消费:{}", messages);
				if (messageType.getName().equals(messageType.getName())) {
					return (Action) ((AliyunBatchConsumerListener) this.actingListener).onBatchMessage((List<T>) messages);
				} else {
					List<byte[]> bytes = messages.stream().map(Message::getBody).collect(Collectors.toList());
					List<T> messageTypeResults = bytes.stream().map(b -> this.serializer.deSerialize(b, messageType)).collect(Collectors.toList());
					return (Action) ((AliyunBatchConsumerListener) this.actingListener).onBatchMessage(messageTypeResults, context);
				}
			} catch (Exception e) {
				log.error("消费时发生异常,发生异常的Message:{},异常信息:{}", messages, e.getMessage());
				e.printStackTrace();
				throw e;
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
