package com.hetan.rocket.base.helper;

import lombok.Data;

/**
 * 消息的附加属性
 *
 * @author 赵元昊
 * @date 2021/10/11 15:31
 **/
@Data
public class MessageAttributes {

	/**
	 * 消息类型
	 */
	String messageType;

	/**
	 * 业务类型
	 */
	String businessType;

	/**
	 * 组id
	 */
	String groupId;

	/**
	 * 主题
	 */
	String topic;

	/**
	 * 标签
	 */
	String tags;
}
