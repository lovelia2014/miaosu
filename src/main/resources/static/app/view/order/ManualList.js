/**
 * Created by angus
 */
ManualListPanel = function (config) {

    var serviceUrl = Desktop.contextPath + "/api/order/search";
    
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
                y: 40 + 2,
                width: 60,
                xtype: 'label',
                text: '会员名：'
            },
            {
                x: 70,
                y: 40,
                width: 90,
                fieldLabel: '会员名',
                name: 'userId',
                readOnly: true,
                cls: IS_ADMIN ? '' : 'x-item-disabled',
                enableKeyEvents: true,
                scope: this,
                listeners: {
                    scope: this,
                    keydown: function (field, event) {
                        if (event.keyCode == 13) { //回车键事件
                            me.searchBtnClick();
                        }
                    }
                },
                value: 'miaosu2015'
            },
            {
                x: 200,
                y: 40 + 2,
                width: 60,
                xtype: 'label',
                text: '充值号码：'
            },
            {
                x: 260,
                y: 40,
                width: 130,
                fieldLabel: '充值号码',
                name: 'phone',
                value: '',
                xtype: 'numberfield',
                allowDecimals: false,
                minLength: 11,
                maxLength: 11,
                allowNegative: false
            },
            {
                x: 430,
                y: 40 + 2,
                width: 60,
                xtype: 'label',
                text: '到账类型：'
            },
            {
                x: 490,
                y: 40,
                width: 80,
                xtype: 'combo',
                typeAhead: true,
                triggerAction: 'all',
                lazyRender: true,
                mode: 'local',
                hiddenName: 'type',
                store: new Ext.data.ArrayStore({
                    id: 0,
                    fields: [
                        'id',
                        'text'
                    ],
                    data: [[0, '立即到账'], [1, '下月生效']]
                }),
                forceSelection: true,
                //editable :false,
                emptyText: '请选择',
                valueField: 'id',
                displayField: 'text',
                value: 0,
            },
            {
                x: 610,
                y: 40 + 2,
                width: 60,
                xtype: 'label',
                text: '订购商品：'
            },
            {
                x: 670,
                y: 40,
                width: 170,
                xtype: 'combo',
                typeAhead: true,
                triggerAction: 'all',
                lazyRender: true,
                mode: 'local',
                hiddenName: 'productId',
                store: new Ext.data.ArrayStore({
                    id: 0,
                    fields: [
                        'id',
                        'text'
                    ],
                    data: [['G00020', '后向全国国内流量包-20MB'], ['G00050', '后向全国国内流量包-50MB'], ['G00100', '后向全国国内流量包-100MB']
                        , ['G00200', '后向全国国内流量包-200MB'], ['G00500', '后向全国国内流量包-500MB']]
                }),
                forceSelection: true,
                //editable :false,
                emptyText: '请选择充值产品',
                valueField: 'id',
                displayField: 'text',
                value : 'G00020'
            },
            {
                x: 880,
                y: 40,
                width: 70,
                xtype: 'button',
                id: 'addOrderBtn',
                text: '提交',
                scope: this,
                handler: this.addOrderClick
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
        fields: ['id', 'username', 'phone', 'effectType', 'province', 'externalId', 'productId', 'productName', 'productPrice', 'price', 'payState',
            'payFailedReason', 'payId', 'refundId', 'rechargeId', 'rechargeSystem', 'rechargeState', 'rechargeFailedReason', 'notifyUrl', 'payTime',
            'rechargeTime', 'createTime', 'rechargeEndTime'],
        listeners: {
            scope: this,
            "beforeload": function (store) {
                var params = me.formPanel.getForm().getFieldValues();
                params.limit = Desktop.pageSize;
                params.username = params.userId;
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

/*    var tbar = [];
    if (IS_ADMIN) {
        tbar = [{
            text: '充值核实',
            iconCls: 'icon-bill-checkin',
            scope: this,
            ref: '../checkinRechargeStatusBtn',
            disabled: true,
            handler: this.checkinRechargeStatus
        }, {
            text: '确认失败',
            iconCls: 'icon-confirm-failed',
            scope: this,
            ref: '../setToRechargeFailedBtn',
            disabled: true,
            handler: this.setToRechargeFailed
        }];
    }*/

    var gridColumns = [];

    gridColumns.push(new Ext.grid.RowNumberer({width: 25}));
    gridColumns.push({id: 'id', align: 'center', header: "订单编号", width: 120, sortable: true, dataIndex: 'id'});
    gridColumns.push({id: 'username', align: 'center', header: "用户名", width: 90, sortable: true, dataIndex: 'username'});
    gridColumns.push({id: 'phone', align: 'center', header: "号码", width: 90, sortable: true, dataIndex: 'phone'});
    gridColumns.push({id: 'price', align: 'center', header: "订单金额", width: 70, sortable: true, dataIndex: 'price'});
    gridColumns.push({
        id: 'effectType', align: 'center', header: "到账类型", width: 70, sortable: true, dataIndex: 'effectType', hidden: true,
        renderer: function (text) {
            switch (text) {
                case 0:
                    return '立即';
                case 1:
                    return '下月';
                default:
                    return text;
            }
        }
    });
    gridColumns.push({id: 'province', align: 'center', header: "省份", width: 70, sortable: true, dataIndex: 'province'});
    gridColumns.push({id: 'externalId', align: 'center', header: "外部单号", width: 120, sortable: true, dataIndex: 'externalId'});
    gridColumns.push({id: 'productId', align: 'center', header: "商品代码", width: 90, sortable: true, dataIndex: 'productId'});
    gridColumns.push({id: 'productName', align: 'center', header: "商品名称", width: 120, sortable: true, dataIndex: 'productName', hidden: true});
    gridColumns.push({
        id: 'payState', align: 'center', header: "支付状态", width: 70, sortable: true, dataIndex: 'payState',
        renderer: function (text) {
            switch (text) {
                case 0:
                    return '<span style="color: #ffb941">' + '待支付' + '</span>';
                case 1:
                    return '<span style="color: aqua">' + '支付中' + '</span>';
                case 2:
                    return '<span style="color: green">' + '已支付' + '</span>';
                case 3:
                    return '<span style="color: red">' + '支付失败' + '</span>';
                case 4:
                    return '<span style="color: blue">' + '退款中' + '</span>';
                case 5:
                    return '<span style="color: purple">' + '已退款' + '</span>';
                default:
                    return text;
            }
        }
    });
    gridColumns.push({id: 'payId', align: 'center', header: "支付单号", width: 100, sortable: true, dataIndex: 'payId', hidden: true});
    gridColumns.push({id: 'payFailedReason', align: 'center', header: "支付失败原因", width: 120, sortable: true, dataIndex: 'payFailedReason', hidden: true});
    gridColumns.push({id: 'refundId', align: 'center', header: "退款单号", width: 100, sortable: true, dataIndex: 'refundId', hidden: true});
    if(IS_ADMIN){
        gridColumns.push({id: 'rechargeId', align: 'center', header: "充值单号", width: 100, sortable: true, dataIndex: 'rechargeId', hidden: true});
    }
    gridColumns.push({
        id: 'rechargeState', align: 'center', header: "充值状态", width: 70, sortable: true, dataIndex: 'rechargeState',
        renderer: function (text) {
            switch (text) {
                case 0:
                    return '<span style="color: #ffb941">' + '待充值' + '</span>';
                case 1:
                    return '<span style="color: aqua">' + '充值中' + '</span>';
                case 2:
                    return '<span style="color: green">' + '充值成功' + '</span>';
                case 3:
                    return '<span style="color: red">' + '充值失败' + '</span>';
                default:
                    return text;
            }
        }
    });
    gridColumns.push({id: 'rechargeFailedReason', align: 'center', header: "充值失败原因", width: 140, sortable: true, dataIndex: 'rechargeFailedReason'});
    gridColumns.push({id: 'notifyUrl', align: 'center', header: "回调地址", width: 140, sortable: true, dataIndex: 'notifyUrl', hidden: true});
    gridColumns.push({id: 'createTime', align: 'center', header: "创建时间", width: 140, sortable: true, dataIndex: 'createTime'});
    gridColumns.push({id: 'payTime', align: 'center', header: "支付时间", width: 140, sortable: true, dataIndex: 'payTime', hidden: true});
    gridColumns.push({id: 'rechargeTime', align: 'center', header: "充值时间", width: 140, sortable: true, dataIndex: 'rechargeTime'});
    gridColumns.push({id: 'rechargeEndTime', align: 'center', header: "充值结束时间", width: 140, sortable: true, dataIndex: 'rechargeEndTime'});

    var detailGrid = new Ext.grid.PropertyGrid({
        title: '详细信息',
        split: true,
        collapseMode: 'mini',
        maxWidth: 300,
        minWidth: 200,
        region: 'east',
        width: 300,
        loadMask: {
            msg: "努力加载数据中，请稍后..."
        },
        //hideHeaders: true,
        //disabled: true,
        customRenderers: {
            e_effectType: function (text) {
                switch (text) {
                    case 0:
                        return '立即';
                    case 1:
                        return '下月';
                    default:
                        return text;
                }
            },
            j_payState: function (text) {
                switch (text) {
                    case 0:
                        return '<span style="color: #ffb941">' + '待支付' + '</span>';
                    case 1:
                        return '<span style="color: aqua">' + '支付中' + '</span>';
                    case 2:
                        return '<span style="color: green">' + '已支付' + '</span>';
                    case 3:
                        return '<span style="color: red">' + '支付失败' + '</span>';
                    case 4:
                        return '<span style="color: blue">' + '退款中' + '</span>';
                    case 5:
                        return '<span style="color: purple">' + '已退款' + '</span>';
                    default:
                        return text;
                }
            },
            o_rechargeState: function (text) {
                switch (text) {
                    case 0:
                        return '<span style="color: #ffb941">' + '待充值' + '</span>';
                    case 1:
                        return '<span style="color: aqua">' + '充值中' + '</span>';
                    case 2:
                        return '<span style="color: green">' + '充值成功' + '</span>';
                    case 3:
                        return '<span style="color: red">' + '充值失败' + '</span>';
                    default:
                        return text;
                }
            }
        },
        propertyNames: {
            a_id: '订单编号',
            b_username: '用户名',
            c_phone: '号码',
            d_price: '订单金额',
            e_effectType: '到账类型',
            f_province: '省份',
            g_externalId: '外部单号',
            h_productId: '商品代码',
            i_productName: '商品名称',
            j_payState: '支付状态',
            k_payFailedReason: '支付失败原因',
            l_payId: '支付单号',
            m_refundId: '退款单号',
            n_rechargeId: '充值单号',
            o_rechargeState: '充值状态',
            p_rechargeFailedReason: '充值失败原因',
            q_notifyUrl: '回调地址',
            r_createTime: '创建时间',
            s_payTime: '支付时间',
            t_rechargeTime: '充值时间',
            u_rechargeEndTime: '充值结束时间'
        },
        viewConfig : {
            forceFit: true,
            scrollOffset: 2 // the grid will never have scrollbars
        }
    });

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
                    gridPanel.checkinRechargeStatusBtn.setDisabled(sm.getCount() < 1);
                    gridPanel.setToRechargeFailedBtn.setDisabled(sm.getCount() < 1);
                }
            }
        }),
        //tbar: tbar,
        columns: gridColumns,
        bbar: pagingbar,
        listeners: {
            rowclick: function(grid, rowIndex, e){
                var record = grid.store.getAt(rowIndex);
                detailGrid.setSource({
                    a_id: record.get("id")||"",
                    b_username: record.get("username")||"",
                    c_phone: record.get("phone")||"",
                    d_price: record.get("price")||"",
                    e_effectType: record.get("effectType"),
                    f_province: record.get("province")||"",
                    g_externalId: record.get("externalId")||"",
                    h_productId: record.get("productId")||"",
                    i_productName: record.get("productName")||"",
                    j_payState: record.get("payState"),
                    k_payFailedReason: record.get("payFailedReason")||"",
                    l_payId: record.get("payId")||"",
                    m_refundId: record.get("refundId")||"",
                    n_rechargeId: IS_ADMIN ? record.get("rechargeId")||"" : null,
                    o_rechargeState: record.get("rechargeState"),
                    p_rechargeFailedReason: record.get("rechargeFailedReason")||"",
                    q_notifyUrl: record.get("notifyUrl")||"",
                    r_createTime: record.get("createTime")||"",
                    s_payTime: record.get("payTime")||"",
                    t_rechargeTime: record.get("rechargeTime")||"",
                    u_rechargeEndTime: record.get("rechargeEndTime")||""
                });
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
                height: 98,
                maxHeight: 80,
                border: false,
                split: true,
                collapseMode: 'mini',
                region: 'north',
                layout: 'border',
                items: [formPanel]
            },
            gridPanel,
            detailGrid
        ]
    };

    // 设置为成员属性
    this.formPanel = formPanel;
    this.gridPanel = gridPanel;
    this.store = store;

    var allConfig = Ext.applyIf(config || {}, cfg);
    ManualListPanel.superclass.constructor.call(this, allConfig);
};

