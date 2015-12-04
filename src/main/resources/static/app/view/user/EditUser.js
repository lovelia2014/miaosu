EditUserWindow = function (config) {
    var member = config.data || {};

    var formPanel = new Ext.FormPanel({
        region: 'center',
        labelWidth: 95, // label settings here cascade unless overridden
        url: Desktop.contextPath + '/api/member/update',
        border: false,
        //title: 'Simple Form',
        bodyStyle: 'padding:10px 10px 0px 10px', //padding：上、右、下、左
        //width: 350,
        defaults: {anchor: '-20', msgTarget: 'side', allowBlank: false},
        defaultType: 'textfield',
        items: [
            {
                fieldLabel: '会员名',
                readOnly: true,
                cls: 'x-item-disabled',
                name: 'username',
                minLength: 8,
                maxLength: 16,
                value: member.username
            }, {
                fieldLabel: '真实姓名',
                name: 'realName',
                minLength: 2,
                maxLength: 32,
                value: member.realName
            }, {
                fieldLabel: '会员折扣',
                name: 'discount',
                xtype: 'numberfield',
                vtype: 'discountVtype',
                readOnly: !IS_ADMIN,
                cls: IS_ADMIN ? '' : 'x-item-disabled',
                decimalPrecision: 4,
                allowDecimals: true,
                allowNegative: false,
                value: member.discount
            }, {
                fieldLabel: '身份证号码',
                name: 'idNumber',
                minLength: 14,
                maxLength: 18,
                value: member.idNumber
            }, {
                fieldLabel: '手机号码',
                name: 'mobilePhone',
                xtype: 'numberfield',
                minLength: 11,
                maxLength: 11,
                allowDecimals: false,
                allowNegative: false,
                value: member.mobilePhone
            }, {
                xtype: 'radiogroup',
                fieldLabel: '性别',
                anchor: '-180',
                items: [
                    {boxLabel: '女', name: 'sex', margins: '0 5 0 5', inputValue: 0, checked: member.sex == 0},
                    {boxLabel: '男', name: 'sex', margins: '0 5 0 5', inputValue: 1, checked: member.sex == 1}
                ]
            }, new Ext.ux.ChinaRegionField({
                fieldLabel: '所在城市',
                //width: 600,
                provinceName: 'province',
                cityName: 'city',
                areaName: 'area',
                provinceValue: member.province,
                cityValue: member.city,
                areaValue: member.area
            }), {
                fieldLabel: '详细地址',
                name: 'detailAddr',
                maxLength: 64,
                value: member.detailAddr
            }, {
                fieldLabel: 'Token',
                name: 'token',
                readOnly: !IS_ADMIN,
                cls: IS_ADMIN ? '' : 'x-item-disabled',
                allowBlank: true,
                value: member.token
            }, {
                fieldLabel: '最近登录时间',
                name: 'lastLoginTime',
                readOnly: true,
                cls: 'x-item-disabled',
                allowBlank: true,
                value: member.lastLoginTime
            }, {
                fieldLabel: '最近登录IP',
                name: 'lastLoginIp',
                readOnly: true,
                cls: 'x-item-disabled',
                allowBlank: true,
                value: member.lastLoginIp
            }, {
                fieldLabel: '注册时间',
                name: 'regTime',
                readOnly: true,
                cls: 'x-item-disabled',
                allowBlank: true,
                value: member.regTime
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
        title: '修改会员信息',
        width: 405,
        height: 420,
        closable: true,
        autoScroll: true,
        iconCls: 'icon-user-add',
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
    EditUserWindow.superclass.constructor.call(this, allConfig);
};

Ext.extend(EditUserWindow, Ext.Window, {
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
                                    console.warn("update failed");
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

Ext.reg('win.edituser', EditUserWindow);

Ext.apply(Ext.form.VTypes, {
    discountVtype: function (val, field) {
        var str = val.toString();
        return val <= 1 && str.length <= 5;
    },
    discountVtypeText: '折扣不能大于1，且小数点后最多3位'
});