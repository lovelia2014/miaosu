package com.miaosu.flux;

import com.miaosu.flux.members.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

/**
 * Welcome controller
 * Created by angus on 15/6/13.
 */
@Controller
public class WelcomeController {

    @Autowired
    private MemberService memberService;

    @RequestMapping({"/", "/index"})
    @Secured("ROLE_USER")
    public String home(Map<String, Object> model, HttpServletRequest request, HttpSession session) {
        SecurityContextImpl securityContextImpl = (SecurityContextImpl) request.getSession().getAttribute("SPRING_SECURITY_CONTEXT");
        String userName = securityContextImpl.getAuthentication().getName();
        boolean isAdmin = false;
        Collection<? extends GrantedAuthority> authorities = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities();
        // 是否管理员用户
        if (authorities.contains(new SimpleGrantedAuthority("ROLE_ADMIN"))
                || authorities.contains(new SimpleGrantedAuthority("ROLE_SYS_ADMIN"))) {
            isAdmin = true;
        } else {
            // 非管理员用户记录登录信息
            Object recordLoginFlag = session.getAttribute("RecordLoginFlag");
            if(recordLoginFlag == null) {
                String realIp = request.getHeader("X-Real-IP");
                realIp = realIp == null ? request.getRemoteHost() : realIp;
                memberService.updateLastLoginInfo(userName, new Date(), realIp);
                session.setAttribute("RecordLoginFlag", true);
            }
        }

        model.put("userName", userName);
        model.put("isAdmin", isAdmin);
        model.put("date", new Date());
        return "index";
    }
}
