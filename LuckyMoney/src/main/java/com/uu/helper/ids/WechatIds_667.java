package com.uu.helper.ids;

/**
 * 微信 6.6.7
 * author：zhangguiyou
 * date: 2019/1/16.
 */
public class WechatIds_667 extends WechatIdsBase {

    public WechatIds_667() {
    }

    @Override
    public String getChatListItemMsgContentId() {
        return "com.tencent.mm:id/as8";
    }

    @Override
    public String getChatListItemMsgTextUnreadId() {
        return "com.tencent.mm:id/k2";
    }

    @Override
    public String getChatListItemMsgImgUnreadId() {
        return "com.tencent.mm:id/as5";
    }

    @Override
    public String getChatDetailLuckyTextId() {
        return "com.tencent.mm:id/afp";
    }

    @Override
    public String getChatDetailLuckyOpenId() {
        return "com.tencent.mm:id/c85";
    }

    @Override
    public String getChatDetailEditorId() {
        return "com.tencent.mm:id/ac8";
    }

    @Override
    public String getChatDetailSendId() {
        return "com.tencent.mm:id/acd";
    }
}
