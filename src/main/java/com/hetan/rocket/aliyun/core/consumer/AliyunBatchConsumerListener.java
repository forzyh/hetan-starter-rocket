package com.hetan.rocket.aliyun.core.consumer;

import com.aliyun.openservices.ons.api.Action;
import com.hetan.rocket.base.core.consumer.RocketBatchConsumerListener;

/**
 * 阿里云SDK 批处理消费监听器
 *
 * @author 赵元昊
 * @date 2021/08/19 13:38
 **/
public interface AliyunBatchConsumerListener<T> extends RocketBatchConsumerListener<T, Action> {
}
