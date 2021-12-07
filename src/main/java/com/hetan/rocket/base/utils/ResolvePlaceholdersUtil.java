package com.hetan.rocket.base.utils;

import org.springframework.core.env.StandardEnvironment;

/**
 * 解析环境配置信息的工具类
 *
 * @author 赵元昊
 * @date 2021/08/19 01:30
 **/
public class ResolvePlaceholdersUtil {

	private StandardEnvironment environment;

	public ResolvePlaceholdersUtil(StandardEnvironment environment) {
		this.environment = environment;
	}

	/**
	 * 解析环境配置信息 如果环境信息有值 会覆写默认值
	 *
	 * @param key          关键
	 * @param defaultValue 默认值
	 * @return {@link String}
	 */
	public String resolvePlaceholders(String key, String defaultValue) {
		return this.resolvePlaceholders(key, defaultValue, false, null);
	}

	/**
	 * 解析环境配置信息 如果环境信息有值 会覆写默认值
	 *
	 * @param key          关键
	 * @param defaultValue 默认值
	 * @param message      消息
	 * @return {@link String}
	 */
	public String resolvePlaceholders(String key, String defaultValue, String message) {
		return this.resolvePlaceholders(key, defaultValue, true, message);
	}

	/**
	 * 解析环境配置信息 如果环境信息有值 会覆写默认值
	 *
	 * @param key          环境占位符 例如：${spring.port} 会获取配置信息
	 * @param defaultValue 默认值
	 * @param isNotEmpty   是否不可为空 如果传入true 但最后结果为空 会抛出message中的异常信息
	 * @param message      异常信息
	 * @return {@link String}
	 */
	public String resolvePlaceholders(String key, String defaultValue, boolean isNotEmpty, String message) {
		String value = defaultValue;
		String placeholders = this.environment.resolvePlaceholders(key);
		if (!key.equals(placeholders) && !"".equals(placeholders)) {
			value = placeholders;
		}
		if (isNotEmpty) {
			if (!(value != null && !"".equals(value))) {
				throw new IllegalStateException(message);
			}
		}
		return value;
	}
}
