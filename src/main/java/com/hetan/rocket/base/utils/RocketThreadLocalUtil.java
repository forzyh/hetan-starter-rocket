package com.hetan.rocket.base.utils;

import com.alibaba.ttl.TransmittableThreadLocal;

import java.util.*;

/**
 * RocketThreadLocalUtil
 *
 * @author 赵元昊
 * @date 2021/08/21 16:03
 */
@SuppressWarnings("unchecked")
public class RocketThreadLocalUtil {
	static TransmittableThreadLocal<List<Object>> ARGS_LOCAL = new TransmittableThreadLocal<List<Object>>() {
		@Override
		protected List<Object> initialValue() {
			return new ArrayList<>(0);
		}
	};

	static TransmittableThreadLocal<String> KEY_LOCAL = new TransmittableThreadLocal();

	static TransmittableThreadLocal<List<String>> BATCH_KEY_LOCAL = new TransmittableThreadLocal<List<String>>() {
		@Override
		protected List<String> initialValue() {
			return new ArrayList<>(0);
		}
	};

	/**
	 * 设置消息键
	 *
	 * @param key 关键
	 */
	public static void setMessageKey(String key) {
		KEY_LOCAL.set(key);
	}

	/**
	 * 得到消息键
	 *
	 * @return {@link String}
	 */
	public static String getMessageKey() {
		return KEY_LOCAL.get();
	}

	/**
	 * 得到一批消息键
	 *
	 * @return {@link List}<{@link String}>
	 */
	public static List<String> getBatchMessageKeys() {
		return BATCH_KEY_LOCAL.get();
	}

	/**
	 * 得到一批消息键
	 *
	 * @return {@link List}<{@link String}>
	 */
	public static String getBatchMessageKeys(Integer index) {
		return BATCH_KEY_LOCAL.get().get(index);
	}

	/**
	 * 内部方法 批处理消息键集
	 *
	 * @param batchMessageKeys 批处理消息键
	 */
	@Deprecated
	public static void setBatchMessageKeys(List<String> batchMessageKeys) {
		if (batchMessageKeys != null) {
			BATCH_KEY_LOCAL.get().addAll(batchMessageKeys);
		}
	}

	/**
	 * @return threadLocal中的全部值
	 */
	public static Object[] getAllArgs() {
		return ARGS_LOCAL.get().toArray();
	}


	/**
	 * 添加对象数组到ThreadLocal里面
	 *
	 * @param args 对象参数
	 */
	public static void addArgs(Object... args) {
		if (args != null && args.length > 0) {
			for (Object arg : args) {
				ARGS_LOCAL.get().add(arg);
			}
		}
	}

	/**
	 * 清理参数
	 */
	public static void clearArgs() {
		ARGS_LOCAL.remove();
	}

	/**
	 * 清理message关键
	 */
	public static void clearMessageKey() {
		KEY_LOCAL.remove();
	}

	/**
	 * 清理message关键
	 */
	public static void clearBatchMessageKey() {
		BATCH_KEY_LOCAL.remove();
	}

	/**
	 * 清空ThreadLocal
	 *
	 * @see Map#clear()
	 */
	public static void clearAll() {
		ARGS_LOCAL.remove();
		KEY_LOCAL.remove();
		BATCH_KEY_LOCAL.remove();
	}
}
