package com.nzhk.wxg.business.habittype.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 任务类型表：用户可自定义名称、图标与排序
 * </p>
 *
 * @author lxy
 * @since 2026-02-07
 */
@Data
@TableName("habit_type")
@ApiModel(value = "HabitType对象", description = "任务类型表：用户可自定义名称、图标与排序")
public class HabitType implements Serializable {

    private static final long serialVersionUID = 1L;


    /**
     * 主键
     */
    @ApiModelProperty("主键")
    @TableId(value = "id")
    private String id;

    private String userId;

    /**
     * 类型名称（如：生活、学习、健身）
     */
    @ApiModelProperty("类型名称（如：生活、学习、健身）")
    private String name;

    /**
     * 图标，emoji 字符
     */
    @ApiModelProperty("图标，emoji 字符")
    private String icon;

    /**
     * 排序序号，越小越靠前，用于新建目标与首页分类顺序
     */
    @ApiModelProperty("排序序号，越小越靠前，用于新建目标与首页分类顺序")
    private Integer sortOrder;

    /**
     * 创建时间
     */
    @ApiModelProperty("创建时间")
    private LocalDateTime createdAt;

}
