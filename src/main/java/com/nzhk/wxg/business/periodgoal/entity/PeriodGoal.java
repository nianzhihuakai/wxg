package com.nzhk.wxg.business.periodgoal.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("period_goal")
@ApiModel(value = "PeriodGoal", description = "周期目标")
public class PeriodGoal implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    private String id;

    @ApiModelProperty("用户ID")
    private String userId;

    @ApiModelProperty("标题")
    private String title;

    @ApiModelProperty("周期类型 week/month/year")
    private String periodType;

    @ApiModelProperty("周期开始")
    private LocalDate periodStart;

    @ApiModelProperty("周期结束")
    private LocalDate periodEnd;

    @ApiModelProperty("统计方式 checkin_days/checkin_count")
    private String metricType;

    @ApiModelProperty("目标值")
    private Integer targetValue;

    @ApiModelProperty("习惯ID列表 JSON")
    private String habitIds;

    @ApiModelProperty("收获富文本")
    private String harvestHtml;

    @ApiModelProperty("0删除 1有效")
    private Integer status;

    @ApiModelProperty("创建时间")
    private LocalDateTime createdAt;

    @ApiModelProperty("更新时间")
    private LocalDateTime updatedAt;
}
