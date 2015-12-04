/**
 * Created by angus
 */
BillStatListPanel = function (config) {

    var serviceUrl = Desktop.contextPath + "/api/billstat/search";
    var me = this;

    var now = new Date();
    var yesterday = new Date(now.getTime() - 24 * 60 * 60 * 1000); // 昨天

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
                width: 60,
                xtype: 'label',
                text: '统计日期：'
            },
            {
                x: 70,
                y: 10,
                width: 140,
                id: 'billStatList_begin',
                name: 'begin',
                vtype: 'date2Vtype',
                value: Util.formatDate2(yesterday),
                cls: 'Wdate',
                listeners: {
                    render:function(p){
                        p.getEl().on('click',function(){
                            WdatePicker({el:'billStatList_begin', dateFmt: 'yyyy-MM-dd'});
                        });
                    }
                }
            },
            {
                x: 215,
                y: 12,
                width: 15,
                xtype: 'label',
                text: '-'
            },
            {
                x: 230,
                y: 10,
                width: 140,
                fieldLabel: '结束',
                id: 'billStatList_end',
                name: 'end',
                vtype: 'date2Vtype',
                value: Util.formatDate2(yesterday),
                cls: 'Wdate',
                listeners: {
                    render:function(p){
                        p.getEl().on('click',function(){
                            WdatePicker({el:'billStatList_end', dateFmt: 'yyyy-MM-dd'});
                        });
                    }
                }
            },
            {
                x: 380,
                y: 12,
                width: 60,
                xtype: 'label',
                text: '会员名：'
            },
            {
                x: 440,
                y: 10,
                width: 130,
                fieldLabel: '会员名',
                name: 'username',
                enableKeyEvents: true,
                readOnly: !IS_ADMIN,
                cls: IS_ADMIN ? '' : 'x-item-disabled',
                scope: this,
                listeners: {
                    scope: this,
                    keydown: function (field, event) {
                        if (event.keyCode == 13) { //回车键事件
                            me.searchBtnClick();
                        }
                    }
                },
                value: (IS_ADMIN ? "" : CURR_USER)
            },
            {
                x: 660,
                y: 10,
                width: 70,
                xtype: 'button',
                id: 'BillStatListSearchBtn',
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
        fields: ['id', 'statDate', 'username', 'addSum', 'paySum', 'refundSum', 'rewardSum', 'othersSum'],
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
        columns: [
            new Ext.grid.RowNumberer({width: 25}),
            {id: 'id', align: 'center', header: "编号", width: 50, sortable: true, dataIndex: 'id'},
            {id: 'statDate', align: 'center', header: "统计日期", width: 100, sortable: true, dataIndex: 'statDate'},
            {id: 'username', align: 'center', header: "会员名", width: 80, sortable: true, dataIndex: 'username'},
            {id: 'addSum', align: 'center', header: "充值金额", width: 100, sortable: true, dataIndex: 'addSum', renderer: function (text) {return '<span style="color: green">' + text + '</span>'}},
            {id: 'paySum', align: 'center', header: "支付金额", width: 100, sortable: true, dataIndex: 'paySum', renderer: function (text) {return '<span style="color: sienna">' + text + '</span>'}},
            {id: 'refundSum', align: 'center', header: "退款金额", width: 100, sortable: true, dataIndex: 'refundSum', renderer: function (text) {return '<span style="color: #ffb941">' + text + '</span>'}},
            {id: 'rewardSum', align: 'center', header: "奖励金额", width: 100, sortable: true, dataIndex: 'rewardSum', renderer: function (text) {return '<span style="color: red">' + text + '</span>'}},
            {id: 'othersSum', align: 'center', header: "其他金额", width: 100, sortable: true, dataIndex: 'othersSum', renderer: function (text) {return '<span style="color: #0ea8c5">' + text + '</span>'}}
        ],
        bbar: pagingbar
    });

    var statGrid = new Ext.grid.PropertyGrid({
        title: '数据汇总',
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
            a_totalAddSum: function (text) {return '<span style="color: green">' + text + '</span>'},
            b_totalPaySum: function (text) {return '<span style="color: sienna">' + text + '</span>'},
            c_totalRefundSum: function (text) {return '<span style="color: #ffb941">' + text + '</span>'},
            d_totalRewardSum: function (text) {return '<span style="color: red">' + text + '</span>'},
            e_totalOthersSum: function (text) {return '<span style="color: #0ea8c5">' + text + '</span>'}
        },
        clicksToEdit: 2,
        propertyNames: {
            a_totalAddSum: '充值总金额',
            b_totalPaySum: '支付总金额',
            c_totalRefundSum: '退款总金额',
            d_totalRewardSum: '奖励总金额',
            e_totalOthersSum: '其他总金额'
        },
        viewConfig : {
            forceFit: true,
            scrollOffset: 2 // the grid will never have scrollbars
        }
    });

    Ext.Ajax.request({
        url: Desktop.contextPath + "/api/billstat/sum",
        method: "POST",
        params: formPanel.getForm().getFieldValues(),
        callback: function (option, success, resp) {
            var result = Ext.decode(resp.responseText);
            if (success && resp.status === 200 && result.success) {
                statGrid.setSource(result.data);
                return;
            }
            console.warn("load order sum failed");
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
            gridPanel,
            statGrid
        ]
    };

    // 设置为成员属性
    this.formPanel = formPanel;
    this.gridPanel = gridPanel;
    this.statGrid = statGrid;
    this.store = store;

    var allConfig = Ext.applyIf(config || {}, cfg);
    BillStatListPanel.superclass.constructor.call(this, allConfig);
};

Ext.extend(BillStatListPanel, Ext.Panel, {

    /**
     * 搜索按钮点击动作
     */
    searchBtnClick: function () {
        var frm = this.formPanel.getForm();
        if (frm.isValid()) {
            var beginStr = frm.findField("begin").getValue();
            var endStr = frm.findField("end").getValue();

            var begin = Date.parseDate(beginStr, "Y-m-d");
            var end = Date.parseDate(endStr, "Y-m-d");

            //最大只支持跨度1天
            if (end.getTime() - begin.getTime() > 1000 * 60 * 60 * 24 * 60) {
                alert("时间范围最大只支持跨度60天");
                return;
            }

            var params = frm.getFieldValues();
            params.start = 0;
            params.limit = Desktop.pageSize;

            var btn = Ext.getCmp("BillStatListSearchBtn");
            btn.disabled = true;
            this.loadGridData(params, function () {
                btn.disabled = false;
            });

            var statGrid = this.statGrid;
            Ext.Ajax.request({
                url: Desktop.contextPath + "/api/billstat/sum",
                method: "POST",
                params: params,
                callback: function (option, success, resp) {
                    var result = Ext.decode(resp.responseText);
                    if (success && resp.status === 200 && result.success) {
                        statGrid.setSource(result.data);
                        return;
                    }
                    console.warn("load order sum failed");
                }
            });
        }
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
    }
});

Ext.reg('tab.billstatlist', BillStatListPanel);

Ext.apply(Ext.form.VTypes, {
    date2Vtype: function(val, field){
        var date = Date.parseDate(val, "Y-m-d");
        return date ? true : false;
    },
    date2VtypeText: '请使用"yyyy-MM-dd"的时间格式'
});

