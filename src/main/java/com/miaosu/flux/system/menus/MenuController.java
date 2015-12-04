package com.miaosu.flux.system.menus;

import com.miaosu.flux.base.ResultInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Menu控制层
 * Created by angus on 15/6/15.
 */
@Controller
@RequestMapping("/api/system/menu")
public class MenuController {

    private static Logger logger = LoggerFactory.getLogger(MenuController.class);

    @Autowired
    private MenuService menuService;

    @RequestMapping(value = "/list", method = { RequestMethod.GET, RequestMethod.POST })
    @Secured("ROLE_USER")
    public @ResponseBody Iterable<Menu> findAllMenus() {
        return menuService.findAll();
    }

    @RequestMapping(value = "/tree", method = { RequestMethod.GET, RequestMethod.POST })
    @Secured("ROLE_USER")
    public @ResponseBody Iterable<Menu> findMenusTree(HttpServletRequest request) {

        Iterable<Menu> menus = menuService.findAll();
        List<Menu> menuList = new ArrayList<Menu>();

        for (Menu menu : menus) {
            if (menu.getParentId() == null) {

                // 判断用户是否拥有菜单权限
                if (hasAuthority(request, menu)) {
                    ExtMenu root = new ExtMenu(menu);
                    root.setIdPath(menu.getId());
                    root.setTextPath(menu.getText());
                    root.setSingleExpand(!menu.isLeaf());

                    // 添加子节点
                    addChild(request, root, menus);

                    menuList.add(root);
                }
            }
        }

        return menuList;
    }

    private boolean hasAuthority(HttpServletRequest request, Menu menu) {
        try {
            SecurityContextImpl securityContextImpl = (SecurityContextImpl) request.getSession().getAttribute(
                    "SPRING_SECURITY_CONTEXT");
            Collection<? extends GrantedAuthority> authorities = securityContextImpl.getAuthentication()
                    .getAuthorities();

            List<String> menuAuthorities = menu.getAuthorities();

            // 如果菜单没有指定权限，则所有角色可以访问
            if (menuAuthorities == null || menuAuthorities.isEmpty()) {
                return true;
            }

            // 用户的权限列表只要包含菜单中的任何一个权限，都表示用户拥有此菜单的权限
            for (GrantedAuthority authority : authorities) {
                if (menuAuthorities.contains(authority.getAuthority())) {
                    return true;
                }
            }
        } catch (Exception e) {
            logger.error("判断用户菜单权限异常；menu：{}", menu, e);
        }

        return false;
    }

    private Menu addChild(HttpServletRequest request, ExtMenu parent, Iterable<Menu> menus) {
        if (parent != null && menus != null) {
            for (Menu menu : menus) {
                // 判断用户是否拥有菜单权限
                if (hasAuthority(request, menu)) {
                    // 添加子节点
                    if (Objects.equals(menu.getParentId(), parent.getId())) {
                        ExtMenu extMenu = new ExtMenu(menu);
                        extMenu.setIdPath(parent.getIdPath() + "/" + menu.getId());
                        extMenu.setTextPath(parent.getTextPath() + "/" + menu.getText());
                        extMenu.setSingleExpand(!menu.isLeaf());

                        parent.addChildren(extMenu);

                        // 如果当前是个目录，递归添加子目录
                        if (!menu.isLeaf()) {
                            addChild(request, extMenu, menus);
                        }
                    }
                }
            }
        }
        return parent;
    }

    @RequestMapping(value = "/remove", method = { RequestMethod.POST, RequestMethod.DELETE })
    @Secured("ROLE_ADMIN")
    public @ResponseBody
    ResultInfo removeMenus(@RequestParam Long... ids) {
        menuService.remove(ids);
        return ResultInfo.SUCCESSFUL();
    }

    @RequestMapping(value = "/save", method = { RequestMethod.POST })
    @Secured("ROLE_ADMIN")
    public @ResponseBody ResultInfo saveMenus(@RequestParam Menu menu) {
        menuService.save(menu);
        return ResultInfo.SUCCESSFUL();
    }
}
