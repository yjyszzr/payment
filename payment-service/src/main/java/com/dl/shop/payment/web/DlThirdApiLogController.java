package com.dl.shop.payment.web;
import com.dl.base.result.BaseResult;
import com.dl.base.result.ResultGenerator;
import com.dl.shop.payment.model.DlThirdApiLog;
import com.dl.shop.payment.service.DlThirdApiLogService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
* Created by CodeGenerator on 2018/05/07.
*/
@RestController
@RequestMapping("/dl/third/api/log")
public class DlThirdApiLogController {
    @Resource
    private DlThirdApiLogService dlThirdApiLogService;

    @PostMapping("/add")
    public BaseResult add(DlThirdApiLog dlThirdApiLog) {
        dlThirdApiLogService.save(dlThirdApiLog);
        return ResultGenerator.genSuccessResult();
    }

    @PostMapping("/delete")
    public BaseResult delete(@RequestParam Integer id) {
        dlThirdApiLogService.deleteById(id);
        return ResultGenerator.genSuccessResult();
    }

    @PostMapping("/update")
    public BaseResult update(DlThirdApiLog dlThirdApiLog) {
        dlThirdApiLogService.update(dlThirdApiLog);
        return ResultGenerator.genSuccessResult();
    }

    @PostMapping("/detail")
    public BaseResult detail(@RequestParam Integer id) {
        DlThirdApiLog dlThirdApiLog = dlThirdApiLogService.findById(id);
        return ResultGenerator.genSuccessResult(null,dlThirdApiLog);
    }

    @PostMapping("/list")
    public BaseResult list(@RequestParam(defaultValue = "0") Integer page, @RequestParam(defaultValue = "0") Integer size) {
        PageHelper.startPage(page, size);
        List<DlThirdApiLog> list = dlThirdApiLogService.findAll();
        PageInfo pageInfo = new PageInfo(list);
        return ResultGenerator.genSuccessResult(null,pageInfo);
    }
}
