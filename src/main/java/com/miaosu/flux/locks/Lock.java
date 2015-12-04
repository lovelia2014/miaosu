package com.miaosu.flux.locks;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

/**
 * lock
 * Created by angus on 15/10/7.
 */
@Data
@AllArgsConstructor
public class Lock {
    private String name;

    private Boolean locked;

    private Date lockedTime;

    private String lockedBy;
}