Ext.extend(ManualListPanel, Ext.Panel, {

    /**
     * 搜索按钮点击动作
     */
    addOrderClick: function () {
        var frm = this.formPanel.getForm();
        if (frm.isValid()) {
            var params = frm.getFieldValues();
            var r = Math.floor(Math.random() * (999 - 100 + 1)) + 100;
            params.transId = 'S_N' + new Date().format('YmhHis') + r;
            params.province = 'js';
            var btn = Ext.getCmp("addOrderBtn");
            btn.disabled = true;
            
            Ext.Ajax.request({
            	url:Desktop.contextPath + "/api/member/get/" + params.userId,
            	method:"GET",
            	callback:function(option, success, resp){
            		var result = Ext.decode(resp.responseText);
                    if (success && resp.status === 200 && result.success) {
                    	//成功获取token
                    	
                    	var array = new Array();
                    	array[0] = 'userId';
                    	array[1] = 'phone';
                    	array[2] = 'type';
                    	array[3] = 'productId';
                    	array[4] = 'transId';
                    	array[5] = 'province';
                    	array.sort();
                    	var s = array[0] + params[array[0]] + array[1] + params[array[1]] + array[2] + params[array[2]] + array[3] + params[array[3]] + 
                    	array[4] + params[array[4]] + array[5] + params[array[5]] + result.data.token; 
                    	//md5加密
                    	params.sign = Util.MD5(s);
                    	Ext.Ajax.request({
                            url: Desktop.contextPath + "/openapi/order/create",
                            method: "POST",
                            params: params,
                            callback: function (option, success, resp) {
                                var result = Ext.decode(resp.responseText);
                                if (success && resp.status === 200 && result.success) {
                                    //me.store.load();
                                	 Ext.Msg.show({
                                         title: '操作成功',
                                         msg: result.message,
                                         buttons: Ext.Msg.OK,
                                         icon: Ext.MessageBox.OK
                                     });
                                    return;
                                }
                                Ext.Msg.show({
                                    title: '操作失败',
                                    msg: result.message,
                                    buttons: Ext.Msg.OK,
                                    icon: Ext.MessageBox.ERROR
                                });
                            }
                        });
                    }
                    else
                    {
                    	Ext.Msg.show({
                    		title: '操作失败',
                    		msg: result.message,
                    		buttons: Ext.Msg.OK,
                    		icon: Ext.MessageBox.ERROR
                    	});
                    }
            	}
            });
        };
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
                if (!success) {
                    window.location.reload(true);
                }
                // do noting
                next();
            }
        });
    },

    /**
     * 充值核实按钮事件
     */
    checkinRechargeStatus: function () {
        var s = this.gridPanel.getSelectionModel().getSelections();
        var ids = [];
        for (var i = 0, r; r = s[i]; i++) {
            ids.push(r.get("id"));
        }

        if (ids.length == 0) {
            return;
        }

        var me = this;

        Ext.Ajax.request({
            url: Desktop.contextPath + "/api/order/checkRechargeState",
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
                console.warn("checkRechargeState failed");
                Ext.Msg.show({
                    title: '操作失败',
                    msg: result.message,
                    buttons: Ext.Msg.OK,
                    icon: Ext.MessageBox.ERROR
                });
            }
        });

    },

    /**
     * 确认失败按钮事件
     */
    setToRechargeFailed: function () {
        var s = this.gridPanel.getSelectionModel().getSelections();
        var ids = [];
        for (var i = 0, r; r = s[i]; i++) {
            ids.push(r.get("id"));
        }

        if (ids.length == 0) {
            return;
        }

        var me = this;

        Ext.MessageBox.show({
            title: '确认',
            msg: '请确认是将选中的' + ids.length + '条记录设置为失败？失败订单将会自动进行退款！',
            width:300,
            buttons: Ext.MessageBox.YESNO,
            multiline: true,
            fn: function (buttonId, text, opt) {
                if (buttonId === 'yes') {
                    Ext.Ajax.request({
                        url: Desktop.contextPath + "/api/order/setToRechargeFailed",
                        method: "POST",
                        params: {
                            failedReason: text,
                            ids: ids
                        },
                        callback: function (option, success, resp) {
                            var result = Ext.decode(resp.responseText);
                            if (success && resp.status === 200 && result.success) {
                                me.store.load();
                                return;
                            }
                            console.warn("setToRechargeFailed failed");
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

Ext.reg('tab.manuallist', ManualListPanel);

Ext.apply(Ext.form.VTypes, {
    dateVtype: function (val, field) {
        var date = Date.parseDate(val, "Y-m-d H:i:s");
        return date ? true : false;
    },
    dateVtypeText: '请使用"yyyy-MM-dd HH:mm:ss"的时间格式'
});

