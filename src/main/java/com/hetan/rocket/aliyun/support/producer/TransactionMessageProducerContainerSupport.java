package com.hetan.rocket.aliyun.support.producer;

import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.ONSFactory;
import com.aliyun.openservices.ons.api.transaction.LocalTransactionChecker;
import com.aliyun.openservices.ons.api.transaction.LocalTransactionExecuter;
import com.aliyun.openservices.ons.api.transaction.TransactionProducer;
import com.hetan.rocket.aliyun.annotation.send.TransactionMessage;
import com.hetan.rocket.aliyun.util.PropertiesFactory;
import com.hetan.rocket.base.annotation.send.RocketMessageSend;
import com.hetan.rocket.base.config.RocketProperties;
import com.hetan.rocket.base.error.RocketAccessException;
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
 * 事务消息提供者支持器
 *
 * @author 赵元昊
 * @date 2021/08/17 17:35
 **/
@Slf4j
public class TransactionMessageProducerContainerSupport extends AbstractProducerContainerSupport {

	private final static List<Class<? extends Annotation>> SUPPORT_CLASS = Collections.singletonList(TransactionMessage.class);

	private final Object lock = new Object();

	/**
	 * 提供者缓存
	 */
	public volatile Map<String, TransactionProducer> producerMap = new ConcurrentHashMap<>();

	@Override
	public List<Class<? extends Annotation>> supportAnnotation() {
		return SUPPORT_CLASS;
	}

	@Override
	public void send(RocketMessageSend rocketMessage, String groupId, Annotation annotation, RocketProperties rocketProperties, Long deliverTime, byte[] body) {
		if (annotation instanceof TransactionMessage) {
			TransactionMessage transactionMessage = (TransactionMessage) annotation;
			if (transactionMessage.checker() == null || transactionMessage.executer() == null) {
				log.error("事务消息未配置回查事务实例！");
				RocketAccessException.throwException("事务消息未配置回查事务实例！");
			}
			LocalTransactionChecker checker = RocketSpringUtil.getBean(transactionMessage.checker());
			if (checker == null) {
				log.error("未找到本地事务消息{}回查实例！", transactionMessage.checker().getName());
				RocketAccessException.throwException("未找到本地事务消息回查实例!");
			}
			LocalTransactionExecuter execute = RocketSpringUtil.getBean(transactionMessage.executer());
			if (execute == null) {
				log.error("未找到本地事务执行器{}实例！", transactionMessage.executer().getName());
				RocketAccessException.throwException("未找到本地事务执行器实例!");
			}
			groupId += RocketSpringUtil.getActiveSuffix();
			String sign = ProducerSignUtil.genSign(groupId, null, null);
			sign = sign.concat(String.format("$checker[%s]", transactionMessage.checker().getName()));
			TransactionProducer producer;
			if (producerMap.containsKey(sign)) {
				producer = producerMap.get(sign);
			} else {
				synchronized (lock) {
					if (producerMap.containsKey(sign)) {
						producer = producerMap.get(sign);
					} else {
						Properties producerProperties = PropertiesFactory.createProducerProperties(rocketProperties, groupId);
						producer = ONSFactory.createTransactionProducer(producerProperties, checker);
						producer.start();
						producerMap.put(sign, producer);
					}
				}
			}
			ResolvePlaceholdersUtil resolvePlaceholdersUtil = RocketSpringUtil.getBean(ResolvePlaceholdersUtil.class);
			String topic = resolvePlaceholdersUtil.resolvePlaceholders(transactionMessage.exTopic(), transactionMessage.topic(), "请配置Topic");
			String tag = resolvePlaceholdersUtil.resolvePlaceholders(transactionMessage.exTag(), transactionMessage.tag(), "请配置Tag");
			topic += RocketSpringUtil.getActiveSuffix();
			Message message = new Message(topic, tag, body);
			if (deliverTime != null) {
				message.setStartDeliverTime(deliverTime);
			}
			message.setKey(StringUtils.isEmpty(RocketThreadLocalUtil.getMessageKey()) ? "" : RocketThreadLocalUtil.getMessageKey());
			producer.send(message, execute, RocketThreadLocalUtil.getAllArgs());
		}
	}

	@Override
	public MessageAttributes getAttributes(RocketMessageSend rocketMessage, String groupId, Annotation annotation, RocketProperties rocketProperties, Long deliverTime) {
		MessageAttributes messageAttributes = new MessageAttributes();
		messageAttributes.setGroupId(groupId);
		messageAttributes.setMessageType("transaction_send");
		if (annotation instanceof TransactionMessage) {
			TransactionMessage transactionMessage = (TransactionMessage) annotation;
			ResolvePlaceholdersUtil resolvePlaceholdersUtil = RocketSpringUtil.getBean(ResolvePlaceholdersUtil.class);
			String topic = resolvePlaceholdersUtil.resolvePlaceholders(transactionMessage.exTopic(), transactionMessage.topic());
			String tag = resolvePlaceholdersUtil.resolvePlaceholders(transactionMessage.exTag(), transactionMessage.tag());
			messageAttributes.setTags(tag);
			messageAttributes.setTopic(topic);
		}
		return messageAttributes;
	}
}
