/**
 * Created by angus
 */
ProductListPanel = function (config) {

    var serviceUrl = Desktop.contextPath + "/api/product/search";
    var me = this;

    // 搜索面板
    var formPanel = new Ext.form.FormPanel({
        autoScroll: true,
        containerScroll: true,
        bodyStyle: 'padding:5px 5px 0',
        region: 'center',
        labelWidth: 75,
        border: false,
        defaultType: 'textfield',
        layout: 'absolute',
        defaults: {
            // 应用于每个被包含的项 applied to each contained item
            width: 370,
            msgTarget: 'side'
        },
        layoutConfig: {
            // 这里是布局配置项 layout-specific configs go here
            labelSeparator: ':'
        },
        items: [
            {
                x: 10,
                y: 12,
                width: 40,
                xtype: 'label',
                text: '搜索：'
            },
            {
                x: 50,
                y: 10,
                width: 400,
                name: 'text',
                emptyText: '可输入商品代码、商品名称进行搜索',
                enableKeyEvents: true,
                scope: this,
                listeners: {
                    scope: this,
                    keydown: function (field, event) {
                        if (event.keyCode == 13) { //回车键事件
                            me.searchBtnClick();
                        }
                    }
                }
            },
            {
                x: 460,
                y: 10,
                width: 70,
                xtype: 'button',
                id: 'ProductListSearchBtn',
                text: '搜  索',
                scope: this,
                handler: this.searchBtnClick
            }

        ]
    });

    var proxy = new Ext.data.HttpProxy({
        url: serviceUrl,
        timeout: 10000,
        method: 'GET'
    });

    var store = new Ext.data.JsonStore({
        autoLoad: true,
        bodyStyle: 'padding:5px 5px 0',
        root: 'data',
        totalProperty: 'totalCount',
        proxy: proxy,
        remoteSort: true,
        fields: ['id', 'name', 'type', 'price', 'enabled'],
        listeners: {
            scope: this,
            "beforeload": function (store) {
                var params = me.formPanel.getForm().getFieldValues();
                params.limit = Desktop.pageSize;
                store.baseParams = params;
            }
        }
    });

    var pageBarDisplayMsg = '当前展示第{0} - {1}条；共{2}条；';

    // 分页条
    var pagingbar = new Ext.PagingToolbar({
        pageSize: Desktop.pageSize,
        store: store,
        displayInfo: true,
        displayMsg: pageBarDisplayMsg,
        emptyMsg: '未搜索到记录'
        //plugins: new Ext.ux.ProgressBarPager()
    });
    pagingbar.inputItem.setDisabled(true);

    // 表格面板
    var gridPanel = new Ext.grid.GridPanel({
        region: 'center',
        stripeRows: true,
        loadMask: {
            msg: "努力加载数据中，请稍后..."
        },
        store: store,
        sm: new Ext.grid.RowSelectionModel({
            //singleSelect: true,
            listeners: {
                scope: this,
                selectionchange: function (sm) {
                    gridPanel.removeBtn.setDisabled(sm.getCount() < 1);
                    gridPanel.enableBtn.setDisabled(sm.getCount() < 1);
                    gridPanel.disableBtn.setDisabled(sm.getCount() < 1);
                    gridPanel.updateBtn.setDisabled(sm.getCount() != 1);
                }
            }
        }),
        tbar: [{
            text: '添 加',
            iconCls: 'icon-add',
            scope: this,
            handler: this.addBtnHandler
        }, '-', {
            text: '删 除',
            ref: '../removeBtn',
            iconCls: 'icon-delete',
            disabled: true,
            scope: this,
            handler: this.deleteBtnHandler
        }, '-', {
            text: '修 改',
            iconCls: 'icon-edit',
            ref: '../updateBtn',
            disabled: true,
            scope: this,
            handler: this.updateBtnHandler
        }, '-', {
            text: '启 用',
            ref: '../enableBtn',
            iconCls: 'icon-start',
            disabled: true,
            scope: this,
            handler: this.enableBtnHandler
        }, '-', {
            text: '禁 用',
            ref: '../disableBtn',
            iconCls: 'icon-stop',
            disabled: true,
            scope: this,
            handler: this.disableBtnHandler
        }],
        columns: [
            new Ext.grid.RowNumberer({width: 25}),
            {id: 'id', align: 'center', header: "商品代码", width: 90, sortable: true, dataIndex: 'id'},
            {id: 'name', align: 'center', header: "商品名称", width: 120, sortable: true, dataIndex: 'name'},
            {id: 'type', align: 'center', header: "类型", width: 60, sortable: true, dataIndex: 'type', renderer: function (text) { if(text == 'QX'){return '前向产品';}if(text == 'HX'){return '后向产品';}} },
            {id: 'price', align: 'center', header: "单价", width: 60, sortable: true, dataIndex: 'price'},
            {id: 'enabled', align: 'center', header: "状态", width: 60, sortable: true, dataIndex: 'enabled', renderer: function (text) { return text ? '<span style="color: green">启用</span>': '<span style="color: red">禁用</span>' ;}}
        ],
        bbar: pagingbar,
        listeners:{
            scope: this,
            rowdblclick: function(grid, rowIndex, event){
                var record = grid.store.getAt(rowIndex);
                var id = record.get("id");
                this.updateBtnHandler(null, null, id);
            }
        }
    });

    var cfg = {
        closable: true,
        autoScroll: true,
        layout: 'border',
        margins: '35 5 5 0',
        containerScroll: true,
        items: [{
            height: 50,
            border: false,
            //split: true,
            //collapseMode: 'mini',
            region: 'north',
            layout: 'border',
            items: [formPanel]
        },
            gridPanel
        ]
    };

    // 设置为成员属性
    this.formPanel = formPanel;
    this.gridPanel = gridPanel;
    this.store = store;

    var allConfig = Ext.applyIf(config || {}, cfg);
    ProductListPanel.superclass.constructor.call(this, allConfig);
};

