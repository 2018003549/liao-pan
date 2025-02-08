package com.study.liao.controller;

import java.util.Arrays;
import java.util.Map;

import com.study.liao.annotation.GlobalInterceptor;
import com.study.liao.entity.vo.ResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.study.liao.entity.StatisticalRulesEntity;
import com.study.liao.service.StatisticalRulesService;



/**
 * 
 *
 * @author liao
 * @email sunlightcs@gmail.com
 * @date 2025-02-07 17:03:40
 */
@RestController
@RequestMapping("/colRules")
public class StatisticalRulesController extends ABaseController {
    @Autowired
    private StatisticalRulesService statisticalRulesService;

    /**
     * @param fileId 在线编辑的excel文件id
     * 根据配置的规则解析在线编辑的excel文件
     */
    @GetMapping("/processExcelColumnsWithRules/{fileId}")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO  processExcelColumnsWithRules(@RequestParam("fileId")String fileId){
        return getSuccessResponseVO(statisticalRulesService.processExcelColumnsWithRules(fileId));
    }

}
