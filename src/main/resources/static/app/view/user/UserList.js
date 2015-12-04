/**
 * Created by angus
 */
UserListPanel = function (config) {

    var serviceUrl = Desktop.contextPath + "/api/member/search";
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
                emptyText: '可输入会员名、真实姓名、手机号码进行搜索',
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
                id: 'UserListSearchBtn',
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
        fields: ['username', 'realName', 'idNumber', 'sex', 'mobilePhone', 'address', 'discount', 'balance', 'regTime',
            'lastLoginTime', 'lastLoginIp', 'enabled'],
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

    var tbar = [];
    if (IS_ADMIN) {
        tbar = [{
            text: '添 加',
            iconCls: 'icon-user-add',
            scope: this,
            handler: this.addBtnHandler
        }, '-', {
            text: '删 除',
            ref: '../removeBtn',
            iconCls: 'icon-user-delete',
            disabled: true,
            scope: this,
            handler: this.deleteBtnHandler
        }, '-', {
            text: '修 改',
            iconCls: 'icon-user-edit',
            ref: '../updateBtn',
            disabled: true,
            scope: this,
            handler: this.updateBtnHandler
        }, '-', {
            text: '启用/禁用',
            ref: '../enableBtn',
            iconCls: 'icon-enable',
            disabled: true,
            scope: this,
            handler: this.enableBtnHandler
        }, '-', {
            text: '重置密钥',
            ref: '../resetTokenBtn',
            iconCls: 'icon-secret',
            disabled: true,
            scope: this,
            handler: this.resetTokenBtnHandler
        }, '-', {
            text: '账户充值',
            ref: '../addBalanceBtn',
            iconCls: 'icon-balance-add',
            disabled: true,
            scope: this,
            handler: this.addBalanceBtnHandler
        }, '-', {
            text: '账户扣款',
            ref: '../subBalanceBtn',
            iconCls: 'icon-balance-sub',
            disabled: true,
            scope: this,
            handler: this.subBalanceBtnHandler
        }];
    }

    // 表格面板
    var gridPanel = new Ext.grid.GridPanel({
        region: 'center',
        stripeRows: true,
        border: IS_ADMIN,
        loadMask: {
            msg: "努力加载数据中，请稍后..."
        },
        store: store,
        sm: new Ext.grid.RowSelectionModel({
            //singleSelect: true,
            listeners: {
                scope: this,
                selectionchange: function (sm) {
                    if(IS_ADMIN) {
                        gridPanel.removeBtn.setDisabled(sm.getCount() < 1);
                        gridPanel.updateBtn.setDisabled(sm.getCount() < 1);
                        gridPanel.enableBtn.setDisabled(sm.getCount() != 1);
                        gridPanel.resetTokenBtn.setDisabled(sm.getCount() != 1);
                        gridPanel.addBalanceBtn.setDisabled(sm.getCount() != 1);
                        gridPanel.subBalanceBtn.setDisabled(sm.getCount() != 1);
                    }
                }
            }
        }),
        tbar: tbar,
        columns: [
            new Ext.grid.RowNumberer({width: 25}),
            {id: 'username', align: 'center', header: "会员名", width: 90, sortable: true, dataIndex: 'username'},
            {id: 'realName', align: 'center', header: "真实姓名", width: 90, sortable: true, dataIndex: 'realName'},
            {id: 'costTime', align: 'center', header: "性别", width: 60, sortable: true, dataIndex: 'sex', renderer: function (text) { return (text == 1) ? '男' : '女'; } },
            {id: 'enabled', align: 'center', header: "状态", width: 60, sortable: true, dataIndex: 'enabled', renderer: function (text) { return text ? '<span style="color: green">启用</span>': '<span style="color: red">禁用</span>' ;}},
            {id: 'balance', align: 'center', header: "余额", width: 60, sortable: true, dataIndex: 'balance'},
            {id: 'discount', align: 'center', header: "折扣", width: 60, sortable: true, dataIndex: 'discount'},
            {id: 'senderHost', align: 'center', header: "手机号码", width: 100, sortable: false, dataIndex: 'mobilePhone'},
            {id: 'uuId', align: 'center', header: "身份证号码", width: 180, sortable: false, dataIndex: 'idNumber', hidden: false},
            {id: 'costTime', align: 'center', header: "联系地址", width: 180, sortable: true, dataIndex: 'address'},
            {id: 'senderName', align: 'center', header: "注册时间", width: 160, sortable: false, dataIndex: 'regTime', hidden: false},
            {id: 'receiverHost', align: 'center', header: "最近一次登录时间", width: 160, sortable: false, dataIndex: 'lastLoginTime'},
            {id: 'receiverName', align: 'center', header: "最近一次登录IP", width: 140, sortable: false, dataIndex: 'lastLoginIp', hidden: false}
        ],
        bbar: pagingbar,
        listeners:{
            scope: this,
            rowdblclick: function(grid, rowIndex, event){
                var record = grid.store.getAt(rowIndex);
                var username = record.get("username");
                this.updateBtnHandler(null, null, username);
            }
        }
    });

    var items = [];
    if(IS_ADMIN) {
        items.push({
            height: 50,
            border: false,
            //split: true,
            //collapseMode: 'mini',
            region: 'north',
            layout: 'border',
            items: [formPanel]
        });
    }
    items.push(gridPanel);

    var cfg = {
        closable: true,
        autoScroll: true,
        layout: 'border',
        margins: '35 5 5 0',
        containerScroll: true,
        items: items
    };

    // 设置为成员属性
    this.formPanel = formPanel;
    this.gridPanel = gridPanel;
    this.store = store;

    var allConfig = Ext.applyIf(config || {}, cfg);
    UserListPanel.superclass.constructor.call(this, allConfig);
};

