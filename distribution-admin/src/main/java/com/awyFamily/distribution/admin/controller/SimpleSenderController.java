package com.awyFamily.distribution.admin.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.awyFamily.message.distribution.core.manager.ISenderManager;
import com.awyFamily.message.distribution.core.model.sender.MessagePayload;
import com.awyFamily.message.distribution.core.sender.ISender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author yhw
 * @date 2021-11-02
 */
@RestController
public class SimpleSenderController {

    @Autowired
    private ISenderManager senderManager;


    @GetMapping("/request/{id}")
    public String request(@PathVariable("id")String id) {
        List<ISender> senders = senderManager.getSenders(id);
        if (CollUtil.isNotEmpty(senders)) {
            HashMap<String,String> map = new HashMap<>();
            map.put("name","tom");
            MessagePayload payload = new MessagePayload(map);
            for (ISender sender : senders) {
                sender.send(payload).subscribe();
            }
        }
        return "request test";
    }

    @PostMapping("/callback")
    public String callback(@RequestBody Map<String,Object> map) {
        System.out.println(JSONUtil.toJsonStr(map));
        return "callback test";
    }

}
