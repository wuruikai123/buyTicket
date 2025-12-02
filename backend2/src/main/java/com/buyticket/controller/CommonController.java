package com.buyticket.controller;

import com.buyticket.utils.JsonData;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/common")
public class CommonController {

    @GetMapping("/agreement")
    public JsonData agreement() {
        Map<String, Object> data = new HashMap<>();
        data.put("content", Arrays.asList(
            "1. 购票须知：所有门票一经售出，非不可抗力因素不退不换。",
            "2. 入场要求：请携带本人身份证原件，凭电子票二维码扫码入场。",
            "3. 禁止携带：严禁携带易燃易爆物品、管制刀具等危险品入场。",
            "4. 知识产权：展馆内部分区域禁止拍照，请留意现场标识。",
            "5. 儿童说明：1.2米以下儿童免票，需由成人陪同。"
        ));
        return JsonData.buildSuccess(data);
    }
}
