package com.hetan.rocket.aliyun.util;

import com.aliyun.openservices.ons.api.PropertyKeyConst;
import com.hetan.rocket.base.config.RocketProperties;

import java.util.Properties;

/**
 * 属性的工厂
 *
 * @author 赵元昊
 * @date 2021/08/23 10:56
 */
public class PropertiesFactory {

	public static Properties createProperties(RocketProperties rocketProperties) {
		Properties properties = new Properties();
		properties.put(PropertyKeyConst.NAMESRV_ADDR, rocketProperties.getNameSrvAddr());
		properties.put(PropertyKeyConst.AccessKey, rocketProperties.getAccessKey());
		properties.put(PropertyKeyConst.SecretKey, rocketProperties.getSecretKey());
		properties.put(PropertyKeyConst.OnsChannel, rocketProperties.getOnsChannel());

		return properties;
	}

	public static Properties createConsumerProperties(RocketProperties rocketProperties,
													  String groupId,
													  String messageModel) {

		Properties properties = createProperties(rocketProperties);

		properties.put(PropertyKeyConst.GROUP_ID, groupId);
		properties.put(PropertyKeyConst.MessageModel, messageModel);
		properties.put(PropertyKeyConst.ConsumeThreadNums, rocketProperties.getConsumeThreadNums());
		properties.put(PropertyKeyConst.MaxReconsumeTimes, rocketProperties.getMaxReconsumeTimes());
		properties.put(PropertyKeyConst.ConsumeTimeout, rocketProperties.getConsumeTimeout());

		return properties;

	}

	public static Properties createProducerProperties(RocketProperties rocketProperties, String groupId) {
		Properties properties = PropertiesFactory.createProperties(rocketProperties);
		properties.put(PropertyKeyConst.SendMsgTimeoutMillis, rocketProperties.getSendMsgTimeoutMillis());
		properties.put(PropertyKeyConst.GROUP_ID, groupId);
		return properties;
	}

	public static Properties createTransactionProducerProperties(RocketProperties rocketProperties, String groupId) {
		Properties properties = createProducerProperties(rocketProperties, groupId);
		properties.put(PropertyKeyConst.CheckImmunityTimeInSeconds, rocketProperties.getCheckImmunityTimeInSeconds());
		return properties;
	}
}
