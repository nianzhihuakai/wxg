package com.nzhk.wxg.common.info;

import lombok.Data;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;

@Data
public class RequestInfo<T> implements Serializable {

    private T data;

}
