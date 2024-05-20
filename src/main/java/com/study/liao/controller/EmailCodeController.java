package com.study.liao.controller;

import java.util.Arrays;
import java.util.Map;

import com.liao.common.utils.PageUtils;
import com.liao.common.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.study.liao.entity.EmailCodeEntity;
import com.study.liao.service.EmailCodeService;



/**
 * 
 *
 * @author liao
 * @email sunlightcs@gmail.com
 * @date 2024-05-04 19:27:15
 */
@RestController
@RequestMapping("liao/emailcode")
public class EmailCodeController {
    @Autowired
    private EmailCodeService emailCodeService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = emailCodeService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{email}")
    public R info(@PathVariable("email") String email){
		EmailCodeEntity emailCode = emailCodeService.getById(email);

        return R.ok().put("emailCode", emailCode);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody EmailCodeEntity emailCode){
		emailCodeService.save(emailCode);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody EmailCodeEntity emailCode){
		emailCodeService.updateById(emailCode);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody String[] emails){
		emailCodeService.removeByIds(Arrays.asList(emails));

        return R.ok();
    }

}
