package com.hetan.rocket.base.annotation;

import com.hetan.rocket.base.config.RocketAutoConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 全局启动
 *
 * @author 赵元昊
 * @date 2021/08/23 10:56
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import({RocketAutoConfiguration.class})
public @interface EnableRocketMQ {

}
