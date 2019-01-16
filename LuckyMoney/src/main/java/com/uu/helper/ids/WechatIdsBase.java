package com.uu.helper.ids;

/**
 * 微信的资源id基类
 * author：zhangguiyou
 * date: 2019/1/16.
 */
public abstract class WechatIdsBase {

    /**
     * 聊天列表中item的未读消息内容的id
     */
    public abstract String getChatListItemMsgContentId();

    /**
     * 聊天列表中item的未读消息标识的id
     */
    public abstract String getChatListItemMsgTextUnreadId();

    /**
     * 聊天列表中item的未读消息标识的id
     */
    public abstract String getChatListItemMsgImgUnreadId();

    public abstract String getChatDetailLuckyTextId();

    /**
     * 聊天详情中红包"開"按钮的id
     */
    public abstract String getChatDetailLuckyOpenId();

    /**
     * 聊天详情中消息编辑器id
     */
    public abstract String getChatDetailEditorId();

    /**
     * 聊天详情中消息发送按钮id
     */
    public abstract String getChatDetailSendId();
}
