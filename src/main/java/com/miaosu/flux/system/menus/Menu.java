package com.miaosu.flux.system.menus;

import com.miaosu.flux.util.ListToStringConverter;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Menu Entity
 * Created by angus on 15/6/13.
 */
@Entity
@Table(name = "menus")
@Data
//@Converts(value = { @Convert(attributeName = "authorities", converter = ListToStringConverter.class) })
public class Menu implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键，唯一标识符
     */
    @Id
    @GeneratedValue
    protected String id;

    /**
     * 名称,不能包含"/"字符
     */
    @Column(nullable = false)
    protected String text;

    /**
     * 别名，可用来快速搜索
     */
    protected String alias;

    /**
     * 父节点ID
     */
    @Column(name = "parent_id")
    protected String parentId;

    /**
     * 图表样式
     */
    @Column(name = "icon_cls")
    protected String iconCls;

    /**
     * 是否叶子节点
     */
    protected boolean leaf;

    /**
     * 对应功能的url
     */
    protected String url;

    /**
     * 菜单对应的权限信息
     */
    @Convert(converter = ListToStringConverter.class)
    protected List<String> authorities = new ArrayList<String>();

}
