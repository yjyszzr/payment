package com.dl.shop.payment.web;
import com.dl.base.result.BaseResult;
import com.dl.base.result.ResultGenerator;
import com.dl.shop.payment.model.WithdrawLog;
import com.dl.shop.payment.service.WithdrawLogService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
* Created by CodeGenerator on 2018/03/28.
*/
@RestController
@RequestMapping("/withdraw/log")
public class WithdrawLogController {
    @Resource
    private WithdrawLogService withdrawLogService;

    @PostMapping("/add")
    public BaseResult add(WithdrawLog withdrawLog) {
        withdrawLogService.save(withdrawLog);
        return ResultGenerator.genSuccessResult();
    }

    @PostMapping("/delete")
    public BaseResult delete(@RequestParam Integer id) {
        withdrawLogService.deleteById(id);
        return ResultGenerator.genSuccessResult();
    }

    @PostMapping("/update")
    public BaseResult update(WithdrawLog withdrawLog) {
        withdrawLogService.update(withdrawLog);
        return ResultGenerator.genSuccessResult();
    }

    @PostMapping("/detail")
    public BaseResult detail(@RequestParam Integer id) {
        WithdrawLog withdrawLog = withdrawLogService.findById(id);
        return ResultGenerator.genSuccessResult(null,withdrawLog);
    }

    @PostMapping("/list")
    public BaseResult list(@RequestParam(defaultValue = "0") Integer page, @RequestParam(defaultValue = "0") Integer size) {
        PageHelper.startPage(page, size);
        List<WithdrawLog> list = withdrawLogService.findAll();
        PageInfo pageInfo = new PageInfo(list);
        return ResultGenerator.genSuccessResult(null,pageInfo);
    }
}
