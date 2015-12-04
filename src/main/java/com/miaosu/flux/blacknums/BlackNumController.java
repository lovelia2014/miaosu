package com.miaosu.flux.blacknums;

import java.util.List;

import javax.validation.ConstraintViolationException;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import com.miaosu.flux.base.QueryResult;
import com.miaosu.flux.base.ResultCode;
import com.miaosu.flux.base.ResultInfo;
import com.miaosu.flux.base.ServiceException;
import com.miaosu.flux.util.PaginationUtil;

/**
 * 黑名单号码Controller
 * Created by angus on 15/10/2.
 */
@RestController
@RequestMapping("/api/blacknum")
public class BlackNumController {
    @Autowired
    private BlackNumService blackNumService;

    /**
     * 获取列表
     */
    @RequestMapping(value = "/search", method = RequestMethod.GET)
    @Secured({ "ROLE_ADMIN", "ROLE_SYS_ADMIN" })
    public QueryResult<List<BlackNum>> list(@RequestParam(value = "start", required = false) Integer start,
            @RequestParam(value = "limit", required = false) Integer size,
            @RequestParam(value = "text", required = false) String text) {
        Page<BlackNum> blackNums = blackNumService.find(text, PaginationUtil.generatePageRequest(start, size));
        return new QueryResult<>(blackNums.getTotalElements(), blackNums.getContent());
    }

    /**
     * 删除
     */
    @RequestMapping(value = "/remove", method = RequestMethod.POST)
    @Secured({ "ROLE_ADMIN", "ROLE_SYS_ADMIN" })
    public ResultInfo remove(@RequestParam("ids") String... ids) {
        blackNumService.remove(ids);
        return ResultInfo.SUCCESSFUL();
    }

    /**
     * 获取单个
     */
    @RequestMapping(value = "/get/{id}", method = RequestMethod.GET)
    @Secured({ "ROLE_ADMIN", "ROLE_SYS_ADMIN" })
    public QueryResult<BlackNum> get(@PathVariable String id) {
        BlackNum product = blackNumService.get(id);
        if (product != null) {
            return new QueryResult<>(1l, product);
        } else {
            throw new ServiceException(ResultCode.DATA_NOT_EXISTS);
        }
    }

    /**
     * 添加
     */
    @RequestMapping(value = "/create", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @Secured({ "ROLE_ADMIN", "ROLE_SYS_ADMIN" })
    public ResultInfo create(@Valid @RequestBody BlackNum blackNum) {
        try {
            blackNumService.create(blackNum);
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
    public ResultInfo update(@Valid @RequestBody BlackNum blackNum) {
        blackNumService.update(blackNum);
        return ResultInfo.SUCCESSFUL();
    }

}
