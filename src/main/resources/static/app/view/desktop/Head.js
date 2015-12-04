Desktop.HeadPanel = function (config) {
    var cfg = {
        border: false,
        layout: 'anchor',
        region: 'north',
        cls: 'docs-header',
        height: 37,
        items: [
            {
                xtype: 'box',
                el: 'header',
                border: false,
                anchor: 'none 0'
            }
        ]
    };

    var allConfig = Ext.applyIf(config || {}, cfg);

    Desktop.HeadPanel.superclass.constructor.call(this, allConfig);
};

Ext.extend(Desktop.HeadPanel, Ext.Panel, {

});

Ext.reg('desktop.head-panel', Desktop.HeadPanel);