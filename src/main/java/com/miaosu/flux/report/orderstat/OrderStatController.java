package com.miaosu.flux.report.orderstat;

import com.miaosu.flux.base.QueryResult;
import com.miaosu.flux.base.ResultCode;
import com.miaosu.flux.base.ServiceException;
import com.miaosu.flux.util.PaginationUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.text.NumberFormat;
import java.util.*;

/**
 * 账单统计Controller
 * Created by angus on 15/10/20.
 */
@RestController
@RequestMapping("/api/orderstat")
public class OrderStatController {

    @Autowired
    private OrderStatService orderStatService;

    /**
     * 获取列表
     */
    @RequestMapping(value = "/search", method = RequestMethod.GET)
    @Secured({"ROLE_USER", "ROLE_ADMIN", "ROLE_SYS_ADMIN"})
    public QueryResult<List<OrderStatForm>> list(@RequestParam(value = "start", required = false) Integer start,
                                            @RequestParam(value = "limit", required = false) Integer size,
                                            @RequestParam(value = "username", required = false) String username,
                                            @RequestParam(value = "productId", required = false) String productId,
                                            @RequestParam(value = "province", required = false) String province,
                                            @DateTimeFormat(pattern = "yyyy-MM-dd") @RequestParam(value = "begin", required = false) Date begin,
                                            @DateTimeFormat(pattern = "yyyy-MM-dd") @RequestParam(value = "end", required = false) Date end,
                                            @RequestParam(value = "groupConditions", required = false) int[] inputGroupConditions,
                                            Principal principal) {
        if (begin == null) {
            begin = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000);
        }
        if (end == null) {
            end = new Date();
        }

        if (end.getTime() - begin.getTime() > 60 * 24 * 60 * 60 * 1000l) {
            throw new ServiceException(ResultCode.DATA_CONSTRAINT_VIOLATION);
        }

        Boolean[] groupConditions = new Boolean[]{false, false, false, false};

        if (inputGroupConditions == null) {
            groupConditions = new Boolean[]{true, true, true, true};
        }else{
            for(int i : inputGroupConditions){
                if(i < 4) {
                    groupConditions[i] = true;
                }
            }
        }

        String currentUserName = principal.getName();
        Collection<? extends GrantedAuthority> authorities = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities();
        // 不允许非管理员用户查询其他用户信息
        if (!authorities.contains(new SimpleGrantedAuthority("ROLE_ADMIN"))
                && !authorities.contains(new SimpleGrantedAuthority("ROLE_SYS_ADMIN"))
                && !currentUserName.equals(username)) {
            username = principal.getName();
        }
        List<OrderStatForm> orderStatForms = new ArrayList<>();

        Page<OrderStat> orderStats = null;
        if(isAllTrue(groupConditions)){
            orderStats = orderStatService.findByCondition(begin, end, username, productId, province,
                PaginationUtil.generatePageRequest(start, size));
        }else {
            orderStats = orderStatService.findByGroup(begin, end, username, productId, province, groupConditions, PaginationUtil.generatePageRequest(start, size));
        }

        NumberFormat numberFormat = NumberFormat.getPercentInstance();
        numberFormat.setMaximumFractionDigits(2);
        numberFormat.setMinimumFractionDigits(2);
        for(OrderStat orderStat : orderStats.getContent()){
            OrderStatForm orderStatForm = new OrderStatForm();
            BeanUtils.copyProperties(orderStat, orderStatForm);
            String rechargeOkRate = "N/A";
            if(orderStatForm.getTotalCount() != 0l) {
                rechargeOkRate = numberFormat.format(Double.longBitsToDouble(orderStatForm.getRechargeOkSum()) / Double.longBitsToDouble(orderStatForm.getTotalCount()));
            }
            orderStatForm.setRechargeOkRate(rechargeOkRate);
            orderStatForms.add(orderStatForm);
        }

        return new QueryResult<>(orderStats.getTotalElements(), orderStatForms);
    }

    private boolean isAllTrue(Boolean[] booleans){
        if (booleans == null){
            return true;
        }
        for (Boolean bool : booleans) {
            if(!bool){
                return false;
            }
        }
        return true;
    }

    /**
     * 数据汇总
     */
    @RequestMapping(value = "/sum", method = {RequestMethod.GET, RequestMethod.POST})
    @Secured({"ROLE_USER", "ROLE_ADMIN", "ROLE_SYS_ADMIN"})
    public QueryResult<Map<String, Object>> sum(@RequestParam(value = "username", required = false) String username,
                                                    @RequestParam(value = "productId", required = false) String productId,
                                                    @RequestParam(value = "province", required = false) String province,
                                                    @DateTimeFormat(pattern = "yyyy-MM-dd") @RequestParam(value = "begin", required = false) Date begin,
                                                    @DateTimeFormat(pattern = "yyyy-MM-dd") @RequestParam(value = "end", required = false) Date end,
                                                    Principal principal) {
        if (begin == null) {
            begin = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000);
        }
        if (end == null) {
            end = new Date();
        }

        if (end.getTime() - begin.getTime() > 60 * 24 * 60 * 60 * 1000l) {
            throw new ServiceException(ResultCode.DATA_CONSTRAINT_VIOLATION);
        }

        String currentUserName = principal.getName();
        Collection<? extends GrantedAuthority> authorities = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities();
        // 不允许非管理员用户查询其他用户信息
        if (!authorities.contains(new SimpleGrantedAuthority("ROLE_ADMIN"))
                && !authorities.contains(new SimpleGrantedAuthority("ROLE_SYS_ADMIN"))
                && !currentUserName.equals(username)) {
            username = principal.getName();
        }

        Map<String, Object> result = orderStatService.sumByStatDateBetween(begin, end, username, productId, province);

        return new QueryResult<>(1l, result);
    }

}
