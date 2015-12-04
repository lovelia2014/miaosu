package com.miaosu.flux.supplier.huazong;

import com.miaosu.flux.supplier.huazong.domain.GetOrderStatusResult;
import com.miaosu.flux.supplier.huazong.domain.GetProductListResult;
import com.miaosu.flux.supplier.huazong.domain.OrderFlowResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 华众联调工具
 * Created by angus on 15/10/8.
 */
@RestController
@RequestMapping("/hztest")
public class HuazongTestController {

    @Autowired
    private HuaZongPlatform huaZongPlatform;

    @RequestMapping("/validate")
    public boolean validate(@RequestParam("phone") String phone, @RequestParam("productId") String productId) {
        return huaZongPlatform.validate(phone, productId);
    }

    @RequestMapping("/queryOrderStatus")
    public GetOrderStatusResult queryOrderStatus(@RequestParam(value = "orderId", required = false) String orderId, @RequestParam(value = "rechargeId", required = false) String rechargeId) {
        return huaZongPlatform.queryOrderStatus(orderId, rechargeId);
    }

    @RequestMapping("/order")
    @Secured({"ROLE_ADMIN", "ROLE_SYS_ADMIN"})
    public OrderFlowResult order(@RequestParam("phone") String phone, @RequestParam("productId") String productId, @RequestParam("effectType") int effectType, @RequestParam("orderId") String orderId, @RequestParam("province") String province) {
        return huaZongPlatform.order(phone, effectType, productId, orderId, province);
    }

    @RequestMapping("/queryBalance")
    public String queryOrderStatus() {
        return huaZongPlatform.queryBalance();
    }

    @RequestMapping("/queryProductList")
    public GetProductListResult queryProductList() {
        return huaZongPlatform.queryProductList();
    }
}
