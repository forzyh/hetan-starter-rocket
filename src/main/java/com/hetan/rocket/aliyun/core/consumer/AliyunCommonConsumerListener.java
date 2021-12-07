package com.hetan.rocket.aliyun.core.consumer;

import com.aliyun.openservices.ons.api.Action;
import com.hetan.rocket.base.core.consumer.RocketConsumerListener;

/**
 * 封装阿里SDK 响应体的监听器
 *
 * @author 赵元昊
 * @date 2021/08/19 13:34
 **/
public interface AliyunCommonConsumerListener<T> extends RocketConsumerListener<T, Action> {
}
