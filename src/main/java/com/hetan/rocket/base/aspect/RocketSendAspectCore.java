package com.hetan.rocket.base.aspect;

import com.hetan.rocket.base.annotation.other.AutoMessageKey;
import com.hetan.rocket.base.annotation.other.MessageSerializer;
import com.hetan.rocket.base.annotation.send.RocketMessageSend;
import com.hetan.rocket.base.config.init.RocketProducerContainerInitConfig;
import com.hetan.rocket.base.helper.MessageAttributes;
import com.hetan.rocket.base.helper.send.SendHelper;
import com.hetan.rocket.base.helper.send.SendHelperProcessor;
import com.hetan.rocket.base.helper.util.ProcessorUtil;
import com.hetan.rocket.base.serializer.DefaultSerializerClazz;
import com.hetan.rocket.base.serializer.base.RocketSerializer;
import com.hetan.rocket.base.support.AbstractProducerContainerSupport;
import com.hetan.rocket.base.utils.*;
import com.hetan.rocket.base.config.RocketProperties;
import com.sinzetech.rocket.base.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.core.env.StandardEnvironment;

import java.lang.annotation.Annotation;
import java.util.*;

/**
 * 切面核心方法
 *
 * @author 赵元昊
 * @date 2021/08/17 11:01
 **/
@Slf4j
public abstract class RocketSendAspectCore {

	/**
	 * 切面核心
	 *
	 * @param rocketProducerContainerInitConfig 火箭生产商容器初始化配置
	 * @param rocketProperties                  火箭的属性
	 * @param environment                       环境
	 */
	public RocketSendAspectCore(RocketProducerContainerInitConfig rocketProducerContainerInitConfig,
                                RocketProperties rocketProperties,
                                StandardEnvironment environment) {
		this.rocketProducerContainerInitConfig = rocketProducerContainerInitConfig;
		this.rocketProperties = rocketProperties;
		this.standardEnvironment = environment;
		this.resolvePlaceholdersUtil = new ResolvePlaceholdersUtil(environment);
	}

	protected final RocketProducerContainerInitConfig rocketProducerContainerInitConfig;
	protected final RocketProperties rocketProperties;
	protected final StandardEnvironment standardEnvironment;
	protected final ResolvePlaceholdersUtil resolvePlaceholdersUtil;

