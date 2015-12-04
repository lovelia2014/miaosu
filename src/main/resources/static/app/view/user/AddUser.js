AddUserWindow = function (config) {

    var formPanel = new Ext.FormPanel({
        region: 'center',
        labelWidth: 75, // label settings here cascade unless overridden
        url: Desktop.contextPath + '/api/member/create',
        border: false,
        //title: 'Simple Form',
        bodyStyle: 'padding:10px 10px 0px 10px', //padding：上、右、下、左
        //width: 350,
        defaults: {anchor: '-20', msgTarget: 'side', allowBlank: false},
        defaultType: 'textfield',
        items: [
            {
                xtype: 'compositefield',
                fieldLabel: '会员名',
                items: [{
                    xtype: 'textfield',
                    fieldLabel: '会员名',
                    minLength: 8,
                    maxLength: 16,
                    name: 'username'
                }, {
                    xtype: 'button',
                    text: '自动生成',
                    margins: '0 5 0 0',
                    scope: this,
                    handler: this.generateMemberName
                }]
            }, {
                fieldLabel: '真实姓名',
                anchor: '-80',
                minLength: 2,
                maxLength: 32,
                name: 'realName'
            }, {
                fieldLabel: '会员折扣',
                anchor: '-80',
                name: 'discount',
                value: 1,
                xtype: 'numberfield',
                vtype: 'discountVtype',
                decimalPrecision: 4,
                allowDecimals: true,
                allowNegative: false
            }, {
                fieldLabel: '身份证号码',
                anchor: '-80',
                minLength: 14,
                maxLength: 18,
                name: 'idNumber'
            }, {
                fieldLabel: '手机号码',
                anchor: '-80',
                name: 'mobilePhone',
                xtype: 'numberfield',
                allowDecimals: false,
                minLength: 11,
                maxLength: 11,
                allowNegative: false
            }, {
                xtype: 'radiogroup',
                fieldLabel: '性别',
                anchor: '-180',
                items: [
                    {boxLabel: '女', name: 'sex', margins: '0 5 0 5', inputValue: 0},
                    {boxLabel: '男', name: 'sex', margins: '0 5 0 5', inputValue: 1, checked: true}
                ]
            }, new Ext.ux.ChinaRegionField({
                fieldLabel: '所在城市',
                //width: 600,
                provinceName: 'province',
                cityName: 'city',
                areaName: 'area'
            }), {
                fieldLabel: '详细地址',
                maxLength: 64,
                name: 'detailAddr'
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
        title: '添加会员信息',
        width: 385,
        height: 310,
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
    AddUserWindow.superclass.constructor.call(this, allConfig);
};

Ext.extend(AddUserWindow, Ext.Window, {
    generateMemberName: function () {
        var frm = this.formPanel.getForm();
        var usernameField = frm.findField("username");

        Ext.Ajax.request({
            url: Desktop.contextPath + "/api/member/generateMemberName",
            method: "GET",
            params: {},
            callback: function (option, success, resp) {
                if (success && resp.status === 200) {
                    var data = resp.responseText;
                    usernameField.setValue(data);
                } else {
                    console.warn("generate memberName failed")
                }
            }
        });
    },
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
                                    console.warn("generate memberName failed");
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

Ext.reg('win.adduser', AddUserWindow);

Ext.apply(Ext.form.VTypes, {
    discountVtype: function (val, field) {
        var str = val.toString();
        return val <= 1 && str.length <= 5;
    },
    discountVtypeText: '折扣不能大于1，且小数点后最多3位'
});