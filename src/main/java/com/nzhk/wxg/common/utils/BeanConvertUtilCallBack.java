package com.nzhk.wxg.common.utils;

/**
 * bean转换回调方法
 *
 * @param <S> 源对象类型
 * @param <T> 目标对象类型
 * @author lzy
 */
@FunctionalInterface
public interface BeanConvertUtilCallBack<S, T> {

    /**
     * 定义默认回调方法
     *
     * @param s 源对象
     * @param t 目标对象
     */
    void callBack(S s, T t);
}