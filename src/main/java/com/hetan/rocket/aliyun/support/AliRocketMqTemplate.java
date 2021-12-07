package com.hetan.rocket.aliyun.support;

import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.ONSFactory;
import com.aliyun.openservices.ons.api.Producer;
import com.aliyun.openservices.ons.api.SendCallback;
import com.hetan.rocket.base.config.RocketProperties;
import com.hetan.rocket.base.serializer.base.RocketSerializer;
import com.hetan.rocket.base.utils.ProducerSignUtil;
import com.hetan.rocket.base.utils.RocketSpringUtil;
import com.hetan.rocket.aliyun.support.producer.enums.MessageSendType;
import com.hetan.rocket.aliyun.util.PropertiesFactory;
import lombok.Data;

import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 阿里mq template
 *
 * @author 赵元昊
 * @date 2021/11/08 15:35
 **/
@Data
public class AliRocketMqTemplate {

	public static final String DEFAULT_GROUP = "";

	private final Object lock = new Object();

	public volatile Map<String, Producer> producerMap = new ConcurrentHashMap<>();


	private final RocketProperties rocketProperties;
	private final RocketSerializer serializer;

	public AliRocketMqTemplate(RocketProperties rocketProperties, RocketSerializer serializer) {
		this.rocketProperties = rocketProperties;
		this.serializer = serializer;
	}

	public Producer build(String groupId, RocketProperties rocketProperties) {
		groupId += RocketSpringUtil.getActiveSuffix();
		String sign = ProducerSignUtil.genSign(groupId, null, null);
		Producer producer;
		if (producerMap.containsKey(sign)) {
			producer = producerMap.get(sign);
		} else {
			synchronized (lock) {
				if (producerMap.containsKey(sign)) {
					producer = producerMap.get(sign);
				} else {
					Properties producerProperties = PropertiesFactory.createProducerProperties(rocketProperties, groupId);
					producer = ONSFactory.createProducer(producerProperties);
					producer.start();
					producerMap.put(sign, producer);
				}
			}
		}
		return producer;
	}

	public Producer build(String groupId) {
		return this.build(groupId, this.rocketProperties);
	}

	/**
	 * 发送消息
	 *
	 * @param groupId     组id
	 * @param topic       主题
	 * @param tag         标签
	 * @param target      目标
	 * @param messageKey  消息键
	 * @param deliverTime 交付时间
	 * @param sendType    发送类型
	 * @param callback    回调
	 */
	public void sendCommon(String groupId, String topic, String tag, Object target, String messageKey, Long deliverTime, MessageSendType sendType, SendCallback callback) {
		this.sendCommon(groupId, topic, tag, this.serializer.serialize(target), messageKey, deliverTime, sendType, callback);
	}

	/**
	 * 发送消息
	 *
	 * @param groupId     组id
	 * @param topic       主题
	 * @param tag         标签
	 * @param body        身体
	 * @param messageKey  消息键
	 * @param deliverTime 交付时间
	 * @param sendType    发送类型
	 * @param callback    回调
	 */
	public void sendCommon(String groupId, String topic, String tag, byte[] body, String messageKey, Long deliverTime, MessageSendType sendType, SendCallback callback) {
		topic += RocketSpringUtil.getActiveSuffix();
		Message message = new Message(topic, tag, body);
		if (deliverTime != null) {
			message.setStartDeliverTime(deliverTime);
		}
		if (messageKey != null) {
			message.setKey(messageKey);
		}
		if (MessageSendType.SEND.equals(sendType)) {
			this.build(groupId).send(message);
		} else if (MessageSendType.SEND_ASYNC.equals(sendType)) {
			this.build(groupId).sendAsync(message, callback);
		} else if (MessageSendType.SEND_ONE_WAY.equals(sendType)) {
			this.build(groupId).sendOneway(message);
		}
	}

	/**
	 * 同步发送
	 *
	 * @param groupId     组id
	 * @param topic       主题
	 * @param tag         标签
	 * @param target      目标
	 * @param messageKey  消息键
	 * @param deliverTime 交付时间
	 */
	public void sendSync(String groupId, String topic, String tag, Object target, String messageKey, Long deliverTime) {
		this.sendCommon(groupId, topic, tag, target, messageKey, deliverTime, MessageSendType.SEND, null);
	}

	/**
	 * 同步发送 随机生成 UUID messageKey
	 *
	 * @param groupId     组id
	 * @param topic       主题
	 * @param tag         标签
	 * @param target      目标
	 * @param deliverTime 交付时间
	 */
	public void sendSyncUUID(String groupId, String topic, String tag, Object target, Long deliverTime) {
		this.sendSync(groupId, topic, tag, target, UUID.randomUUID().toString(), deliverTime);
	}

	/**
	 * 同步发送 随机生成 UUID messageKey
	 *
	 * @param topic  主题
	 * @param tag    标签
	 * @param target 目标
	 */
	public void sendSyncUUID(String topic, String tag, Object target) {
		this.sendSyncUUID(DEFAULT_GROUP, topic, tag, target, null);
	}

	/**
	 * 异步发送
	 *
	 * @param groupId     组id
	 * @param topic       主题
	 * @param tag         标签
	 * @param target      目标
	 * @param messageKey  消息键
	 * @param deliverTime 交付时间
	 * @param callback    回调
	 */
	public void sendAsync(String groupId, String topic, String tag, Object target, String messageKey, Long deliverTime, SendCallback callback) {
		this.sendCommon(groupId, topic, tag, target, messageKey, deliverTime, MessageSendType.SEND_ASYNC, callback);
	}

	/**
	 * 异步发送 随机生成 UUID messageKey
	 *
	 * @param topic    主题
	 * @param tag      标签
	 * @param target   目标
	 * @param callback 回调
	 */
	public void sendAsyncUUID(String topic, String tag, Object target, SendCallback callback) {
		this.sendAsync(DEFAULT_GROUP, topic, tag, target, UUID.randomUUID().toString(), null, callback);
	}

	/**
	 * 单向发送
	 *
	 * @param groupId     组id
	 * @param topic       主题
	 * @param tag         标签
	 * @param target      目标
	 * @param messageKey  消息键
	 * @param deliverTime 交付时间
	 */
	public void sendOneWay(String groupId, String topic, String tag, Object target, String messageKey, Long deliverTime) {
		this.sendCommon(groupId, topic, tag, target, messageKey, deliverTime, MessageSendType.SEND_ONE_WAY, null);
	}

	/**
	 * 单向发送 随机生成 UUID messageKey
	 *
	 * @param topic  主题
	 * @param tag    标签
	 * @param target 目标
	 */
	public void sendOneWayUUID(String topic, String tag, Object target) {
		this.sendOneWay(DEFAULT_GROUP, topic, tag, target, UUID.randomUUID().toString(), null);
	}
}
