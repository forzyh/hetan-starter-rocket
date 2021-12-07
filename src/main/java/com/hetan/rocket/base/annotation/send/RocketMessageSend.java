package com.hetan.rocket.base.annotation.send;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * 消息发送
 *
 * @author 赵元昊
 * @date 2021/08/23 10:56
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Component
@Documented
public @interface RocketMessageSend {
	/**
	 * 您在控制台创建的 Group ID
	 *
	 * @return String
	 */
	String groupID() default "";

	/**
	 * 拓展GroupID
	 *
	 * @return {@link String}
	 */
	String exGroupID() default "";

}
