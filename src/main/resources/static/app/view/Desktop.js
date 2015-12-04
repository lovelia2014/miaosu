Desktop.Viewport = function (config) {
    var cfg = {
        layout: 'border',
        items: [{
            xtype: 'desktop.head-panel'
        }, {
            id: 'desktop.menu-panel',
            xtype: 'desktop.menu-panel'
        }, {
            id: 'desktop.main-tab',
            xtype: 'desktop.main-tab'
        }]
    };

    var allConfig = Ext.applyIf(config || {}, cfg);
    Desktop.Viewport.superclass.constructor.call(this, allConfig);
};

Ext.extend(Desktop.Viewport, Ext.Viewport, {});

Ext.reg('desktop-viewport', Desktop.Viewport);