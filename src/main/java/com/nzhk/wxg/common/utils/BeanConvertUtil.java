package com.nzhk.wxg.common.utils;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * bean拷贝工具类
 *
 * @author lzy
 */
public class BeanConvertUtil extends BeanUtils {

    /**
     * 拷贝单个数据的工具类
     *
     * @param source: 数据源类
     * @param target: 目标类::new(eg: UserVO::new)
     * @return 拷贝好的结果
     */
    public static <S, T> T copySingleProperties(S source, Supplier<T> target) {
        return copySingleProperties(source, target, null);
    }

    /**
     * 拷贝单个数据的工具类
     *
     * @param source:   数据源类
     * @param target:   目标类::new(eg: UserVO::new)
     * @param callBack: 回调方法
     * @return 拷贝好的结果
     */
    public static <S, T> T copySingleProperties(S source, Supplier<T> target, BeanConvertUtilCallBack<S, T> callBack) {
        T t = target.get();
        copyProperties(source, t);
        if (callBack != null) {
            // 回调
            callBack.callBack(source, t);
        }
        return t;
    }

    /**
     * 集合数据的拷贝
     *
     * @param sources: 数据源类
     * @param target:  目标类::new(eg: UserVO::new)
     * @return 拷贝好的list
     */
    public static <S, T> List<T> copyListProperties(List<S> sources, Supplier<T> target) {
        return copyListProperties(sources, target, null);
    }

    /**
     * 带回调函数的集合数据的拷贝（可自定义字段拷贝规则）
     *
     * @param sources:  数据源类
     * @param target:   目标类::new(eg: UserVO::new)
     * @param callBack: 回调函数
     * @return 拷贝好的list
     */
    public static <S, T> List<T> copyListProperties(List<S> sources, Supplier<T> target, BeanConvertUtilCallBack<S, T> callBack) {
        List<T> list = new ArrayList<>(sources.size());
        for (S source : sources) {
            T t = target.get();

            if (source != null) {
                copyProperties(source, t);
                list.add(t);
            }

            if (callBack != null) {
                // 回调
                callBack.callBack(source, t);
            }
        }
        return list;
    }

    /**
     * 带回调函数的Page集合数据的拷贝（可自定义字段拷贝规则）
     *
     * @param sources:  数据源类
     * @param target:   目标类::new(eg: UserVO::new)
     * @param callBack: 回调函数
     * @return 拷贝好的Page
     */
    public static <S, T> IPage<T> copyPageProperties(IPage<S> sources, Supplier<T> target, BeanConvertUtilCallBack<S, T> callBack) {
        IPage<T> page = new Page<>();
        // 需要指定ArrayList，因为Page源码默认创建了EmptyList，执行add会抛出UnsupportedOperationException异常
        page.setRecords(new ArrayList<>());
        copyProperties(sources, page, "records");

        for (S item : sources.getRecords()) {
            T t = target.get();
            copyProperties(item, t);
            page.getRecords().add(t);

            if (callBack != null) {
                // 回调
                callBack.callBack(item, t);
            }
        }
        return page;
    }

    /**
     * Page集合数据的拷贝
     *
     * @param sources: 数据源类
     * @param target:  目标类::new(eg: UserVO::new)
     * @return 拷贝好的Page
     */
    public static <S, T> IPage<T> copyPageProperties(IPage<S> sources, Supplier<T> target) {
        return copyPageProperties(sources, target, null);
    }

}
