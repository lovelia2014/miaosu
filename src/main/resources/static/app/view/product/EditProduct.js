EditProductWindow = function (config) {
    var product = config.data || {};
    var isAdd = config.isAdd || false;

    var formPanel = new Ext.FormPanel({
        region: 'center',
        labelWidth: 95, // label settings here cascade unless overridden
        url: isAdd ? Desktop.contextPath + '/api/product/create' : Desktop.contextPath + '/api/product/update',
        border: false,
        bodyStyle: 'padding:10px 10px 0px 10px', //padding：上、右、下、左
        //width: 350,
        defaults: {anchor: '-20', msgTarget: 'side', allowBlank: false},
        defaultType: 'textfield',
        items: [
            {
                fieldLabel: '商品代码',
                readOnly: !isAdd,
                maxLength: 32,
                name: 'id',
                value: product.id
            }, {
                fieldLabel: '商品名称',
                minLength: 3,
                maxLength: 64,
                name: 'name',
                value: product.name
            }, {
                fieldLabel: '商品价格',
                name: 'price',
                xtype: 'numberfield',
                vtype: 'priceVtype',
                decimalPrecision: 4,
                allowDecimals: true,
                allowNegative: false,
                value: product.price
            }, {
                xtype: 'radiogroup',
                fieldLabel: '商品类型',
                anchor: '-130',
                items: [
                    {boxLabel: '前向产品', name: 'type', margins: '0 5 0 5', inputValue: 'QX', checked: isAdd || product.type == 'QX'},
                    {boxLabel: '后向产品', name: 'type', margins: '0 5 0 5', inputValue: 'HX', checked: product.type == 'HX'}
                ]
            }, {
                xtype: 'radiogroup',
                fieldLabel: '商品状态',
                anchor: '-180',
                items: [
                    {boxLabel: '启用', name: 'enabled', margins: '0 5 0 5', inputValue: true, checked: isAdd || product.enabled},
                    {boxLabel: '禁用', name: 'enabled', margins: '0 5 0 5', inputValue: false, checked: !isAdd && !product.enabled}
                ]
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
        title: isAdd ? '新增商品信息' : '修改商品信息',
        width: 405,
        height: 235,
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
    EditProductWindow.superclass.constructor.call(this, allConfig);
};

Ext.extend(EditProductWindow, Ext.Window, {
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

Ext.reg('win.editproduct', EditProductWindow);

Ext.apply(Ext.form.VTypes, {
    priceVtype: function (val, field) {
        var str = val.toString();

        return (str.length - str.indexOf(".")) < 5;
    },
    priceVtypeText: '商品价格小数点后最多3位'
});