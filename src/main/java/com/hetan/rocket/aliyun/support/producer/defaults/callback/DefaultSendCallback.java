package com.hetan.rocket.aliyun.support.producer.defaults.callback;

import com.aliyun.openservices.ons.api.OnExceptionContext;
import com.aliyun.openservices.ons.api.SendCallback;
import com.aliyun.openservices.ons.api.SendResult;
import lombok.extern.slf4j.Slf4j;

/**
 * 默认发送回调
 *
 * @author 赵元昊
 * @date 2021/08/23 10:56
 */
@Slf4j
public class DefaultSendCallback implements SendCallback {
	/**
	 * 发送成功回调的方法.
	 *
	 * @param sendResult 发送结果
	 */
	@Override
	public void onSuccess(SendResult sendResult) {
		log.info("Message sent successfully sendResult{}", sendResult);
	}

	/**
	 * 发送失败回调方法.
	 *
	 * @param context 失败上下文.
	 */
	@Override
	public void onException(OnExceptionContext context) {
		log.error("Failed to send message context{}", context);
	}
}
