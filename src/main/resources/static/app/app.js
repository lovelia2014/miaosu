Desktop = {
    contextPath: '/miaosu',
    pageSize: 20
};

Ext.onReady(function () {
    /*解决Extjs grid不允许选择复制的问题*/
    if (!Ext.grid.GridView.prototype.templates) {
        Ext.grid.GridView.prototype.templates = {};
    }
    Ext.grid.GridView.prototype.templates.cell = new Ext.Template(
        '<td class="x-grid3-col x-grid3-cell x-grid3-td-{id} x-selectable {css}" style="{style}" tabIndex="0" {cellAttr}>',
        '<div class="x-grid3-cell-inner x-grid3-col-{id}" {attr}>{value}</div>',
        '</td>'
    );


    if (Ext.isIE) {
        alert("oh my god! \nIE已经被out啦，请下载Chrome或Firefox后继续使用！");
        window.close();
        return;
    }

    Ext.Ajax.on('requestcomplete', function (ajax, xhr, o) {
        if (typeof urchinTracker == 'function' && o && o.url) {
            urchinTracker(o.url);
        }
    });

    Ext.QuickTips.init();

    //var splashscreen = Ext.getBody().mask('Loading application...', 'splashscreen');
    console.log('App is init...');


    //api.expandPath('/root/apidocs');


    var task = new Ext.util.DelayedTask(function () {
            Ext.get('loading').remove();
            Ext.get('loading-mask').fadeOut({
                duration: 1,
                remove: true
            });

            var desktop = new Desktop.Viewport();
            desktop.doLayout();

            console.log('App launched');
        }
    );
    task.delay(100);
});