AddBalanceWindow = function (config) {

    this.formPanel = new Ext.form.FormPanel({
        labelWidth: 75, // label settings here cascade unless overridden
        url: Desktop.contextPath + '/api/member/recharge',
        region: 'center',
        frame: false,
        border: false,
        labelAlign: 'right',
        bodyStyle: 'padding:10px 10px 0',
        //width: 350,
        defaults: {
            width: 150,
            blankText: '不能为空',
            msgTarget: 'side'
        },
        defaultType: 'textfield',
        items: [{
            fieldLabel: '会员名',
            name: 'userName',
            value: config.userName,
            readOnly: true,
            minLength: 8,
            maxLength: 16,
            allowBlank: false
        }, {
            fieldLabel: '充值金额',
            xtype: 'numberfield',
            name: 'amount',
            decimalPrecision: 4,
            allowDecimals: true,
            allowNegative: false,
            allowBlank: false
        }, {
            fieldLabel: '备注',
            name: 'remark',
            value: '账户充值',
            maxLength: 60,
            allowBlank: true
        }]
    });


    this.resultPanel = new Ext.Panel({
        bodyStyle: 'padding:10px 10px 10px 10px', //padding：上、右、下、左
        region: 'south',
        height: 20,
        border: false,
        html: ''
    });

    var cfg = {
        iconCls: 'icon-balance-add',
        resizable: false,
        title: '账户充值',
        modal: true,
        layout: 'border',
        width: 300,
        //autoHeight: true,
        height: 182,
        closeAction: 'hide',
        plain: true,
        items: [this.formPanel, this.resultPanel],
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
                this.closeWin();
            }
        }]
    };

    var allConfig = Ext.applyIf(config || {}, cfg);
    AddBalanceWindow.superclass.constructor.call(this, allConfig);
};

Ext.extend(AddBalanceWindow, Ext.Window, {
    closeWin: function () {
        this.close();
    },
    submitForm: function () {
        if (this.formPanel.getForm().isValid()) {
            var me = this;
            Ext.Msg.show({
                title: '确认',
                msg: '请确认是否继续?',
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
                            params: frm.getValues(),
                            callback: function (option, success, resp) {
                                var result = Ext.decode(resp.responseText);
                                var msg = "";
                                if (success && resp.status === 200) {
                                    if (result.success) {
                                        msg = "<div style='color: green; text-align: center;'>" + result.message + "</div>";
                                        me.close();
                                        if(me.callback){
                                            me.callback();
                                        }
                                        return;
                                    }else{
                                        msg = "<div style='color: red; text-align: center;'>" + result.message + "</div>";
                                    }

                                } else {
                                    msg = "<div style='color: red; text-align: center;'>" + result.message + "</div>";
                                }
                                resultPanel.update(msg);
                            }
                        });
                    }
                }
            });
        }

    }
});