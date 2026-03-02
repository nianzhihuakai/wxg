package com.nzhk.wxg.business.habitcheckin.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * <p>
 * 习惯打卡记录表
 * </p>
 *
 * @author lxy
 * @since 2026-02-06
 */
@Data
@TableName("habit_check_in")
@ApiModel(value = "HabitCheckIn对象", description = "习惯打卡记录表")
public class HabitCheckIn implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 打卡记录ID
     */
    @ApiModelProperty("打卡记录ID")
    private String id;

    /**
     * 习惯ID
     */
    @ApiModelProperty("习惯ID")
    private String habitId;

    /**
     * 用户ID
     */
    @ApiModelProperty("用户ID")
    private String userId;

    /**
     * 打卡日期
     */
    @ApiModelProperty("打卡日期")
    private LocalDate checkInDate;

    /**
     * 打卡时间(时间戳)
     */
    @ApiModelProperty("打卡时间(时间戳)")
    private LocalDateTime checkInTime;

    /**
     * 创建时间(时间戳)
     */
    @ApiModelProperty("创建时间(时间戳)")
    private LocalDateTime createTime;

    @ApiModelProperty("打卡类型 1-普通打卡 2-补卡")
    private Integer checkInType;
}
