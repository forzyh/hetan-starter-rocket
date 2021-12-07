package com.hetan.rocket.base.helper.listener;

import java.lang.annotation.*;

/**
 * 监听助手注解
 *
 * @author 赵元昊
 * @date 2021/10/11 18:05
 **/
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface ListenerHelper {

	boolean isIdempotent() default false;

	/**
	 * 成功保存
	 *
	 * @return boolean
	 */
	boolean successSave() default true;

	/**
	 * 发送失败保存
	 *
	 * @return boolean
	 */
	boolean failSave() default false;

	/**
	 * 失败的通知
	 *
	 * @return boolean
	 */
	boolean failNotify() default true;

	/**
	 * 自动提交-永远消费成功，忽略任何异常情况
	 *
	 * @return boolean
	 */
	boolean autoCommit() default true;

	/**
	 * 业务类型
	 *
	 * @return {@link String}
	 */
	String businessType() default "";

	/**
	 * 处理器
	 *
	 * @return {@link Class}<{@link ?} {@link extends} {@link ListenerHelperProcessor}>{@link []}
	 */
	Class<? extends ListenerHelperProcessor>[] processor() default {};

}
