package com.hetan.rocket.aliyun.support.producer.enums;

/**
 * ClassName: MessageSendType
 * Description:
 * date: 2019/4/28 21:02
 *
 * @author ThierrySquirrel
 * @since JDK 1.8
 */
public enum MessageSendType {

	/**
	 * 同步发送
	 */
	SEND("sync_send"),

	/**
	 * 异步发送
	 */
	SEND_ASYNC("async_send"),

	/**
	 * 单向发送
	 */
	SEND_ONE_WAY("one_way_send");

	private String name;

	MessageSendType(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

}
