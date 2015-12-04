package com.miaosu.flux.openapi;

import com.miaosu.flux.base.ResultCode;
import com.miaosu.flux.base.ResultInfo;
import com.miaosu.flux.base.ServiceException;
import com.miaosu.flux.bills.Balance;
import com.miaosu.flux.bills.BalanceService;
import com.miaosu.flux.members.Member;
import com.miaosu.flux.members.MemberService;
import com.miaosu.flux.system.users.User;
import com.miaosu.flux.system.users.UserService;
import com.miaosu.flux.util.DESUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 会员开放接口
 * Created by angus on 15/10/6.
 */
@RestController
@RequestMapping("/openapi/user")
public class OpenUserController extends OpenBaseController {

    @Autowired
    private MemberService memberService;

    @Autowired
    private UserService userService;

    @Autowired
    private BalanceService balanceService;

    @RequestMapping(value = "balance", method = {RequestMethod.GET, RequestMethod.POST})
    public ResultInfo<Map<String, Object>> create(@RequestParam(value = "userId") final String userId,
                                                  @RequestParam(value = "sign") final String sign) {
        Map<String, Object> data = new HashMap<>();

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("userId", userId);

        // Step.1 签名校验
        Member member = memberService.get(userId);
        User user = userService.get(userId);
        if (member == null || user == null || !user.isEnabled()) {
            // 用户不存在或被禁用
            throw new ServiceException(ResultCode.OPEN_USER_NOT_EXISTS);
        }
        checkSign(paramMap, sign, DESUtil.decryptToString(member.getToken(), userId));

        Balance balance = balanceService.get(userId);
        data.put("userId", userId);
        data.put("balance", balance.getBalance());
        return new ResultInfo<>(true, ResultCode.SUCCESSFUL, data);
    }
}