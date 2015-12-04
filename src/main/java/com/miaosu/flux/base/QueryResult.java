package com.miaosu.flux.base;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * 查询结果集
 * Created by angus on 15/9/29.
 */
@Data
@AllArgsConstructor
public class QueryResult<T> implements Serializable{
    public QueryResult(){}

    public QueryResult(Long totalCount, T data) {
        this(true, totalCount, data);
    }

    private boolean success;

    private Long totalCount;

    private T data;
}
