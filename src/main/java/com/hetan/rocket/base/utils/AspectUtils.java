package com.hetan.rocket.base.utils;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 方面跑龙套
 *
 * @author 赵元昊
 * @date 2021/08/23 10:53
 */
public class AspectUtils {
	private AspectUtils() {
	}

	/**
	 * 获得代理的方法
	 *
	 * @param proceedingJoinPoint 进行连接点
	 * @return {@link Method}
	 */
	public static Method getMethod(ProceedingJoinPoint proceedingJoinPoint) {
		MethodSignature signature = (MethodSignature) proceedingJoinPoint.getSignature();
		return signature.getMethod();
	}

	/**
	 * 获得代理的实际类
	 *
	 * @param proceedingJoinPoint 进行连接点
	 * @return {@link Class}
	 */
	public static Class<?> getClass(ProceedingJoinPoint proceedingJoinPoint) {
		return proceedingJoinPoint.getTarget().getClass();
	}

	/**
	 * 获取方法上的注解
	 *
	 * @param proceedingJoinPoint 进行连接点
	 * @param annotationClass     注释类
	 * @return {@link T}
	 */
	public static <T extends Annotation> T getAnnotation(ProceedingJoinPoint proceedingJoinPoint, Class<T> annotationClass) {
		return getMethod(proceedingJoinPoint).getAnnotation(annotationClass);
	}

	/**
	 * 获取类上的注解
	 *
	 * @param proceedingJoinPoint 进行连接点
	 * @param annotationClass     注释类
	 * @return {@link T}
	 */
	public static <T extends Annotation> T getClassAnnotation(ProceedingJoinPoint proceedingJoinPoint, Class<T> annotationClass) {
		return getMethod(proceedingJoinPoint).getAnnotation(annotationClass);
	}

	/**
	 * 获取方法上的所有注解
	 *
	 * @param proceedingJoinPoint 进行连接点
	 * @return {@link Annotation[]}
	 */
	public static Annotation[] getAnnotations(ProceedingJoinPoint proceedingJoinPoint) {
		return getMethod(proceedingJoinPoint).getAnnotations();
	}

	/**
	 * 得到声明类注解
	 *
	 * @param proceedingJoinPoint 进行连接点
	 * @param annotationClass     注释类
	 * @return {@link T}
	 */
	public static <T extends Annotation> T getDeclaringClassAnnotation(ProceedingJoinPoint proceedingJoinPoint, Class<T> annotationClass) {
		return getMethod(proceedingJoinPoint).getDeclaringClass().getAnnotation(annotationClass);
	}

	/**
	 * 获取方法参数
	 *
	 * @param proceedingJoinPoint 进行连接点
	 * @return {@link Parameter[]}
	 */
	public static Parameter[] getParams(ProceedingJoinPoint proceedingJoinPoint) {
		Method method = getMethod(proceedingJoinPoint);
		return method.getParameters();
	}

	/**
	 * 检查是否是传入的接口的子类（任意成功一个就算成功）
	 *
	 * @param target     目标
	 * @param interfaces 接口
	 * @return boolean
	 */
	public static boolean checkClassAnySubOfInterface(Class<?> target, Class<?>... interfaces) {
		List<Class<?>> classInterface = getClassAllInterface(target);
		if (classInterface.isEmpty() || interfaces == null || interfaces.length == 0) {
			return false;
		}
		return Arrays.stream(interfaces).anyMatch(classInterface::contains);
	}

	/**
	 * 得到类的所有接口 包括接口的父接口
	 *
	 * @param clazz clazz
	 * @return {@link List<Class<?>>}
	 */
	public static List<Class<?>> getClassAllInterface(Class<?> clazz) {
		Set<Class<?>> classes = new HashSet<>();
		List<Class<?>> classInterface = getClassInterface(clazz);
		do {
			classes.addAll(classInterface);
			Set<Class<?>> tempSet = new HashSet<>();
			for (Class<?> tempInterface : classInterface) {
				List<Class<?>> supperInterface = getClassInterface(tempInterface);
				if (supperInterface != null && !supperInterface.isEmpty()) {
					tempSet.addAll(supperInterface);
				}
			}
			classInterface = tempSet.stream().collect(Collectors.toList());
		} while (classInterface != null && !classInterface.isEmpty());
		return classes.stream().collect(Collectors.toList());
	}

	/**
	 * 得到类的第一层接口
	 *
	 * @param clazz clazz
	 * @return {@link List<Class<?>>}
	 */
	public static List<Class<?>> getClassInterface(Class<?> clazz) {
		if (clazz == null) {
			return null;
		} else {
			return Arrays.asList(clazz.getInterfaces());
		}
	}
}
