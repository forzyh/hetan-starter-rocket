package com.hetan.rocket.base.error;

/**
 * rocket mq 异常
 *
 * @author 赵元昊
 * @date 2021/08/18 17:43
 **/
public class RocketAccessException extends RuntimeException {
	public RocketAccessException() {
		super();
	}

	public RocketAccessException(String message) {
		super(message);
	}

	public RocketAccessException(String message, Throwable cause) {
		super(message, cause);
	}

	public RocketAccessException(Throwable cause) {
		super(cause);
	}

	protected RocketAccessException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	/**
	 * 静态异常工厂
	 *
	 * @param message 消息
	 */
	public static void throwException(String message) {
		throw new RocketAccessException(message);
	}
}
