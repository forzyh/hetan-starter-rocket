package com.hetan.rocket.base.utils;

import com.hetan.rocket.base.annotation.other.DeliverTime;

import java.lang.reflect.Parameter;
import java.util.Objects;

/**
 * 延时时间处理工具类
 *
 * @author 赵元昊
 * @date 2021/08/23 10:54
 */
public class StartDeliverTimeFactory {

	private StartDeliverTimeFactory() {
	}

	/**
	 * 解析定时投放的时延
	 *
	 * @param args   arg游戏
	 * @param params 参数个数
	 * @return {@link Long}
	 */
	public static Long getStartDeliverTime(Object[] args, Parameter[] params) {
		for (int i = 0; i < args.length; i++) {
			DeliverTime annotation = params[i].getAnnotation(DeliverTime.class);
			if (!Objects.isNull(annotation)) {
				if (annotation.timeStampModel()) {
					return (Long) args[i];
				} else {
					return System.currentTimeMillis() + (Long) args[i];
				}
			}
		}
		return null;
	}
}
