package com.miaosu.flux.system;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * 序列号
 * Created by angus on 15/9/29.
 */
@Data
@Entity
@Table(name = "serial_no")
public class SerialNo implements Serializable{
    @Id
    private String seqName;

    private Long curlVal;

    private Long incrementVal;
}
