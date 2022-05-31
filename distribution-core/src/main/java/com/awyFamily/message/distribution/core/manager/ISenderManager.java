package com.awyFamily.message.distribution.core.manager;

import com.awyFamily.message.distribution.core.model.sender.SenderTypeEnum;
import com.awyFamily.message.distribution.core.sender.ISender;

import java.util.List;

/**
 * 简单的发送提供者管理器
 * @author yhw
 * @date 2021-10-16
 */
public interface ISenderManager {

    /**
     * 通过资源ID获取所有的消息发送者
     * @param id 资源ID(获取配置),
     *           可以是资源的唯一标识,例如设备的唯一编号
     * @return 消息发送者列表
     */
    List<ISender> getSenders(String id);

    /**
     * 通过资源ID和发送者类型获取消息发送者
     * @param id 资源ID(获取配置)
     *           可以是资源的唯一标识,例如设备的唯一编号
     * @param type 发送者类型类型
     * @return 消息发送者列表
     */
    List<ISender> getSenders(String id, SenderTypeEnum type);

    /**
     * 移除发送者
     * @param id 资源ID
     */
    void remove(String id);
}
