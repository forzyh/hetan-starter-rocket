package com.hetan.rocket.base.error;

/**
 * rocket mq 幂等异常
 *
 * @author 赵元昊
 * @date 2021/08/18 17:43
 **/
public class RocketIdempotentException extends RocketAccessException {
	public RocketIdempotentException() {
		super();
	}

	public RocketIdempotentException(String message) {
		super(message);
	}

	public RocketIdempotentException(String message, Throwable cause) {
		super(message, cause);
	}

	public RocketIdempotentException(Throwable cause) {
		super(cause);
	}

	protected RocketIdempotentException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	/**
	 * 静态异常工厂
	 *
	 * @param message 消息
	 */
	public static void throwException(String message) {
		throw new RocketIdempotentException(message);
	}
}
