package com.miaosu.flux.members;

import com.miaosu.flux.base.*;
import com.miaosu.flux.bills.AcctService;
import com.miaosu.flux.bills.Balance;
import com.miaosu.flux.bills.BalanceService;
import com.miaosu.flux.system.SerialNoUtil;
import com.miaosu.flux.system.users.User;
import com.miaosu.flux.system.users.UserService;
import com.miaosu.flux.util.DESUtil;
import com.miaosu.flux.util.PaginationUtil;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * User Controller
 * Created by angus on 15/6/23.
 */
@RestController
@RequestMapping("/api/member")
public class MemberController extends BaseController {

    @Autowired
    private MemberService memberService;

    @Autowired
    private UserService userService;

    @Autowired
    private SerialNoUtil serialNoUtil;

    @Autowired
    private BalanceService balanceService;

    @Autowired
    private AcctService acctService;

    /**
     * 账户充值
     */
    @RequestMapping(value = "/recharge", method = {RequestMethod.POST})
    @Secured({"ROLE_ADMIN", "ROLE_SYS_ADMIN"})
    public ResultInfo rechargeBalance(@RequestParam String userName, @RequestParam BigDecimal amount,
                                      @RequestParam(required = false) String remark, Principal principal) {
        Assert.isTrue(amount != null && amount.compareTo(BigDecimal.ZERO) >= 0, "充值金额必须大于0");

        String remarkInfo = "账户充值";
        if(StringUtils.hasText(remark)){
            remarkInfo = remark;
        }
        String info = String.format("%s(操作人:%s)", remarkInfo, principal.getName());

        acctService.recharge(userName, amount, info);
        return ResultInfo.SUCCESSFUL();
    }

    /**
     * 账户扣款
     */
    @RequestMapping(value = "/deduct", method = {RequestMethod.POST})
    @Secured({"ROLE_ADMIN", "ROLE_SYS_ADMIN"})
    public ResultInfo deductBalance(@RequestParam String userName, @RequestParam BigDecimal amount,
                                    @RequestParam(required = false) String remark, Principal principal) {
        Assert.isTrue(amount != null && amount.compareTo(BigDecimal.ZERO) >= 0, "扣款金额必须大于0");

        String remarkInfo = "账户扣款";
        if(StringUtils.hasText(remark)){
            remarkInfo = remark;
        }
        String info = String.format("%s(操作人:%s)", remarkInfo, principal.getName());

        acctService.deduct(userName, amount, info);
        return ResultInfo.SUCCESSFUL();
    }


    /**
     * 生成会员名
     *
     * @return 会员名
     */
    @RequestMapping(value = "/generateMemberName", method = RequestMethod.GET)
    @Secured({"ROLE_ADMIN", "ROLE_SYS_ADMIN"})
    public String generateMemberName() {
        String memberName = serialNoUtil.genrateMemberName();
        if (memberName == null) {
            throw new ServiceException(ResultCode.FAILED);
        }
        return memberName;
    }

    /**
     * 重置接口token
     */
    @RequestMapping(value = "/resetToken", method = {RequestMethod.GET, RequestMethod.POST})
    @Secured({"ROLE_USER", "ROLE_ADMIN", "ROLE_SYS_ADMIN"})
    public ResultInfo resetToken(@RequestParam String userName) {
        String token = memberService.resetToken(userName);
        return new ResultInfo(true, ResultCode.SUCCESSFUL, "新token:" + token);
    }

