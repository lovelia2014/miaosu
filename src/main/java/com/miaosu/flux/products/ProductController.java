package com.miaosu.flux.products;

import com.miaosu.flux.base.QueryResult;
import com.miaosu.flux.base.ResultCode;
import com.miaosu.flux.base.ResultInfo;
import com.miaosu.flux.base.ServiceException;
import com.miaosu.flux.util.PaginationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import java.util.List;

/**
 * 商品Controller
 * Created by angus on 15/10/2.
 */
@RestController
@RequestMapping("/api/product")
public class ProductController {
    @Autowired
    private ProductService productService;

    /**
     * 获取列表
     */
    @RequestMapping(value = "/search", method = RequestMethod.GET)
    @Secured({ "ROLE_ADMIN", "ROLE_SYS_ADMIN" })
    public QueryResult<List<Product>> list(@RequestParam(value = "start", required = false) Integer start,
            @RequestParam(value = "limit", required = false) Integer size,
            @RequestParam(value = "text", required = false) String text) {
        Page<Product> products = productService.find(text, PaginationUtil.generatePageRequest(start, size));
        return new QueryResult<>(products.getTotalElements(), products.getContent());
    }

    /**
     * 删除
     */
    @RequestMapping(value = "/remove", method = RequestMethod.POST)
    @Secured({ "ROLE_ADMIN", "ROLE_SYS_ADMIN" })
    public ResultInfo remove(@RequestParam("ids") String... ids) {
        productService.remove(ids);
        return ResultInfo.SUCCESSFUL();
    }

    /**
     * 获取单个
     */
    @RequestMapping(value = "/get/{id}", method = RequestMethod.GET)
    @Secured({ "ROLE_USER", "ROLE_ADMIN", "ROLE_SYS_ADMIN" })
    public QueryResult<Product> get(@PathVariable String id) {
        Product product = productService.get(id);
        if (product != null) {
            return new QueryResult<>(1l, product);
        } else {
            throw new ServiceException(ResultCode.DATA_NOT_EXISTS);
        }
    }

    /**
     * 添加会
     */
    @RequestMapping(value = "/create", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @Secured({ "ROLE_ADMIN", "ROLE_SYS_ADMIN" })
    public ResultInfo create(@Valid @RequestBody Product product) {
        try {
            productService.create(product);
        } catch (ConstraintViolationException e) {
            throw new ServiceException(ResultCode.DATA_CONSTRAINT_VIOLATION);
        }
        return ResultInfo.SUCCESSFUL();
    }

    /**
     * 修改
     */
    @RequestMapping(value = "/update", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @Secured({ "ROLE_ADMIN", "ROLE_SYS_ADMIN" })
    public ResultInfo update(@Valid @RequestBody Product product) {
        productService.update(product);
        return ResultInfo.SUCCESSFUL();
    }


    /**
     * 禁用
     */
    @RequestMapping(value = "/disable", method = RequestMethod.POST)
    @Secured({ "ROLE_ADMIN", "ROLE_SYS_ADMIN" })
    public ResultInfo disable(@RequestParam("ids") String... ids) {
        productService.disable(ids);
        return ResultInfo.SUCCESSFUL();
    }


    /**
     * 启用
     */
    @RequestMapping(value = "/enable", method = RequestMethod.POST)
    @Secured({ "ROLE_ADMIN", "ROLE_SYS_ADMIN" })
    public ResultInfo enable(@RequestParam("ids") String... ids) {
        productService.enable(ids);
        return ResultInfo.SUCCESSFUL();
    }
}
