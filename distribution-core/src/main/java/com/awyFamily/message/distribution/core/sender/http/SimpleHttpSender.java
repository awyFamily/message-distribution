package com.awyFamily.message.distribution.core.sender.http;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.awyFamily.message.distribution.core.model.sender.MessagePayload;
import com.awyFamily.message.distribution.core.model.sender.SenderTypeEnum;
import com.awyFamily.message.distribution.core.sender.ISender;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.HashMap;

/**
 * 默认 post 请求
 * @author yhw
 * @date 2021-10-19
 */
@Slf4j
public class SimpleHttpSender implements ISender<HashMap> {


    public static Scheduler scheduler = Schedulers.newSingle("simple-http-sender");

    private final String host;

    public SimpleHttpSender(String topic) {
        this.host = topic;
    }

    @Override
    public String getTopic() {
        return this.host;
    }

    @Override
    public Mono<Void> send(MessagePayload<HashMap> payload, String topic, JSONObject expands) {
        return Mono.fromCallable(() -> {

            String result = HttpRequest.post(topic).body(JSONUtil.toJsonStr(payload.getPayloadMessage()))
                    .execute()
                    .body();
            log.info("result : {} ",result);
            return result;
        }).subscribeOn(scheduler).then();
    }

    @Override
    public SenderTypeEnum getType() {
        return SenderTypeEnum.http;
    }

}