    /**
     * 获取会员列表
     */
    @RequestMapping(value = "/search", method = RequestMethod.GET)
    @Secured({"ROLE_USER", "ROLE_ADMIN", "ROLE_SYS_ADMIN"})
    public QueryResult<List<MemberForm>> list(@RequestParam(value = "start", required = false) Integer start,
                                              @RequestParam(value = "limit", required = false) Integer size,
                                              @RequestParam(value = "text", required = false) String text,
                                              Principal principal) {
        List<MemberForm> memberForms = new ArrayList<>();

        String currentUserName = principal.getName();
        Collection<? extends GrantedAuthority> authorities = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities();
        // 不允许非管理员用户查询其他用户信息
        if (!authorities.contains(new SimpleGrantedAuthority("ROLE_ADMIN"))
                && !authorities.contains(new SimpleGrantedAuthority("ROLE_SYS_ADMIN"))) {

            Member member = memberService.get(currentUserName);
            User user = userService.get(member.getUsername());
            MemberForm memberForm = new MemberForm();
            BeanUtils.copyProperties(member, memberForm);
            memberForm.setEnabled(user.isEnabled());
            String address = String.format("%s, %s, %s, %s", member.getProvince(), member.getCity(), member.getArea(), member.getDetailAddr());
            memberForm.setAddress(address);

            Balance balance = balanceService.get(member.getUsername());
            memberForm.setBalance(balance.getBalance());
            memberForms.add(memberForm);

            return new QueryResult<>(1l, memberForms);
        }

        Page<Member> members = memberService.find(text, PaginationUtil.generatePageRequest(start, size));

        for (Member member : members.getContent()) {
            User user = userService.get(member.getUsername());
            MemberForm memberForm = new MemberForm();
            BeanUtils.copyProperties(member, memberForm);
            memberForm.setEnabled(user.isEnabled());
            String address = String.format("%s, %s, %s, %s", member.getProvince(), member.getCity(), member.getArea(), member.getDetailAddr());
            memberForm.setAddress(address);

            Balance balance = balanceService.get(member.getUsername());
            memberForm.setBalance(balance.getBalance());

            memberForms.add(memberForm);
        }

        return new QueryResult<>(members.getTotalElements(), memberForms);
    }

    /**
     * 删除会员
     */
    @RequestMapping(value = "/remove", method = RequestMethod.POST)
    @Secured({"ROLE_ADMIN", "ROLE_SYS_ADMIN"})
    public ResultInfo remove(@RequestParam("userNames") String... userNames) {
        memberService.remove(userNames);
        return ResultInfo.SUCCESSFUL();
    }

    /**
     * 获取单个会员
     */
    @RequestMapping(value = "/get/{userName}", method = RequestMethod.GET)
    @Secured({"ROLE_USER", "ROLE_ADMIN", "ROLE_SYS_ADMIN"})
    public QueryResult<Member> get(@PathVariable String userName) {
        Member member = memberService.get(userName);
        if (member != null) {
            Member cloneMember = new Member();
            BeanUtils.copyProperties(member, cloneMember);
            cloneMember.setToken(DESUtil.decryptToString(member.getToken(), userName));
            return new QueryResult<>(1l, cloneMember);
        } else {
            throw new ServiceException(ResultCode.DATA_NOT_EXISTS);
        }
    }

    /**
     * 添加会员
     */
    @RequestMapping(value = "/create", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "创建会员", notes = "用户信息用json传递，regTime、lastLoginTime字段不用传递")
    @Secured({"ROLE_ADMIN", "ROLE_SYS_ADMIN"})
    public ResultInfo create(@Valid @RequestBody Member member) {
        member.setToken(DESUtil.encryptToString(memberService.generateToken(), member.getUsername()));
        try {
            memberService.create(member);
        } catch (ConstraintViolationException e) {
            throw new ServiceException(ResultCode.DATA_CONSTRAINT_VIOLATION);
        }
        return ResultInfo.SUCCESSFUL();
    }

    /**
     * 修改会员
     */
    @RequestMapping(value = "/update", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @Secured({"ROLE_USER", "ROLE_ADMIN", "ROLE_SYS_ADMIN"})
    public ResultInfo update(@Valid @RequestBody Member member, Principal principal) {

        String currentUserName = principal.getName();
        Collection<? extends GrantedAuthority> authorities = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities();

        Member userFind = memberService.get(member.getUsername());

        if (userFind != null) {
            // 不允许非管理员用户修改其他用户信息
            if (!authorities.contains(new SimpleGrantedAuthority("ROLE_ADMIN"))
                    && !authorities.contains(new SimpleGrantedAuthority("ROLE_SYS_ADMIN"))) {
                member.setToken(userFind.getToken()); // 非管理员用户不允许修改Token
                member.setDiscount(userFind.getDiscount()); // 非管理员用户不允许修改折扣
                member.setUsername(currentUserName); // 非管理员用户不允许修改用户名
                member.setLastLoginIp(userFind.getLastLoginIp()); // 非管理员用户不允许修改最后登录ip
                member.setLastLoginTime(userFind.getLastLoginTime()); // 非管理员用户不允许修改最后登录时间
                member.setRegTime(userFind.getRegTime()); // 非管理员用户不允许修改注册时间
            }else {
                member.setToken(DESUtil.encryptToString(member.getToken(), member.getUsername()));
            }
            memberService.update(member);
            return ResultInfo.SUCCESSFUL();
        } else {
            throw new ServiceException(ResultCode.DATA_NOT_EXISTS);
        }
    }

}
