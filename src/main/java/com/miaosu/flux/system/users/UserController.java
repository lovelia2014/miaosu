package com.miaosu.flux.system.users;

import com.miaosu.flux.base.BaseController;
import com.miaosu.flux.base.ResultCode;
import com.miaosu.flux.base.ResultInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;

/**
 * User Controller
 * Created by angus on 15/6/23.
 */
@Controller
@RequestMapping("/api/system/user")
public class UserController extends BaseController{

    @Autowired
    private UserService userService;

    @RequestMapping(value = "/changePwd", method = { RequestMethod.POST, RequestMethod.GET })
    @Secured({"ROLE_USER", "ROLE_ADMIN","ROLE_SYS_ADMIN"})
    public @ResponseBody ResultInfo changePwd(@RequestParam String oldPwd, @RequestParam String newPwd, Principal principal) {

        try {
            if (userService.changePassword(principal.getName(), oldPwd, newPwd)) {
                SecurityContextHolder.clearContext();
                return ResultInfo.SUCCESSFUL();
            }else{
                return new ResultInfo(false, ResultCode.CHANGE_PWD_WITH_WORNG_OLD_PWD, "旧密码验证失败！");
            }
        } catch (AccessDeniedException e) {
            return new ResultInfo(false, ResultCode.CHANGE_PWD_WITH_WORNG_OLD_PWD, "旧密码验证失败！");
        } catch (AuthenticationException e) {
            return new ResultInfo(false, ResultCode.CHANGE_PWD_WITH_WORNG_OLD_PWD, "旧密码验证失败！");
        }
    }

    @RequestMapping(value = "/enabled", method = { RequestMethod.POST, RequestMethod.GET })
    @Secured({"ROLE_ADMIN","ROLE_SYS_ADMIN"})
    public @ResponseBody ResultInfo enabled(@RequestParam String userName, @RequestParam boolean enabled) {
        if(enabled){
            userService.enable(userName);
        }else{
            userService.disable(userName);
        }

        return ResultInfo.SUCCESSFUL();
    }
}
