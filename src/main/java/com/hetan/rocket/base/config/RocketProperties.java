package com.hetan.rocket.base.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 火箭的属性
 *
 * @author 赵元昊
 * @date 2021/08/23 10:55
 */
@Data
@ConfigurationProperties(prefix = RocketProperties.ROCKET_PREFIX)
public class RocketProperties {

	public static final String ROCKET_PREFIX = "rocket";

	/**
	 * TOPIC GROUP 后缀放行列表
	 */
	private List<String> enabledSuffixList = new ArrayList<>();

	/**
	 * TOPIC GROUP 优先级最高的自定义后缀 会覆盖enabledSuffixList 自动产生的后缀
	 */
	private String suffix = null;

	/**
	 * 设置 TCP 协议接入点，从控制台获取
	 */
	private String nameSrvAddr;

	/**
	 * 您在阿里云账号管理控制台中创建的 AccessKey，用于身份认证
	 */
	private String accessKey;

	/**
	 * 您在阿里云账号管理控制台中创建的 SecretKey，用于身份认证
	 */
	private String secretKey;

	/**
	 * 用户渠道，默认为：ALIYUN，聚石塔用户为：CLOUD
	 */
	private String onsChannel = "ALIYUN";

	/**
	 * 设置消息发送的超时时间，单位（毫秒），默认：3000
	 */
	private Integer sendMsgTimeoutMillis = 3000;

	/**
	 * 设置事务消息第一次回查的最快时间，单位（秒）
	 */
	private Integer checkImmunityTimeInSeconds = 5;

	/**
	 * 设置 RocketMessage 实例的消费线程数，阿里云默认：20
	 * 默认cpu数量*2+1
	 */
	private Integer consumeThreadNums = Runtime.getRuntime().availableProcessors() * 2 + 1;

	/**
	 * 设置消息消费失败的最大重试次数，默认：16
	 */
	private Integer maxReconsumeTimes = 16;

	/**
	 * 设置每条消息消费的最大超时时间，超过设置时间则被视为消费失败，等下次重新投递再次消费。 每个业务需要设置一个合理的值，单位（分钟）。 默认：15
	 */
	private Integer consumeTimeout = 15;

	/**
	 * 只适用于顺序消息，设置消息消费失败的重试间隔时间默认100毫秒
	 */
	private Integer suspendTimeMilli = 100;

	/**
	 * 默认发送的助手
	 */
	private String defaultSendHelper;

	/**
	 * 默认侦听器辅助
	 */
	private String defaultListenerHelper;


}
