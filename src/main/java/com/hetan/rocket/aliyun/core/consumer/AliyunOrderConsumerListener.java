package com.hetan.rocket.aliyun.core.consumer;

import com.aliyun.openservices.ons.api.order.OrderAction;
import com.hetan.rocket.base.core.consumer.RocketConsumerListener;

/**
 * 阿里云sdk 顺序消费
 *
 * @author 赵元昊
 * @date 2021/08/19 13:36
 **/
public interface AliyunOrderConsumerListener<T> extends RocketConsumerListener<T, OrderAction> {
}
