package com.nzhk.wxg.business.habittype.bean;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class UpdateHabitTypeReqData {

    /**
     * 主键
     */
    @ApiModelProperty("主键")
    private String id;

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

    private List<String> orderedIds;

}
