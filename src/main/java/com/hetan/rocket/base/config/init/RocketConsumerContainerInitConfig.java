package com.hetan.rocket.base.config.init;

import com.hetan.rocket.base.annotation.listener.RocketMessageListener;
import com.hetan.rocket.base.annotation.other.MessageSerializer;
import com.hetan.rocket.base.config.RocketProperties;
import com.hetan.rocket.base.serializer.DefaultSerializerClazz;
import com.hetan.rocket.base.support.container.AbstractConsumerContainer;
import com.hetan.rocket.base.support.AbstractConsumerContainerSupport;
import com.hetan.rocket.base.serializer.base.RocketSerializer;
import com.hetan.rocket.base.core.consumer.RocketConsumerListener;
import com.hetan.rocket.base.utils.AspectUtils;
import com.hetan.rocket.base.utils.ResolvePlaceholdersUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.scope.ScopedProxyUtils;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.config.BeanDefinitionCustomizer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.StandardEnvironment;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 消费者容器初始化配置
 *
 * @author 赵元昊
 * @date 2021/08/19 19:15
 */
@Slf4j
public class RocketConsumerContainerInitConfig implements ApplicationContextAware, SmartInitializingSingleton {
	private ApplicationContext applicationContext;
	private RocketProperties rocketProperties;
	private AtomicLong counter = new AtomicLong(0);
	private StandardEnvironment environment;
	private List<AbstractConsumerContainerSupport> containerSupports = new LinkedList<>();
	private ResolvePlaceholdersUtil resolvePlaceholdersUtil;

	/**
	 * 火箭消费者容器初始化配置
	 *
	 * @param rocketProperties 火箭的属性
	 * @param environment      环境信息
	 */
	public RocketConsumerContainerInitConfig(RocketProperties rocketProperties, StandardEnvironment environment) {
		this.rocketProperties = rocketProperties;
		this.environment = environment;
		this.resolvePlaceholdersUtil = new ResolvePlaceholdersUtil(environment);
	}

	@PostConstruct
	public void initialize() {

	}

	@Override
	public void setApplicationContext(@Nonnull ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	@Override
	public void afterSingletonsInstantiated() {
		Map<String, Object> beans = this.applicationContext.getBeansWithAnnotation(RocketMessageListener.class)
			.entrySet().stream().filter(entry -> !ScopedProxyUtils.isScopedTarget(entry.getKey()))
			.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

		// 获取所有的容器支持器
		this.containerSupports.addAll(this.applicationContext.getBeansOfType(AbstractConsumerContainerSupport.class)
			.values().stream().sorted(AbstractConsumerContainerSupport::compareTo).collect(Collectors.toList()));
		if (this.containerSupports.isEmpty()) {
			throw new IllegalStateException("未找到消费者容器提供者！");
		}
		// 初始化bean和监听器
		beans.forEach(this::registerContainer);
	}

	/**
	 * 注册容器并启动容器
	 *
	 * @param beanName bean的名字
	 * @param bean     豆
	 */
	private void registerContainer(String beanName, Object bean) {
		Class<?> clazz = AopProxyUtils.ultimateTargetClass(bean);
		if (!AspectUtils.checkClassAnySubOfInterface(clazz, RocketConsumerListener.class)) {
			throw new IllegalStateException(clazz + " is not instance of " + RocketConsumerListener.class.getName());
		}
		RocketMessageListener rocketMessage = clazz.getAnnotation(RocketMessageListener.class);
		MessageSerializer serializerAnnotation = bean.getClass().getAnnotation(MessageSerializer.class);
		Class<? extends RocketSerializer> clzSerializer = DefaultSerializerClazz.DEFAULT_SERIALIZER_CLAZZ;
		if (serializerAnnotation != null && serializerAnnotation.deSerializer() != null) {
			clzSerializer = serializerAnnotation.deSerializer();
		}
		// 获取各项配置
		RocketSerializer serializer = this.applicationContext.getBean(clzSerializer);
		String groupId = this.resolvePlaceholdersUtil.resolvePlaceholders(rocketMessage.exGroupID(), rocketMessage.groupID(), "请配置GroupID");
		String topic = this.resolvePlaceholdersUtil.resolvePlaceholders(rocketMessage.exTopic(), rocketMessage.topic(), "请配置Topic");
		String tag = this.resolvePlaceholdersUtil.resolvePlaceholders(rocketMessage.exTag(), rocketMessage.tag(), "请配置Tag");
		AbstractConsumerContainerSupport support = null;
		for (AbstractConsumerContainerSupport containerSupport : this.containerSupports) {
			if (containerSupport.isSupport(this.rocketProperties, rocketMessage, serializer,
				(Class<? extends RocketConsumerListener<?, ?>>) clazz, rocketMessage.messageType(),
				rocketMessage.returnType(), (RocketConsumerListener<?, ?>) bean, groupId, topic, tag)) {
				support = containerSupport;
				break;
			}
		}
		if (support == null) {
			throw new IllegalStateException("未找到受支持的提供者！");
		}
		AbstractConsumerContainer<?, ?> container = support.generateContainer(this.rocketProperties, rocketMessage, serializer,
			(Class<? extends RocketConsumerListener<?, ?>>) clazz, rocketMessage.messageType(),
			rocketMessage.returnType(), (RocketConsumerListener<?, ?>) bean, groupId, topic, tag);
		String registerContainerName = String.format("%s_%s_%s", container.getClass().getName(), clazz.getName(), counter.getAndIncrement());
		GenericApplicationContext genericApplicationContext = (GenericApplicationContext) this.applicationContext;
		log.info("消费者容器:{}开始注册,Bean名称:{},代理的处理类:{},GroupID:{},Topic:{},Tag:{},MessageModel:{},ConsumerModel:{}",
			container.getClass().getName(), registerContainerName, clazz.getName(), groupId, topic, tag,
			rocketMessage.messageModel(), rocketMessage.consumerModel());
		try {
			Method registerBean = GenericApplicationContext.class.getMethod("registerBean", String.class, Class.class, Supplier.class, BeanDefinitionCustomizer[].class);
			registerBean.invoke(genericApplicationContext, registerContainerName, container.getClass(), (Supplier<? extends AbstractConsumerContainer>) () -> container, new BeanDefinitionCustomizer[]{});
		} catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ignored) {
			ignored.printStackTrace();
		}
		if (container != null) {
			if (!container.isRunning()) {
				try {
					log.info("消费者容器:{}开始启动,Bean名称:{},代理的处理类:{},GroupID:{},Topic:{},Tag:{},MessageModel:{},ConsumerModel:{}",
						container.getClass().getName(), registerContainerName, clazz.getName(), groupId, topic, tag,
						rocketMessage.messageModel(), rocketMessage.consumerModel());
					// 启动容器
					container.start();
					log.info("消费者容器:{}启动成功,Bean名称:{},代理的处理类:{},GroupID:{},Topic:{},Tag:{},MessageModel:{},ConsumerModel:{}",
						container.getClass().getName(), registerContainerName, clazz.getName(), groupId, topic, tag,
						rocketMessage.messageModel(), rocketMessage.consumerModel());
				} catch (Exception e) {
					log.error("消费者容器:{}启动失败,失败原因:{}", clazz.getName(), e.getMessage());
					e.printStackTrace();
				}
			}
		} else {
			log.error("消费者容器:{}未生成,Bean名称:{},代理的处理类:{},GroupID:{},Topic:{},Tag:{},MessageModel:{},ConsumerModel:{}",
				null, registerContainerName, clazz.getName(), groupId, topic, tag,
				rocketMessage.messageModel(), rocketMessage.consumerModel());
		}
	}


}
