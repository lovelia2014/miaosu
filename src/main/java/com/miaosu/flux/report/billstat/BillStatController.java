package com.miaosu.flux.report.billstat;

import com.miaosu.flux.base.QueryResult;
import com.miaosu.flux.base.ResultCode;
import com.miaosu.flux.base.ServiceException;
import com.miaosu.flux.util.PaginationUtil;
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

import java.math.BigDecimal;
import java.security.Principal;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 账单统计Controller
 * Created by angus on 15/10/20.
 */
@RestController
@RequestMapping("/api/billstat")
public class BillStatController {

    @Autowired
    private BillStatService billStatService;

    /**
     * 获取列表
     */
    @RequestMapping(value = "/search", method = RequestMethod.GET)
    @Secured({"ROLE_USER", "ROLE_ADMIN", "ROLE_SYS_ADMIN"})
    public QueryResult<List<BillStat>> list(@RequestParam(value = "start", required = false) Integer start,
                                            @RequestParam(value = "limit", required = false) Integer size,
                                            @RequestParam(value = "username", required = false) String username,
                                            @DateTimeFormat(pattern = "yyyy-MM-dd") @RequestParam(value = "begin", required = false) Date begin,
                                            @DateTimeFormat(pattern = "yyyy-MM-dd") @RequestParam(value = "end", required = false) Date end,
                                            Principal principal) {
        if (begin == null) {
            begin = new Date(System.currentTimeMillis() - 2 * 24 * 60 * 60 * 1000);
        }
        if (end == null) {
            end = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000);
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

        Page<BillStat> billStats = billStatService.findByCondition(begin, end, username, PaginationUtil.generatePageRequest(start, size));

        return new QueryResult<>(billStats.getTotalElements(), billStats.getContent());
    }

    /**
     * 数据汇总
     */
    @RequestMapping(value = "/sum", method = {RequestMethod.GET, RequestMethod.POST})
    @Secured({"ROLE_USER", "ROLE_ADMIN", "ROLE_SYS_ADMIN"})
    public QueryResult<Map<String, BigDecimal>> sum(@RequestParam(value = "start", required = false) Integer start,
                                                    @RequestParam(value = "limit", required = false) Integer size,
                                                    @RequestParam(value = "username", required = false) String username,
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

        Map<String, BigDecimal> result = billStatService.sumByStatDateBetween(begin, end, username);

        return new QueryResult<>(1l, result);
    }

}
