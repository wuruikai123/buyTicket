package com.buyticket.controller.admin;

import com.buyticket.entity.AboutConfig;
import com.buyticket.service.AboutConfigService;
import com.buyticket.utils.JsonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/about")
public class AdminAboutController {

    @Autowired
    private AboutConfigService aboutConfigService;

    /**
     * 获取关于展厅配置
     */
    @GetMapping("/config")
    public JsonData getConfig() {
        List<AboutConfig> list = aboutConfigService.list();
        if (list.isEmpty()) {
            return JsonData.buildError("配置不存在");
        }
        return JsonData.buildSuccess(list.get(0));
    }

    /**
     * 更新关于展厅配置
     */
    @PostMapping("/config")
    public JsonData updateConfig(@RequestBody AboutConfig config) {
        // 验证标题长度
        if (config.getTitle() != null && config.getTitle().length() > 10) {
            return JsonData.buildError("标题不能超过10个字");
        }
        
        List<AboutConfig> list = aboutConfigService.list();
        if (list.isEmpty()) {
            // 如果不存在，创建新记录
            aboutConfigService.save(config);
        } else {
            // 如果存在，更新第一条记录
            config.setId(list.get(0).getId());
            aboutConfigService.updateById(config);
        }
        return JsonData.buildSuccess("更新成功");
    }
}
