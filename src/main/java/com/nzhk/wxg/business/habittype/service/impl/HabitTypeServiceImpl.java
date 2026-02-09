package com.nzhk.wxg.business.habittype.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nzhk.wxg.business.habittype.bean.HabitTypeListReqData;
import com.nzhk.wxg.business.habittype.bean.UpdateHabitTypeReqData;
import com.nzhk.wxg.business.habittype.entity.HabitType;
import com.nzhk.wxg.business.habittype.service.IHabitTypeService;
import com.nzhk.wxg.common.cache.ContextCache;
import com.nzhk.wxg.common.utils.BeanConvertUtil;
import com.nzhk.wxg.common.utils.IdUtil;
import com.nzhk.wxg.mapper.HabitTypeMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 任务类型表：用户可自定义名称、图标与排序 服务实现类
 * </p>
 *
 * @author lxy
 * @since 2026-02-07
 */
@Service
public class HabitTypeServiceImpl extends ServiceImpl<HabitTypeMapper, HabitType> implements IHabitTypeService {

    @Override
    public List<HabitType> getHabitTypes(HabitTypeListReqData data) {
        LambdaQueryWrapper<HabitType> habitTypeLambdaQueryWrapper = new LambdaQueryWrapper<>();
        habitTypeLambdaQueryWrapper.eq(HabitType::getUserId, ContextCache.getUserId());
        return baseMapper.selectList(habitTypeLambdaQueryWrapper);
    }

    @Override
    public void addHabitType(UpdateHabitTypeReqData data) {

        LambdaQueryWrapper<HabitType> habitTypeLambdaQueryWrapper = new LambdaQueryWrapper<>();
        habitTypeLambdaQueryWrapper.eq(HabitType::getUserId, ContextCache.getUserId());
        List<HabitType> habitTypes = baseMapper.selectList(habitTypeLambdaQueryWrapper);

        HabitType habitType = BeanConvertUtil.copySingleProperties(data, HabitType::new);
        habitType.setId(IdUtil.getId());
        habitType.setUserId(ContextCache.getUserId());
        habitType.setSortOrder(habitTypes.size()+1);
        baseMapper.insert(habitType);
    }

    @Override
    public void deleteHabitType(UpdateHabitTypeReqData data) {
        baseMapper.deleteById(data.getId());
    }

    @Override
    public void updateHabitTypeOrder(UpdateHabitTypeReqData data) {
        List<String> orderedIds = data.getOrderedIds();
        for (int i = 0; i < orderedIds.size(); i++) {
            String orderedId =  orderedIds.get(i);
            LambdaUpdateWrapper<HabitType> habitTypeLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            habitTypeLambdaUpdateWrapper.eq(HabitType::getId, orderedId)
                    .set(HabitType::getSortOrder, i+1);
            baseMapper.update(null, habitTypeLambdaUpdateWrapper);
        }

    }

    @Override
    public void updateHabitType(UpdateHabitTypeReqData data) {
        LambdaUpdateWrapper<HabitType> habitTypeLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        habitTypeLambdaUpdateWrapper.eq(HabitType::getId, data.getId())
                .set(HabitType::getName, data.getName())
                .set(HabitType::getIcon, data.getIcon());
        baseMapper.update(null, habitTypeLambdaUpdateWrapper);
    }
}
