package com.hetan.rocket.base.helper.send;

import java.lang.annotation.*;

/**
 * 发送助手
 *
 * @author 赵元昊
 * @date 2021/10/09 11:48
 **/
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface SendHelper {

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
	 * 处理器
	 *
	 * @return {@link Class}<{@link ?} {@link extends} {@link SendHelperProcessor}>{@link []}
	 */
	Class<? extends SendHelperProcessor>[] processor() default {};
}
