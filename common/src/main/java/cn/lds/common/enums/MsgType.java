package cn.lds.common.enums;

/**
 * 消息类型枚举
 * Created by leadingsoft on 18/1/10.
 */

public enum MsgType {
    /**
     * 车辆异常移动
     */
    ABNORMAL_MOVEMENT,
    /**
     * 远程故障
     */
    REMOTE_FAULT,
    /**
     * 维保提醒
     */
    CARE_NOTIFACTION,
    /**
     * 系统消息（群发)
     */
    SYSTEM_NOTIFYCATION,
    /**
     * 文本申请
     */
    TEXT_APPLICATION,
    /**
     * 图片申请
     */
    IMAGE_APPLICATION
}
