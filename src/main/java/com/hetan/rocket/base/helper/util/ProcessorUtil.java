package com.hetan.rocket.base.helper.util;

import com.hetan.rocket.base.helper.send.SendHelper;
import com.hetan.rocket.base.helper.send.SendHelperProcessor;
import com.hetan.rocket.base.config.RocketProperties;
import com.hetan.rocket.base.error.RocketAccessException;
import com.hetan.rocket.base.helper.listener.ListenerHelper;
import com.hetan.rocket.base.helper.listener.ListenerHelperProcessor;
import com.hetan.rocket.base.utils.RocketSpringUtil;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 赵元昊
 * @date 2021/10/12 11:45
 **/
public class ProcessorUtil {

	private static final Map<String, Class<?>> CLASS_CACHE = new ConcurrentHashMap<>();

	/**
	 * 得到处理器
	 *
	 * @param processorClass      处理器类
	 * @param defaultProcessClass 默认过程类
	 * @return {@link List}<{@link T}>
	 */
	public static <T> List<T> getProcessor(Class<? extends T>[] processorClass, String defaultProcessClass) {
		List<T> processors = new ArrayList<>();
		if (ObjectUtils.isEmpty(processorClass)) {
			if (!ObjectUtils.isEmpty(defaultProcessClass)) {
				try {
					Class<? extends T> aClass;
					if (CLASS_CACHE.containsKey(defaultProcessClass)) {
						aClass = (Class<? extends T>) CLASS_CACHE.get(defaultProcessClass);
					} else {
						aClass = (Class<? extends T>) RocketSpringUtil.getContext().getClassLoader().loadClass(defaultProcessClass);
						CLASS_CACHE.put(defaultProcessClass, aClass);
					}
					T helperProcessor = RocketSpringUtil.getBean(aClass);
					if (helperProcessor != null) {
						processors.add(helperProcessor);
					}
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
		} else {
			for (Class<? extends T> aClass : processorClass) {
				T bean = RocketSpringUtil.getBean(aClass);
				if (bean == null) {
					throw new RocketAccessException("未找到名称为：[" + aClass.getName() + "]的bean");
				}
				processors.add(bean);
			}
		}
		if (processors.isEmpty()) {
			throw new RocketAccessException("未找到有效的执行bean");
		}
		return processors;
	}

	/**
	 * 让听众辅助处理器
	 *
	 * @param listenerHelper   侦听器辅助
	 * @param rocketProperties 火箭的属性
	 * @return {@link List}<{@link ListenerHelperProcessor}>
	 */
	public static List<ListenerHelperProcessor> getListenerHelperProcessors(ListenerHelper listenerHelper, RocketProperties rocketProperties) {
		if (listenerHelper != null) {
			return getProcessor(listenerHelper.processor(), rocketProperties.getDefaultListenerHelper());
		} else {
			return Collections.emptyList();
		}
	}

	/**
	 * 得到发送辅助处理器
	 *
	 * @param sendHelper       派助手
	 * @param rocketProperties 火箭的属性
	 * @return {@link List}<{@link SendHelperProcessor}>
	 */
	public static List<SendHelperProcessor> getSendHelperProcessors(SendHelper sendHelper, RocketProperties rocketProperties) {
		if (sendHelper != null) {
			return getProcessor(sendHelper.processor(), rocketProperties.getDefaultSendHelper());
		} else {
			return Collections.emptyList();
		}
	}
}
