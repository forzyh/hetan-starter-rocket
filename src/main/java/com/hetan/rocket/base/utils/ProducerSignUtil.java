package com.hetan.rocket.base.utils;

/**
 * 签名util
 *
 * @author 赵元昊
 * @date 2021/08/17 18:12
 **/
public class ProducerSignUtil {

	/**
	 * 生成签名
	 *
	 * @param groupId 组id
	 * @param topic   主题
	 * @param tag     标签
	 * @return {@link String}
	 */
	public static String genSign(String groupId, String topic, String tag) {
		return "$groupId:[" + groupId + "]$topic:[" + topic + "]$tag[" + tag + "]";
	}
}
