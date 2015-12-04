package com.miaosu.flux.system.menus;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * Ext 菜单
 * Created by angus on 15/6/16.
 */
@EqualsAndHashCode(callSuper = false)
@Data
public class ExtMenu extends Menu {

    /**
     * 是否展开
     */
    private boolean expandable;

    /**
     * 单击展开
     */
    private boolean singleExpand;


    /**
     * id path. 全路径
     */
    protected String idPath;

    /**
     * text path. 全路径
     */
    protected String textPath;

    /**
     * 子节点
     */
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private List<Menu> children = new ArrayList<Menu>();

    public ExtMenu(){}

    public ExtMenu(Menu menu){
        this.id = menu.id;
        this.text = menu.text;
        this.iconCls = menu.iconCls;
        this.parentId = menu.parentId;
        this.leaf = menu.leaf;
        this.alias = menu.alias;
        this.url = menu.url;
        this.authorities = menu.authorities;
    }

    public void addChildren(Menu menu) {
        this.children.add(menu);
    }

}
