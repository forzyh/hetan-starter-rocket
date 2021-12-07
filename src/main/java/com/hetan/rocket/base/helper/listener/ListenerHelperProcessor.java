package com.hetan.rocket.base.helper.listener;


import com.hetan.rocket.base.helper.MessageAttributes;

/**
 * 发送辅助处理器
 *
 * @author 赵元昊
 * @date 2021/10/09 11:58
 **/
public interface ListenerHelperProcessor {

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
	 * @param e          e
	 */
	void onFailSave(String messageKey, MessageAttributes attributes, Object message, Exception e);

	/**
	 * 失败通知
	 *
	 * @param messageKey 消息键
	 * @param attributes 属性
	 * @param message    消息
	 * @param e          e
	 */
	void onFailNotify(String messageKey, MessageAttributes attributes, Object message, Exception e);

	/**
	 * 幂等
	 *
	 * @param messageKey 消息键
	 * @param attributes 属性
	 * @param message    消息
	 * @return boolean
	 */
	boolean onIdempotent(String messageKey, MessageAttributes attributes, Object message);

	/**
	 * 幂等提交
	 *
	 * @param messageKey 消息键
	 * @param attributes 属性
	 * @param message    消息
	 */
	void onIdempotentCommit(String messageKey, MessageAttributes attributes, Object message);

	/**
	 * 幂等回滚
	 *
	 * @param messageKey 消息键
	 * @param attributes 属性
	 * @param message    消息
	 */
	void onIdempotentRollback(String messageKey, MessageAttributes attributes, Object message);

}
