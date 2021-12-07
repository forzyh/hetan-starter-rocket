package com.hetan.rocket.base.annotation.other;

import java.lang.annotation.*;

/**
 * 延时标记
 *
 * @author 赵元昊
 * @date 2021/08/23 10:55
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface DeliverTime {

	/**
	 * 时间戳模式-默认关闭，关闭后会计算当前系统的时间戳，
	 * 累加上传入的毫秒数，发送给RocketMQ，开启时间戳模式则什么都不会操作，
	 * 传入什么返回给RocketMQ什么
	 *
	 * @return boolean
	 */
	boolean timeStampModel() default false;

}