Ext.extend(UserListPanel, Ext.Panel, {

    /**
     * 搜索按钮点击动作
     */
    searchBtnClick: function () {
        var frm = this.formPanel.getForm();

        var params = frm.getFieldValues();
        params.start = 0;
        params.limit = Desktop.pageSize;

        var btn = Ext.getCmp("UserListSearchBtn");
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
        var win = Ext.getCmp("AddUserWin");
        var me = this;
        if (!win) {
            win = new AddUserWindow({
                id: 'AddUserWin',
                modal: true,
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
        var userNames = [];
        for(var i = 0, r; r = s[i]; i++){
            userNames.push(r.get("username"));
        }

        if(userNames.length == 0){
            return;
        }

        var me = this;

        Ext.Msg.show({
            title: '确认',
            msg: '请确认是否删除选中的' + userNames.length + '条记录，数据删除后不可恢复?',
            scope: this,
            buttons: Ext.Msg.YESNO,
            icon: Ext.MessageBox.QUESTION,
            fn: function (buttonId, text, opt) {
                if (buttonId === 'yes') {
                    Ext.Ajax.request({
                        url: Desktop.contextPath + "/api/member/remove",
                        method: "POST",
                        params: {
                            userNames: userNames
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
    updateBtnHandler: function (btn, event, userName) {
        if(!userName) {
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

            userName = s[0].get("username")
        }
        var me = this;

        Ext.Ajax.request({
            url: Desktop.contextPath + "/api/member/get/" + userName,
            method: "GET",
            params: {
            },
            callback: function (option, success, resp) {
                var result = Ext.decode(resp.responseText);
                if (success && resp.status === 200 && result.success) {
                    var win = Ext.getCmp("EditUserWin");
                    if (!win) {
                        win = new EditUserWindow({
                            id: 'EditUserWin',
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
     * 启用/禁用按钮事件
     */
    enableBtnHandler: function () {
        var s = this.gridPanel.getSelectionModel().getSelections();

        if(s.length != 1){
            Ext.Msg.show({
                title: '警告',
                msg: "只能选择一条数据！",
                buttons: Ext.Msg.OK,
                icon: Ext.MessageBox.WARNING
            });

            return;
        }

        var me = this;

        Ext.Msg.show({
            title: '确认',
            msg: '请确认是否启用/禁用用户，用户禁用后将不可下单?',
            scope: this,
            buttons: Ext.Msg.YESNO,
            icon: Ext.MessageBox.QUESTION,
            fn: function (buttonId, text, opt) {
                if (buttonId === 'yes') {
                    Ext.Ajax.request({
                        url: Desktop.contextPath + "/api/system/user/enabled",
                        method: "POST",
                        params: {
                            userName: s[0].get("username"),
                            enabled: !s[0].get("enabled")
                        },
                        callback: function (option, success, resp) {
                            var result = Ext.decode(resp.responseText);
                            if (success && resp.status === 200 && result.success) {
                                me.store.load();
                            } else {
                                console.warn("enabled failed");
                                Ext.Msg.show({
                                    title: '操作失败',
                                    msg: result.message,
                                    buttons: Ext.Msg.OK,
                                    icon: Ext.MessageBox.ERROR
                                });
                            }
                        }
                    });
                }
            }
        });
    },

    /**
     * 重置接口密钥按钮事件
     */
    resetTokenBtnHandler: function(){
        var s = this.gridPanel.getSelectionModel().getSelections();

        if(s.length != 1){
            Ext.Msg.show({
                title: '警告',
                msg: "只能选择一条数据！",
                buttons: Ext.Msg.OK,
                icon: Ext.MessageBox.WARNING
            });
            return;
        }

        var me = this;

        Ext.Msg.show({
            title: '确认',
            msg: '请确认是否重置用户密钥，重置用户密钥后将可能导致用户不可下单?',
            scope: this,
            buttons: Ext.Msg.YESNO,
            icon: Ext.MessageBox.QUESTION,
            fn: function (buttonId, text, opt) {
                if (buttonId === 'yes') {
                    Ext.Ajax.request({
                        url: Desktop.contextPath + "/api/member/resetToken",
                        method: "POST",
                        params: {
                            userName: s[0].get("username")
                        },
                        callback: function (option, success, resp) {
                            var result = Ext.decode(resp.responseText);
                            if (success && resp.status === 200 && result.success) {
                                Ext.Msg.show({
                                    title: '成功',
                                    msg: result.message,
                                    buttons: Ext.Msg.OK,
                                    icon: Ext.MessageBox.INFO
                                });
                            }else {
                                console.warn("reset token failed");
                                Ext.Msg.show({
                                    title: '操作失败',
                                    msg: result.message,
                                    buttons: Ext.Msg.OK,
                                    icon: Ext.MessageBox.ERROR
                                });
                            }
                        }
                    });
                }
            }
        });

    },

    /**
     * 充值按钮事件
     */
    addBalanceBtnHandler: function () {
        var s = this.gridPanel.getSelectionModel().getSelections();

        var win = Ext.getCmp("AddBalanceWin");
        var me = this;
        if (!win) {
            win = new AddBalanceWindow({
                id: 'AddBalanceWin',
                userName: s[0].get("username"),
                modal: true,
                callback: function(){
                    me.store.load();
                }
            });
        }
        win.show();
    },

    /**
     * 扣款按钮事件
     */
    subBalanceBtnHandler: function () {
        var s = this.gridPanel.getSelectionModel().getSelections();

        var win = Ext.getCmp("DeductBalanceWin");
        var me = this;
        if (!win) {
            win = new DeductBalanceWindow({
                id: 'DeductBalanceWin',
                userName: s[0].get("username"),
                modal: true,
                callback: function(){
                    me.store.load();
                }
            });
        }
        win.show();
    }
});

Ext.reg('tab.userlist', UserListPanel);

