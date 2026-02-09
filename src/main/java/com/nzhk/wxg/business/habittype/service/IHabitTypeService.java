package com.nzhk.wxg.business.habittype.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nzhk.wxg.business.habittype.bean.HabitTypeListReqData;
import com.nzhk.wxg.business.habittype.bean.UpdateHabitTypeReqData;
import com.nzhk.wxg.business.habittype.entity.HabitType;

import java.util.List;

/**
 * <p>
 * 任务类型表：用户可自定义名称、图标与排序 服务类
 * </p>
 *
 * @author lxy
 * @since 2026-02-07
 */
public interface IHabitTypeService extends IService<HabitType> {

    List<HabitType> getHabitTypes(HabitTypeListReqData data);

    void addHabitType(UpdateHabitTypeReqData data);

    void deleteHabitType(UpdateHabitTypeReqData data);

    void updateHabitTypeOrder(UpdateHabitTypeReqData data);

    void updateHabitType(UpdateHabitTypeReqData data);
}
