EditTokenWin = function (config) {

    this.formPanel = new Ext.form.FormPanel({
        labelWidth: 75, // label settings here cascade unless overridden
        url: Desktop.contextPath + '/api/member/changeToken',
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
            fieldLabel: '接口密钥',
            name: 'token',
            allowBlank: false
        },{
            fieldLabel: '密  码',
            inputType: 'password',
            name: 'password',
            allowBlank: false
        }]
    });

    var cfg = {
        resizable: false,
        title: '接口密钥',
        modal: true,
        layout: 'fit',
        width: 300,
        //autoHeight: true,
        height: 165,
        closeAction: 'hide',
        plain: true,
        items: [this.formPanel],
        buttons: [{
            text: '修  改',
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
    EditTokenWin.superclass.constructor.call(this, allConfig);
};

Ext.extend(EditTokenWin, Ext.Window, {
    closeWin: function () {
        this.hide();
    },
    submitForm: function () {
        if (this.formPanel.getForm().isValid()) {
            var me = this;
            Ext.Msg.show({
                title: '确认',
                msg: '密码修改完成后，您需要使用新密码重新登录，是否继续?',
                scope: this,
                buttons: Ext.Msg.YESNO,
                icon: Ext.MessageBox.QUESTION,
                fn: function (buttonId, text, opt) {
                    if (buttonId === 'yes') {
                        me.formPanel.getForm().submit({
                            clientValidation: true,
                            success: function (form, action) {
                                Ext.Msg.alert('Success', action.result.message);
                            },
                            failure: function (form, action) {
                                switch (action.failureType) {
                                    case Ext.form.Action.CLIENT_INVALID:
                                        Ext.Msg.alert('失败', '请检查表单字段是正确');
                                        break;
                                    case Ext.form.Action.CONNECT_FAILURE:
                                        console.warn('Ajax communication failed.');
                                        Ext.Msg.alert('失败', 'Ajax communication failed');
                                        break;
                                    case Ext.form.Action.SERVER_INVALID:
                                        Ext.Msg.alert('失败', action.result.message);
                                }
                            }
                        });
                    }
                }
            });
        }

    }
});

Ext.apply(Ext.form.VTypes, {
    passwordConfirm: function (val, field) {
        if (field.initialPassField) {
            var pwd = Ext.getCmp(field.initialPassField);
            return (val == pwd.getValue());
        }
        return true;
    },
    passwordConfirmText: '新密码两次输入不一致'
});