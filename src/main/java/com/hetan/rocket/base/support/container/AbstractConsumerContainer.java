package com.hetan.rocket.base.support.container;


import com.hetan.rocket.base.helper.MessageAttributes;
import com.hetan.rocket.base.config.RocketProperties;
import com.hetan.rocket.base.core.consumer.RocketConsumerListener;
import com.hetan.rocket.base.error.RocketAccessException;
import com.hetan.rocket.base.error.RocketIdempotentException;
import com.hetan.rocket.base.helper.listener.ListenerHelper;
import com.hetan.rocket.base.helper.listener.ListenerHelperProcessor;
import com.hetan.rocket.base.helper.util.ProcessorUtil;
import com.hetan.rocket.base.serializer.base.RocketSerializer;
import com.hetan.rocket.base.utils.RocketSpringUtil;
import com.hetan.rocket.base.utils.RocketThreadLocalUtil;
import lombok.Data;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.factory.DisposableBean;

import java.util.List;
import java.util.Objects;

/**
 * 抽象监听器容器 由实现类负责启动 销毁 处理消息等
 * <p>
 * 默认会在系统初始化的时候找到所有需要代理监听的类 使用容器support生成容器 然后回调容器启动开关 启动容器
 *
 * @author 赵元昊
 * @date 2021/08/18 19:41
 **/
@Data
public abstract class AbstractConsumerContainer<T, A> implements DisposableBean {

	protected boolean running = false;

	protected RocketSerializer serializer;

	protected RocketProperties rocketProperties;

	/**
	 * 代理监听器
	 */
	protected RocketConsumerListener<T, A> actingListener;

	protected Class<T> messageType;
	protected Class<A> returnType;

	protected String groupId;
	protected String topic;
	protected String tag;

	protected boolean isIdempotent = false;
	protected boolean successSave = false;
	protected boolean failSave = false;
	protected boolean failNotify = false;
	protected boolean autoCommit = false;
	protected String businessType = "";

	protected List<ListenerHelperProcessor> listenerHelperProcessors;

	/**
	 * 消费者容器 构造器
	 *
	 * @param serializer     序列化器
	 * @param actingListener 代理侦听器
	 * @param messageType    消息类型
	 * @param returnType     返回类型
	 * @param groupId        组id
	 * @param topic          主题
	 * @param tag            标签
	 */
	public AbstractConsumerContainer(RocketProperties rocketProperties, RocketSerializer serializer, RocketConsumerListener<T, A> actingListener, Class<T> messageType, Class<A> returnType, String groupId, String topic, String tag) {
		this.rocketProperties = rocketProperties;
		this.serializer = serializer;
		this.actingListener = actingListener;
		this.messageType = messageType;
		this.returnType = returnType;
		this.groupId = groupId + RocketSpringUtil.getActiveSuffix();
		this.topic = topic + RocketSpringUtil.getActiveSuffix();
		this.tag = tag;
		Class<?> targetClass = AopProxyUtils.ultimateTargetClass(this.actingListener);
		ListenerHelper listenerHelper = targetClass.getAnnotation(ListenerHelper.class);
		if (listenerHelper != null) {
			this.isIdempotent = listenerHelper.isIdempotent();
			this.successSave = listenerHelper.successSave();
			this.failSave = listenerHelper.failSave();
			this.failNotify = listenerHelper.failNotify();
			this.autoCommit = listenerHelper.autoCommit();
			this.businessType = listenerHelper.businessType();
		}
		this.listenerHelperProcessors = ProcessorUtil.getListenerHelperProcessors(listenerHelper, rocketProperties);
	}


	/**
	 * 启动消费者监听
	 */
	public abstract void start();

	/**
	 * 消费消息 默认已被实现
	 *
	 * @param message 消息
	 * @param args    arg游戏
	 * @return {@link A}
	 */
	public A onMessage(MessageAttributes messageAttributes, byte[] message, Object... args) {
		return this.onMessage(messageAttributes, this.getSerializer().deSerialize(message, messageType), args);
	}

	/**
	 * 消费消息 默认使用内部执行者监听器消费
	 *
	 * @param message 消息
	 * @param args    args
	 * @return {@link A}
	 */
	public A onMessage(MessageAttributes messageAttributes, T message, Object... args) {
		A result = null;
		try {
			boolean idempotentPass = true;
			// 开启幂等
			if (isIdempotent) {
				for (ListenerHelperProcessor listenerHelperProcessor : this.listenerHelperProcessors) {
					idempotentPass &= listenerHelperProcessor.onIdempotent(RocketThreadLocalUtil.getMessageKey(), messageAttributes, message);
				}
			}
			// 幂等拦截
			if (!idempotentPass) {
				RocketIdempotentException.throwException(
					String.format("[RocketMQ][幂等拦截]RocketMQ消息重复,GroupID:%s,Topic:%s,Tag:%s,重复MessageKey:%s,重复消息类型:%s,重复消息内容:%s",
						messageAttributes.getGroupId(), messageAttributes.getTopic(), messageAttributes.getTags(),
						RocketThreadLocalUtil.getMessageKey(), messageAttributes.getMessageType(), message));
			}
			// 消费
			result = this.actingListener.onMessage(message, args);
			// 消费结果不一致弹出异常
			if (!Objects.equals(getSuccessCommit(), result)) {
				RocketAccessException.throwException(String.format("[RocketMQ][提交警告]提交类型与成功类型不一致,期望的类型:%s,提交的类型:%s", this.getSuccessCommit(), result));
			}
			// 消费成功保存
			if (successSave) {
				for (ListenerHelperProcessor listenerHelperProcessor : this.listenerHelperProcessors) {
					listenerHelperProcessor.onSuccessSave(RocketThreadLocalUtil.getMessageKey(), messageAttributes, message);
				}
			}
			// 消费成功提交幂等表数据
			if (isIdempotent) {
				for (ListenerHelperProcessor listenerHelperProcessor : this.listenerHelperProcessors) {
					listenerHelperProcessor.onIdempotentCommit(RocketThreadLocalUtil.getMessageKey(), messageAttributes, message);
				}
			}
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			// 消费产生异常
			// 是否幂等 回滚幂等数据
			if (!(e instanceof RocketIdempotentException)) {
				if (isIdempotent) {
					for (ListenerHelperProcessor listenerHelperProcessor : this.listenerHelperProcessors) {
						listenerHelperProcessor.onIdempotentRollback(RocketThreadLocalUtil.getMessageKey(), messageAttributes, message);
					}
				}
			}

			// 消费失败保存
			if (failSave) {
				for (ListenerHelperProcessor listenerHelperProcessor : this.listenerHelperProcessors) {
					listenerHelperProcessor.onFailSave(RocketThreadLocalUtil.getMessageKey(), messageAttributes, message, e);
				}
			}
			// 消费失败通知【异常情款通知】
			if (failNotify) {
				for (ListenerHelperProcessor listenerHelperProcessor : this.listenerHelperProcessors) {
					listenerHelperProcessor.onFailNotify(RocketThreadLocalUtil.getMessageKey(), messageAttributes, message, e);
				}
			}
			// 是否自动提交
			if (autoCommit) {
				if (result != null && !Objects.equals(result, this.getSuccessCommit())) {
					return result;
				} else {
					return this.getSuccessCommit();
				}
			} else {
				// 不自动提交则不包装异常
				throw e;
			}
		}
	}

	public abstract A getSuccessCommit();
}
