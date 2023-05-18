package com.lx.system.api;


import com.lx.system.model.entity.Dictionary;
import com.lx.system.service.DictionaryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 * 数据字典 前端控制器
 * </p>
 *
 * @author itcast
 */
@Slf4j
@RefreshScope
@RestController
@Api(value = "数据字典查询接口",tags = "数据字典查询接口")
public class DictionaryController  {

    @Autowired
    private DictionaryService dictionaryService;

    @GetMapping("/dictionary/all")
    @ApiOperation("查询所有数据字典的内容")
    public List<Dictionary> queryAll() {
        return dictionaryService.queryAll();
    }

    @GetMapping("/dictionary/code/{code}")
    @ApiOperation("根据Code查询数据字典")
    public Dictionary getByCode(@PathVariable String code) {
        return dictionaryService.getByCode(code);
    }
}
