package com.hetan.rocket.base.config.init;

import com.hetan.rocket.base.support.AbstractProducerContainerSupport;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 提供者容器支持器初始化
 *
 * @author 赵元昊
 * @date 2021/08/17 10:54
 */
public class RocketProducerContainerInitConfig implements ApplicationContextAware {
	private ApplicationContext applicationContext;
	private List<AbstractProducerContainerSupport> producerSupports;
	private Set<Class<? extends Annotation>> supportAnnotation = new HashSet<>();

	@PostConstruct
	public void initialize() {
		// 检索所有的消费者容器支持者
		Map<String, AbstractProducerContainerSupport> producerSupportMap = applicationContext.getBeansOfType(AbstractProducerContainerSupport.class);
		// 根据Order进行排序
		producerSupports = producerSupportMap.values().stream().sorted(AbstractProducerContainerSupport::compareTo).collect(Collectors.toList());

		//缓存筛选 用于只筛选对应的注解
		for (AbstractProducerContainerSupport producerSupport : producerSupports) {
			supportAnnotation.addAll(producerSupport.supportAnnotation() == null ? Collections.emptySet() : producerSupport.supportAnnotation());
		}
	}

	/**
	 * 获取支持器 可能为null
	 *
	 * @param annotation 注释
	 * @return {@link AbstractProducerContainerSupport}
	 */
	public AbstractProducerContainerSupport getSupport(Annotation annotation) {
		for (AbstractProducerContainerSupport producerSupport : producerSupports) {
			if (producerSupport.isSupport(annotation)) {
				return producerSupport;
			}
		}
		return null;
	}

	@Override
	public void setApplicationContext(@Nonnull ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}
}
