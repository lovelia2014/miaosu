Desktop.MenuPanel = function (config) {

    // 菜单树
    var treePanel = new Ext.tree.TreePanel({
        region: 'center',
        margins: '0 0 5 5',
        cmargins: '0 0 0 0',
        rootVisible: false,
        lines: false,
        autoScroll: true,
        animCollapse: false,
        animate: true,
        enableDD: true,
        containerScroll: true,
        root: new Ext.tree.AsyncTreeNode({
            text: 'API Test',
            id: 'root',
            singleClickExpand: true,
            expanded: true
        }),
        loader: new Ext.tree.TreeLoader({
            preloadChildren: true,
            clearOnLoad: false,
            method: 'get',
            dataUrl: Desktop.contextPath + '/api/system/menu/tree'
        }),
        collapseFirst: false,
        listeners: {
            click: function (node, e) {
                if (node.isLeaf()) {
                    e.stopEvent();
                    var mainTab = Ext.getCmp('desktop.main-tab');
                    if (mainTab) {
                        mainTab.loadTab(node);
                    }
                }
            }
        }
    });

    treePanel.getSelectionModel().on('beforeselect', function (sm, node) {
        return node.isLeaf();
    });

    var me = this;

    // 工具条
    var toolbar = new Ext.Toolbar({
        region: 'north',
        height: 30,
        cls: 'top-toolbar',
        items: [' ', new Ext.form.TextField({
            width: 150,
            emptyText: '快速查找 ...',
            listeners: {
                scope: this,
                render: function (f) {
                    f.el.on('keydown', me.filterTree, me, {
                        buffer: 350
                    });
                }
            }
        }), ' ', ' ', {
            iconCls: 'icon-expand-all',
            tooltip: '展开所有',
            handler: function () {
                treePanel.root.expand(true);
            }
        }, '-', {
            iconCls: 'icon-collapse-all',
            tooltip: '折叠所有',
            handler: function () {
                treePanel.root.collapse(true);
            }
        }]
    });

    this.menuFilter = new Ext.tree.TreeFilter(treePanel, {
        clearBlank: true,
        autoClear: true
    });

    this.hiddenPkgs = [];


    var cfg = {
        region: 'west',
        split: true,
        width: 220,
        minSize: 220,
        maxSize: 500,
        collapseMode: 'mini',
        layout: 'border',
        border: false,
        items: [toolbar, treePanel]
    };

    this.treePanel = treePanel;

    var allConfig = Ext.applyIf(config || {}, cfg);
    Desktop.MenuPanel.superclass.constructor.call(this, allConfig);


};

Ext.extend(Desktop.MenuPanel, Ext.Panel, {
    selectNode: function (fullPath) {
        if (fullPath) {
            // xxx.xxx.xxx
            this.treePanel.selectPath('/root/' + fullPath);
        }
    },

    filterTree: function (field) {
        if (this.treePanel) {
            var text = field.target.value;
            //var alias = e.target.alias;
            Ext.each(this.hiddenPkgs, function (n) {
                n.ui.show();
            });

            if (!text) {
                this.menuFilter.clear();
                return;
            }
            this.treePanel.expandAll();

            var re = new RegExp('^' + Ext.escapeRe(text), 'i');
            this.menuFilter.filterBy(function (n) {
                return !n.attributes.leaf || re.test(n.text) || (n.attributes.alias && re.test(n.attributes.alias));
            });

            // hide empty packages that weren't filtered
            this.hiddenPkgs = [];
            var me = this;
            this.treePanel.root.cascade(function (n) {
                if (!n.attributes.leaf && n.ui.ctNode && n.ui.ctNode.offsetHeight < 3) {
                    n.ui.hide();
                    me.hiddenPkgs.push(n);
                }
            });
        }
    }
});

Ext.reg('desktop.menu-panel', Desktop.MenuPanel);


