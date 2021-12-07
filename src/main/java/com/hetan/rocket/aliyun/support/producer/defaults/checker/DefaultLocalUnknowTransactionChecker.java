package com.hetan.rocket.aliyun.support.producer.defaults.checker;

import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.transaction.LocalTransactionChecker;
import com.aliyun.openservices.ons.api.transaction.TransactionStatus;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 回查本地事务时会进行的操作 本类文件默认提交
 *
 * @author 赵元昊
 * @date 2021/08/20 15:36
 */
@Data
@Slf4j
public class DefaultLocalUnknowTransactionChecker implements LocalTransactionChecker {

	/**
	 * 回查本地事务，Broker回调Producer，将未结束的事务发给Producer，由Producer来再次决定事务是提交还是回滚
	 *
	 * @param msg 消息
	 * @return {@link TransactionStatus} 事务状态, 包含提交事务、回滚事务、未知状态
	 */
	@Override
	public TransactionStatus check(Message msg) {
		log.info(">>>> Review local transactions message:{}>>>>", msg);
		return TransactionStatus.CommitTransaction;
	}
}
