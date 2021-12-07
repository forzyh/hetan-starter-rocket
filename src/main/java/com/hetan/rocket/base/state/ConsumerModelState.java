package com.hetan.rocket.base.state;

/**
 * 消费处理类型 暂未使用
 *
 * @author 赵元昊
 * @date 2021/08/19 00:20
 **/
public interface ConsumerModelState {

	/**
	 * 普通处理
	 */
	String COMMON = "COMMON";

	/**
	 * 顺序处理
	 */
	String ORDER = "ORDER";

	/**
	 * 批处理
	 */
	String BATCH = "BATCH";
}
