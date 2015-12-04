EditBlackNumWindow = function (config) {
    var blackNum = config.data || {};
    var isAdd = config.isAdd || false;

    var formPanel = new Ext.FormPanel({
        region: 'center',
        labelWidth: 95, // label settings here cascade unless overridden
        url: isAdd ? Desktop.contextPath + '/api/blacknum/create' : Desktop.contextPath + '/api/blacknum/update',
        border: false,
        bodyStyle: 'padding:10px 10px 0px 10px', //padding：上、右、下、左
        //width: 350,
        defaults: {anchor: '-20', msgTarget: 'side', allowBlank: false},
        defaultType: 'textfield',
        items: [
            {
                fieldLabel: '号码',
                readOnly: !isAdd,
                emptyText: '请输入号段或号码',
                minLength: 3,
                maxLength: 11,
                name: 'number',
                value: blackNum.number
            }, {
                fieldLabel: '备注',
                name: 'remark',
                minLength: 3,
                maxLength: 64,
                value: blackNum.remark
            }
        ]
    });

    this.resultPanel = new Ext.Panel({
        bodyStyle: 'padding:10px 10px 10px 10px', //padding：上、右、下、左
        region: 'south',
        height: 30,
        border: false,
        html: ''
    });

    var cfg = {
        title: isAdd ? '新增黑名单信息' : '修改黑名单信息',
        width: 405,
        height: 160,
        closable: true,
        autoScroll: true,
        iconCls: isAdd ? 'icon-add' : 'icon-edit',
        //closeAction: 'hide',
        resizable: false,
        shim: false,
        animCollapse: false,
        constrainHeader: true,
        layout: 'border',
        //margins: '35 5 5 0',
        containerScroll: true,
        items: [formPanel, this.resultPanel],
        buttons: [{
            text: '确  认',
            scope: this,
            handler: function () {
                this.submitForm();
            }
        }, {
            text: '关  闭',
            scope: this,
            handler: function () {
                this.close();
            }
        }]
    };

    this.formPanel = formPanel;

    var allConfig = Ext.applyIf(config || {}, cfg);
    EditBlackNumWindow.superclass.constructor.call(this, allConfig);
};

Ext.extend(EditBlackNumWindow, Ext.Window, {
    submitForm: function () {
        if (this.formPanel.getForm().isValid()) {
            var me = this;
            Ext.Msg.show({
                title: '确认',
                msg: '请确认是否保存?',
                scope: this,
                buttons: Ext.Msg.YESNO,
                icon: Ext.MessageBox.QUESTION,
                fn: function (buttonId, text, opt) {
                    if (buttonId === 'yes') {
                        var frm = me.formPanel.getForm();
                        var resultPanel = me.resultPanel;

                        Ext.Ajax.request({
                            url: frm.url,
                            method: "POST",
                            jsonData: frm.getValues(),
                            params: {},
                            callback: function (option, success, resp) {
                                var result = Ext.decode(resp.responseText);
                                var msg = "";
                                if (success && resp.status === 200) {
                                    if (result.success) {
                                        msg = "<div style='color: green; text-align: center;'>" + result.message + "</div>";
                                    }else{
                                        msg = "<div style='color: red; text-align: center;'>" + result.message + "</div>";
                                    }

                                } else {
                                    console.warn("submit failed");
                                    msg = "<div style='color: red; text-align: center;'>" + result.message + "</div>";
                                }
                                resultPanel.update(msg);
                                me.callback();
                            }
                        });
                    }
                }
            });
        }

    }
});

Ext.reg('win.editblacknum', EditBlackNumWindow);