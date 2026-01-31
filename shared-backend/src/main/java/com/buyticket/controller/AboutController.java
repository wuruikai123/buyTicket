package com.buyticket.controller;

import com.buyticket.entity.AboutConfig;
import com.buyticket.service.AboutConfigService;
import com.buyticket.utils.JsonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/about")
public class AboutController {

    @Autowired
    private AboutConfigService aboutConfigService;

    /**
     * 获取关于展厅配置（A端）
     */
    @GetMapping("/config")
    public JsonData getConfig() {
        List<AboutConfig> list = aboutConfigService.list();
        if (list.isEmpty()) {
            return JsonData.buildError("配置不存在");
        }
        return JsonData.buildSuccess(list.get(0));
    }
}
