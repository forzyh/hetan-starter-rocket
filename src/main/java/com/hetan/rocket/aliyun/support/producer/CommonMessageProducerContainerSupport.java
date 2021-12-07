package com.hetan.rocket.aliyun.support.producer;

import com.aliyun.openservices.ons.api.SendCallback;
import com.hetan.rocket.aliyun.annotation.send.CommonMessage;
import com.hetan.rocket.aliyun.support.AliRocketMqTemplate;
import com.hetan.rocket.aliyun.support.producer.enums.MessageSendType;
import com.hetan.rocket.base.annotation.send.RocketMessageSend;
import com.hetan.rocket.base.config.RocketProperties;
import com.hetan.rocket.base.helper.MessageAttributes;
import com.hetan.rocket.base.support.AbstractProducerContainerSupport;
import com.hetan.rocket.base.utils.ResolvePlaceholdersUtil;
import com.hetan.rocket.base.utils.RocketSpringUtil;
import com.hetan.rocket.base.utils.RocketThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;

/**
 * 常用消息提供者支持器
 *
 * @author 赵元昊
 * @date 2021/08/17 17:35
 **/
@Slf4j
public class CommonMessageProducerContainerSupport extends AbstractProducerContainerSupport {

	private final static List<Class<? extends Annotation>> SUPPORT_CLASS = Collections.singletonList(CommonMessage.class);

	private final AliRocketMqTemplate aliRocketMqTemplate;

	public CommonMessageProducerContainerSupport(AliRocketMqTemplate aliRocketMqTemplate) {
		this.aliRocketMqTemplate = aliRocketMqTemplate;
	}

	@Override
	public List<Class<? extends Annotation>> supportAnnotation() {
		return SUPPORT_CLASS;
	}

	@Override
	public void send(RocketMessageSend rocketMessage, String groupId, Annotation annotation, RocketProperties rocketProperties, Long deliverTime, byte[] body) {
		if (annotation instanceof CommonMessage) {
			CommonMessage commonMessage = (CommonMessage) annotation;
			MessageSendType sendType = commonMessage.messageSendType();

			SendCallback callback = null;
			if (MessageSendType.SEND_ASYNC.equals(sendType)) {
				callback = RocketSpringUtil.getBean(commonMessage.callback());
				if (callback == null) {
					log.error("未找到所需要的CallbackBean:{}", commonMessage.callback());
					throw new RuntimeException("请配置回调实体!");
				}
			}
			this.aliRocketMqTemplate.build(groupId, rocketProperties);
			ResolvePlaceholdersUtil resolvePlaceholdersUtil = RocketSpringUtil.getBean(ResolvePlaceholdersUtil.class);
			String topic = resolvePlaceholdersUtil.resolvePlaceholders(commonMessage.exTopic(), commonMessage.topic(), "请配置Topic");
			String tag = resolvePlaceholdersUtil.resolvePlaceholders(commonMessage.exTag(), commonMessage.tag(), "请配置Tag");
			String messageKey = StringUtils.isEmpty(RocketThreadLocalUtil.getMessageKey()) ? "" : RocketThreadLocalUtil.getMessageKey();
			this.aliRocketMqTemplate.sendCommon(groupId, topic, tag, body, messageKey, deliverTime, sendType, callback);
		}
	}

	@Override
	public MessageAttributes getAttributes(RocketMessageSend rocketMessage, String groupId, Annotation annotation, RocketProperties rocketProperties, Long deliverTime) {
		MessageAttributes messageAttributes = new MessageAttributes();
		messageAttributes.setGroupId(groupId);
		if (annotation instanceof CommonMessage) {
			CommonMessage commonMessage = (CommonMessage) annotation;
			messageAttributes.setMessageType(commonMessage.messageSendType().getName());
			ResolvePlaceholdersUtil resolvePlaceholdersUtil = RocketSpringUtil.getBean(ResolvePlaceholdersUtil.class);
			String topic = resolvePlaceholdersUtil.resolvePlaceholders(commonMessage.exTopic(), commonMessage.topic());
			String tag = resolvePlaceholdersUtil.resolvePlaceholders(commonMessage.exTag(), commonMessage.tag());
			messageAttributes.setTags(tag);
			messageAttributes.setTopic(topic);
		}
		return messageAttributes;
	}
}
