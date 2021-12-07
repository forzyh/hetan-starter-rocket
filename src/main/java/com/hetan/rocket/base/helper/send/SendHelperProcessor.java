package com.hetan.rocket.base.helper.send;


import com.hetan.rocket.base.helper.MessageAttributes;

/**
 * 发送辅助处理器
 *
 * @author 赵元昊
 * @date 2021/10/09 11:58
 **/
public interface SendHelperProcessor {

	/**
	 * 成功保存
	 *
	 * @param messageKey 消息键
	 * @param attributes 属性
	 * @param message    消息
	 */
	void onSuccessSave(String messageKey, MessageAttributes attributes, Object message);

	/**
	 * 失败保存
	 *
	 * @param messageKey 消息键
	 * @param attributes 属性
	 * @param message    消息
	 */
	void onFailSave(String messageKey, MessageAttributes attributes, Object message,Exception e);

	/**
	 * 失败通知
	 *
	 * @param messageKey 消息键
	 * @param attributes 属性
	 * @param message    消息
	 */
	void onFailNotify(String messageKey, MessageAttributes attributes, Object message,Exception e);

}
