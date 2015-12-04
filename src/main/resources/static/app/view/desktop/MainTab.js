Desktop.MainTab = function (config) {

    var cfg = {
        id: 'doc-body',
        region: 'center',
        //margins: '0 5 5 0',
        resizeTabs: true,
        minTabWidth: 135,
        //tabWidth: 120,
        plugins: [
            //new Ext.ux.TabCloseMenu(),
            new Ext.ux.TabScrollerMenu({
                maxText: 15,
                pageSize: 20
            })
        ],
        enableTabScroll: true,
        activeTab: 0,
        items: {
            //cmargins: '10 5 5 0',
            id: 'welcome-panel',
            tabTip: '欢迎页面',
            title: '欢迎您',
            autoLoad: {
                url: 'welcome',
                scope: this
            },
            iconCls: 'icon-home',
            autoScroll: true
        },
        listeners: {
            'tabchange': function (tp, tab) {
                var menuPanel = Ext.getCmp('desktop.menu-panel');
                if(menuPanel){
                    menuPanel.selectNode(tab.menuIdPath);
                }
            }
        }
    };

    var allConfig = Ext.applyIf(config || {}, cfg);

    Desktop.MainTab.superclass.constructor.call(this, allConfig);
};

Ext.extend(Desktop.MainTab, Ext.TabPanel, {

    initEvents: function () {
        Desktop.MainTab.superclass.initEvents.call(this);

        /*
         // 测试代码
         for (var i = 1; i <= 20; i++) {
         var title = 'Tab # ' + i;
         this.add({
         title: title,
         html: 'Hi, i am tab ' + i,
         tabTip: title,
         closable: true
         });
         }*/
    },

    // node.attributes.href, node.text, node.attributes.fullPath, node.attributes.serviceKey, node.attributes.fullText
    loadTab: function (node) {
        var id = node.id;
        var tab = this.getComponent(id);
        if (tab) {
            this.setActiveTab(tab);
        } else {
            var nodeAttr = node.attributes;
            var tabPanel = nodeAttr.url;
            if(tabPanel){
                var p = this.add({
                    xtype: tabPanel,
                    id: id,
                    menuIdPath: nodeAttr.idPath,
                    title: node.text,
                    tabTip: nodeAttr.textPath,
                    iconCls: nodeAttr.iconCls
                });
                this.setActiveTab(p);
            }else{
                console.warn("***nodeAttr.url is undefined***")
            }
        }
    }

});

Ext.reg('desktop.main-tab', Desktop.MainTab);