	public Object rockerMessageSend(ProceedingJoinPoint point) throws Throwable {
		try {
			Annotation[] annotations = AspectUtils.getAnnotations(point);
			Map<Annotation, AbstractProducerContainerSupport> supportMap = new HashMap<>(annotations.length);
			for (Annotation annotation : annotations) {
				// 获取容器支持器 支持多注解 发送多消息
				AbstractProducerContainerSupport support = rocketProducerContainerInitConfig.getSupport(annotation);
				if (support != null) {
					supportMap.put(annotation, support);
				}
			}
			RocketMessageSend rocketMessageAnnotation = AspectUtils.getDeclaringClassAnnotation(point, RocketMessageSend.class);
			if (rocketMessageAnnotation == null) {
				if (!supportMap.isEmpty()) {
					log.error("请检查调用的该类中是否标记RocketMessage注解！非安全调用已被终止！调用的类为：{},调用的方法为：{}!",
						point.getTarget().getClass().getSimpleName(),
						AspectUtils.getMethod(point).getName());
					throw new IllegalAccessException("非安全调用已被终止!");
				}
			}

			SendHelper sendHelper = AspectUtils.getAnnotation(point, SendHelper.class);
			if (sendHelper == null) {
				sendHelper = AspectUtils.getDeclaringClassAnnotation(point, SendHelper.class);
			}
			List<SendHelperProcessor> sendHelperProcessor = Collections.emptyList();
			if (sendHelper != null) {
				sendHelperProcessor = ProcessorUtil.getSendHelperProcessors(sendHelper, rocketProperties);
			}
			RocketSerializer serializer = getRocketSerializer(point);
			log.info("RocketMQ消息方法调用开始，调用的类为：{},调用的方法为：{}", point.getTarget().getClass().getSimpleName(), AspectUtils.getMethod(point).getName());
			Object proceed = point.proceed();
			byte[] body = serializer.serialize(proceed);
			Long startDeliverTime = StartDeliverTimeFactory.getStartDeliverTime(point.getArgs(), AspectUtils.getParams(point));
			AutoMessageKey messageKey = AspectUtils.getAnnotation(point, AutoMessageKey.class);
			if (messageKey == null) {
				messageKey = AspectUtils.getClassAnnotation(point, AutoMessageKey.class);
			}
			if (messageKey != null) {
				if (Objects.isNull(RocketThreadLocalUtil.getMessageKey())) {
					RocketThreadLocalUtil.setMessageKey(UUID.randomUUID().toString());
				}
			}
			String groupId = resolvePlaceholdersUtil.resolvePlaceholders(rocketMessageAnnotation.exGroupID(), rocketMessageAnnotation.groupID());
			for (Map.Entry<Annotation, AbstractProducerContainerSupport> producerSupportEntry : supportMap.entrySet()) {
				AbstractProducerContainerSupport producerSupport = producerSupportEntry.getValue();
				Annotation annotation = producerSupportEntry.getKey();
				log.debug("RocketMQ消息开始发送，调用的类为：{},调用的方法为：{},消息发送Support为：{},消息注解为：{},发送的消息内容为：{}",
					point.getTarget().getClass().getSimpleName(),
					AspectUtils.getMethod(point).getName(),
					producerSupport.getClass().getName(),
					annotation.annotationType(),
					proceed);
				MessageAttributes attributes = producerSupport.getAttributes(rocketMessageAnnotation, groupId, annotation, rocketProperties, startDeliverTime);
				try {
					producerSupport.send(rocketMessageAnnotation,
						groupId,
						annotation,
						rocketProperties,
						startDeliverTime,
						body);
					if (sendHelper != null && !sendHelperProcessor.isEmpty() && sendHelper.successSave()) {
						sendHelperProcessor.forEach(processor -> {
							try {
								processor.onSuccessSave(RocketThreadLocalUtil.getMessageKey(), attributes, proceed);
							} catch (Exception pe) {
								pe.printStackTrace();
							}
						});
					}
				} catch (Exception e) {
					log.error("RocketMQ消息发送调用失败，调用的类为：{},调用的方法为：{},消息发送Support为：{},消息注解为：{},发送的消息内容为：{}，错误内容为：{}",
						point.getTarget().getClass().getSimpleName(),
						AspectUtils.getMethod(point).getName(),
						producerSupport.getClass().getName(),
						annotation.annotationType(),
						proceed,
						e.getMessage());
					if (sendHelper != null && !sendHelperProcessor.isEmpty() && sendHelper.failSave()) {
						sendHelperProcessor.forEach(processor -> {
							try {
								processor.onFailSave(RocketThreadLocalUtil.getMessageKey(), attributes, proceed, e);
							} catch (Exception pe) {
								pe.printStackTrace();
							}
						});
					}
					if (sendHelper != null && !sendHelperProcessor.isEmpty() && sendHelper.failNotify()) {
						sendHelperProcessor.forEach(processor -> {
							try {
								processor.onFailNotify(RocketThreadLocalUtil.getMessageKey(), attributes, proceed, e);
							} catch (Exception pe) {
								pe.printStackTrace();
							}
						});
					}
					throw e;
				}
				log.info("RocketMQ消息发送调用成功，调用的类为：{},调用的方法为：{},消息发送Support为：{},消息注解为：{},发送的消息内容为：{}",
					point.getTarget().getClass().getSimpleName(),
					AspectUtils.getMethod(point).getName(),
					producerSupport.getClass().getName(),
					annotation.annotationType(),
					proceed);
			}
			return proceed;
		} finally {
			RocketThreadLocalUtil.clearAll();
		}
	}

	/**
	 * 获取序列化器 方法上的序列化器优先使用
	 *
	 * @param point 点
	 * @return {@link RocketSerializer}
	 * @throws IllegalAccessException 非法访问异常
	 */
	protected RocketSerializer getRocketSerializer(ProceedingJoinPoint point) throws IllegalAccessException {
		Class<? extends RocketSerializer> serializerClass = DefaultSerializerClazz.DEFAULT_SERIALIZER_CLAZZ;
		MessageSerializer classMessageSerializer = AspectUtils.getClassAnnotation(point, MessageSerializer.class);
		MessageSerializer methodMessageSerializer = AspectUtils.getAnnotation(point, MessageSerializer.class);
		if (classMessageSerializer != null && classMessageSerializer.serializer() != null) {
			serializerClass = classMessageSerializer.serializer();
		}
		if (methodMessageSerializer != null && methodMessageSerializer.serializer() != null) {
			serializerClass = methodMessageSerializer.serializer();
		}
		RocketSerializer serializer = RocketSpringUtil.getBean(serializerClass);
		if (serializer != null) {
			return serializer;
		}
		log.error("未找到配置的序列化器{}!调用已被终止！", serializerClass.getName());
		throw new IllegalAccessException("未找到配置的序列化器!");
	}

}
