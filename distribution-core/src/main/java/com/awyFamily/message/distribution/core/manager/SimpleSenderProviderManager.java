package com.awyFamily.message.distribution.core.manager;

import cn.hutool.core.collection.CollUtil;
import com.awyFamily.message.distribution.core.model.sender.SenderConfig;
import com.awyFamily.message.distribution.core.model.sender.SenderTypeEnum;
import com.awyFamily.message.distribution.core.sender.ISender;
import com.awyFamily.message.distribution.core.sender.ISenderProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 简单的发送提供者管理器
 * @author yhw
 * @date 2021-10-18
 */
public class SimpleSenderProviderManager implements ISenderManager {

    private static Map<String,ISenderProvider> providerMap = new ConcurrentHashMap<>();

    //发送者配置列表（真实环境,可以采取数据库）
    //Map<唯一标识,配置列表>
    //例如 单个设备(组织),关联了多个主题
    private final Map<String,List<SenderConfig>> configMap;

    public SimpleSenderProviderManager(Map<String, List<SenderConfig>> configMap,List<ISenderProvider> providers) {
        this.configMap = configMap;
        register(providers);
    }

    @Override
    public List<ISender> getSenders(String id) {
        return this.getSenders(id,null);
    }

    @Override
    public List<ISender> getSenders(String id, SenderTypeEnum type) {
        //单个设备,可以推送到多个主题
        List<SenderConfig> senderConfigs = configMap.get(id);
        if (CollUtil.isEmpty(senderConfigs))
            return new ArrayList<>();

        return senderConfigs.stream().filter(cfg -> {
            if (type == null) return true;
            return cfg.getType() == type;
        }).map(cfg -> {
            //遍历资源列表,获取具体的发送者
            //通过配置列表查询,资源
            return providerMap.get(cfg.getType().getName()).createSender(cfg);
        }).collect(Collectors.toList());
    }

    @Override
    public void remove(String id) {
        List<SenderConfig> senderConfigs = configMap.get(id);
        if (CollUtil.isEmpty(senderConfigs))
            return;
        senderConfigs.forEach(cfg -> {
            providerMap.get(SenderTypeEnum.getName(cfg.getType().getName())).removeSender(cfg.getTopic()).subscribe();
        });
    }

    /**
     * 注册
     * @param providers
     */
    public void register(List<ISenderProvider> providers){
        for (ISenderProvider provider : providers) {
            providerMap.put(provider.getSenderTypeName(),provider);
        }
    }
}