Ext.extend(ProductListPanel, Ext.Panel, {

    /**
     * 搜索按钮点击动作
     */
    searchBtnClick: function () {
        var frm = this.formPanel.getForm();

        var params = frm.getFieldValues();
        params.start = 0;
        params.limit = Desktop.pageSize;

        var btn = Ext.getCmp("ProductListSearchBtn");
        btn.disabled = true;
        this.loadGridData(params, function () {
            btn.disabled = false;
        });
    },

    /**
     * 加载表格数据
     * @param params
     * @param next
     */
    loadGridData: function (params, next) {
        //var text = this.pagingBar.displayItem.autoEl.html || this.pagingBar.displayItem.el.dom.innerHTML;
        this.gridPanel.getStore().load({
            params: params,
            scope: this,
            callback: function (records, options, success) {
                if(!success){
                    window.location.reload(true);
                }
                // do noting
                next();
            }
        });
    },

    /**
     * 新增按钮事件
     */
    addBtnHandler: function () {
        var win = Ext.getCmp("AddProductWin");
        var me = this;
        if (!win) {
            win = new EditProductWindow({
                id: 'AddProductWin',
                modal: true,
                isAdd: true,
                callback: function(){
                    me.store.load();
                }
            });
        }
        win.show();
    },

    /**
     * 删除按钮事件
     */
    deleteBtnHandler: function () {
        var s = this.gridPanel.getSelectionModel().getSelections();
        var ids = [];
        for(var i = 0, r; r = s[i]; i++){
            ids.push(r.get("id"));
        }

        if(ids.length == 0){
            return;
        }

        var me = this;

        Ext.Msg.show({
            title: '确认',
            msg: '请确认是否删除选中的' + ids.length + '条记录，数据删除后不可恢复?',
            scope: this,
            buttons: Ext.Msg.YESNO,
            icon: Ext.MessageBox.QUESTION,
            fn: function (buttonId, text, opt) {
                if (buttonId === 'yes') {
                    Ext.Ajax.request({
                        url: Desktop.contextPath + "/api/product/remove",
                        method: "POST",
                        params: {
                            ids: ids
                        },
                        callback: function (option, success, resp) {
                            var result = Ext.decode(resp.responseText);
                            if (success && resp.status === 200 && result.success) {
                                me.store.load();
                                return;
                            }
                            console.warn("delete failed");
                            Ext.Msg.show({
                                title: '操作失败',
                                msg: result.message,
                                buttons: Ext.Msg.OK,
                                icon: Ext.MessageBox.ERROR
                            });
                        }
                    });
                }
            }
        });
    },

    /**
     * 修改按钮事件
     */
    updateBtnHandler: function (btn, event, id) {
        if(!id) {
            var s = this.gridPanel.getSelectionModel().getSelections();

            if (s.length != 1) {
                Ext.Msg.show({
                    title: '警告',
                    msg: "只能选择一条数据！",
                    buttons: Ext.Msg.OK,
                    icon: Ext.MessageBox.WARNING
                });
                return;
            }

            id = s[0].get("id")
        }
        var me = this;

        Ext.Ajax.request({
            url: Desktop.contextPath + "/api/product/get/" + id,
            method: "GET",
            params: {
            },
            callback: function (option, success, resp) {
                var result = Ext.decode(resp.responseText);
                if (success && resp.status === 200 && result.success) {
                    var win = Ext.getCmp("EditProductWin");
                    if (!win) {
                        win = new EditProductWindow({
                            id: 'EditProductWin',
                            modal: true,
                            data: result.data,
                            callback: function(){
                                me.store.load();
                            }
                        });
                    }
                    win.show();
                } else {
                    console.warn("updateBtnHandler failed");
                    Ext.Msg.show({
                        title: '获取用户失败',
                        msg: result.message,
                        buttons: Ext.Msg.OK,
                        icon: Ext.MessageBox.ERROR
                    });
                    me.store.load();
                }
            }
        });
    },

    /**
     * 启用事件
     */
    enableBtnHandler: function () {
        var s = this.gridPanel.getSelectionModel().getSelections();
        var ids = [];
        for(var i = 0, r; r = s[i]; i++){
            ids.push(r.get("id"));
        }

        if(ids.length == 0){
            return;
        }

        var me = this;

        Ext.Msg.show({
            title: '确认',
            msg: '请确认是否启用选中的' + ids.length + '条记录?',
            scope: this,
            buttons: Ext.Msg.YESNO,
            icon: Ext.MessageBox.QUESTION,
            fn: function (buttonId, text, opt) {
                if (buttonId === 'yes') {
                    Ext.Ajax.request({
                        url: Desktop.contextPath + "/api/product/enable",
                        method: "POST",
                        params: {
                            ids: ids
                        },
                        callback: function (option, success, resp) {
                            var result = Ext.decode(resp.responseText);
                            if (success && resp.status === 200 && result.success) {
                                me.store.load();
                                return;
                            }
                            console.warn("enable failed");
                            Ext.Msg.show({
                                title: '操作失败',
                                msg: result.message,
                                buttons: Ext.Msg.OK,
                                icon: Ext.MessageBox.ERROR
                            });
                        }
                    });
                }
            }
        });

    },

    /**
     * 禁用按钮事件
     */
    disableBtnHandler: function () {
        var s = this.gridPanel.getSelectionModel().getSelections();
        var ids = [];
        for (var i = 0, r; r = s[i]; i++) {
            ids.push(r.get("id"));
        }

        if (ids.length == 0) {
            return;
        }

        var me = this;

        Ext.Msg.show({
            title: '确认',
            msg: '请确认是否禁用选中的' + ids.length + '条记录，商品禁用后不可下单?',
            scope: this,
            buttons: Ext.Msg.YESNO,
            icon: Ext.MessageBox.QUESTION,
            fn: function (buttonId, text, opt) {
                if (buttonId === 'yes') {
                    Ext.Ajax.request({
                        url: Desktop.contextPath + "/api/product/disable",
                        method: "POST",
                        params: {
                            ids: ids
                        },
                        callback: function (option, success, resp) {
                            var result = Ext.decode(resp.responseText);
                            if (success && resp.status === 200 && result.success) {
                                me.store.load();
                                return;
                            }
                            console.warn("disable failed");
                            Ext.Msg.show({
                                title: '操作失败',
                                msg: result.message,
                                buttons: Ext.Msg.OK,
                                icon: Ext.MessageBox.ERROR
                            });
                        }
                    });
                }
            }
        });
    }
});

Ext.reg('tab.productlist', ProductListPanel);

