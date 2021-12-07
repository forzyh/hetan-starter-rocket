package com.hetan.rocket.base.utils;

import com.hetan.rocket.base.config.RocketProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.lang.Nullable;
import org.springframework.util.ObjectUtils;

/**
 * spring 工具类
 *
 * @author 赵元昊
 * @date 2021/08/17 19:15
 */
@Slf4j
public class RocketSpringUtil implements ApplicationContextAware {

	private static ApplicationContext context;

	@Override
	public void setApplicationContext(@Nullable ApplicationContext context) throws BeansException {
		RocketSpringUtil.context = context;
	}

	/**
	 * 获取bean
	 *
	 * @param clazz class类
	 * @param <T>   泛型
	 * @return T
	 */
	public static <T> T getBean(Class<T> clazz) {
		if (clazz == null) {
			return null;
		}
		return context.getBean(clazz);
	}

	/**
	 * 获取bean
	 *
	 * @param beanId beanId
	 * @param <T>    泛型
	 * @return T
	 */
	public static <T> T getBean(String beanId) {
		if (beanId == null) {
			return null;
		}
		return (T) context.getBean(beanId);
	}

	/**
	 * 获取bean
	 *
	 * @param beanName bean名称
	 * @param clazz    class类
	 * @param <T>      泛型
	 * @return T
	 */
	public static <T> T getBean(String beanName, Class<T> clazz) {
		if (null == beanName || "".equals(beanName.trim())) {
			return null;
		}
		if (clazz == null) {
			return null;
		}
		return (T) context.getBean(beanName, clazz);
	}

	/**
	 * 获取 ApplicationContext
	 *
	 * @return ApplicationContext
	 */
	public static ApplicationContext getContext() {
		if (context == null) {
			return null;
		}
		return context;
	}

	/**
	 * 发布事件
	 *
	 * @param event 事件
	 */
	public static void publishEvent(ApplicationEvent event) {
		if (context == null) {
			return;
		}
		try {
			context.publishEvent(event);
		} catch (Exception ex) {
			log.error(ex.getMessage());
		}
	}

	/**
	 * 得到环境信息
	 *
	 * @return {@link String}
	 */
	public static String getActive() {
		String property = System.getProperty("spring.profiles.active");
		if (property != null) {
			property = property.toUpperCase();
		}
		return property;
	}

	/**
	 * 得到环境后缀
	 *
	 * @return {@link String}
	 */
	public static String getActiveSuffix() {
		RocketProperties rocketProperties = getBean(RocketProperties.class);
		if (!ObjectUtils.isEmpty(rocketProperties.getSuffix())) {
			return "_" + rocketProperties.getSuffix().toUpperCase();
		}
		String active = getActive();
		if (rocketProperties.getEnabledSuffixList().contains(active)) {
			return "_" + active;
		}
		return "";
	}

}
