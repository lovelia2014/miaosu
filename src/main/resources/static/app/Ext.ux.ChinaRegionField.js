Ext.ux.ChinaRegionField = function(config){
    var items =  [];
    var me = this;

    var showMode = 3;
    if(config && config.showMode){
        showMode = config.showMode;
    }
    var provinceStore, cityStore, areaStore;
    var provinceCmb, cityCmb, areaCmb;

    if(showMode >= 3){
        areaStore = new Ext.data.ArrayStore({
            // store configs
            //autoDestroy: true,
            //autoLoad: true,
            //idIndex: 0,
            //data: ChinaRegionData,
            fields: ['id', 'name', 'parentId']
        });

        areaCmb = new Ext.form.ComboBox({
            store: areaStore,
            typeAhead: true,
            name: config.areaName || "area",
            value: config.areaValue,
            forceSelection: true,
            triggerAction: 'all',
            selectOnFocus:true,
            mode: "local",
            displayField: "name",
            valueField: "id",
            emptyText: "请选择区",
            width: 80,
            allowBlank: false
        });
    }

    if(showMode >= 2){
        cityStore = new Ext.data.ArrayStore({
            fields: ['id', 'name', 'parentId']
        });

        cityCmb = new Ext.form.ComboBox({
            store: cityStore,
            typeAhead: true,
            name: config.cityName || "city",
            value: config.cityValue,
            forceSelection: true,
            triggerAction: 'all',
            selectOnFocus:true,
            mode: "local",
            displayField: "name",
            valueField: "id",
            emptyText: "请选择市",
            width: 80,
            allowBlank: false,
            listeners: {
                change: function (field, newValue, oldValue) {
                    if(showMode >=3 ){
                        //当下拉框选择改变的时候，也就是原值不等于新值时
                        if (newValue != "" && newValue != oldValue) {
                            //清空原来的下拉框
                            areaCmb.clearValue();
                            //过滤数据源
                            var areaData = [];
                            ChinaRegionData.forEach(function(item){
                                if(item[2] == newValue){
                                    areaData.push(item);
                                }
                            });
                            areaStore.loadData(areaData);

                            if(areaData.length == 0){
                                for(var i = me.items.length - 2; i > 0 ; i--){
                                    me.items.removeAt(i);
                                }
                            }
                        }
                    }
                }
            }
        });
    }

    if(showMode >= 1){
        provinceStore = new Ext.data.ArrayStore({
            fields: ['id', 'name', 'parentId']
        });

        var provData = [];
        ChinaRegionData.forEach(function(item){
            if(item[2] == "086"){
                provData.push(item);
            }
        });
        provinceStore.loadData(provData);

        provinceCmb = new Ext.form.ComboBox({
            triggerAction: 'all',
            typeAhead: true,
            mode: "local",
            name: config.provinceName || "province",
            value: config.provinceValue,
            forceSelection: true,
            selectOnFocus:true,
            displayField: "name",
            valueField: "id",
            store: provinceStore,
            emptyText: "请选择省",
            width: 80,
            allowBlank: false,
            listeners: {
                change: function (field, newValue, oldValue) {
                    if(showMode >=2 ) {
                        //当下拉框选择改变的时候，也就是原值不等于新值时
                        if (newValue != "" && newValue != oldValue) {

                            //清空原来的下拉框
                            cityCmb.clearValue();

                            var cityData = [];
                            ChinaRegionData.forEach(function(item){
                                if(item[2] == newValue){
                                    cityData.push(item);
                                }
                            });
                            cityStore.loadData(cityData);

                            if(cityData.length == 0){
                                for(var i = me.items.length - 1; i > 0; i--){
                                    me.items.removeAt(i);
                                }
                            }

                            if(showMode >=3 ){
                                //清空原来的下拉框
                                areaCmb.clearValue();
                                areaStore.loadData([]);
                            }
                        }
                    }
                }
            }
        });

    }

    var combos = [provinceCmb, cityCmb, areaCmb];

    for (var i = 0; i < showMode; i++) {
        items.push(combos[i]);
    }

    var cfg = {
        items: items
    };


    var allConfig = Ext.applyIf(config || {}, cfg);
    Ext.ux.ChinaRegionField.superclass.constructor.call(this, allConfig);
};


Ext.extend(Ext.ux.ChinaRegionField, Ext.form.CompositeField, {});

Ext.reg('ChinaRegionField', Ext.ux.ChinaRegionField);

