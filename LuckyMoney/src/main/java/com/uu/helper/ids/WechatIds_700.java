package com.uu.helper.ids;

/**
 * 微信7.0.0
 * author：zhangguiyou
 * date: 2019/1/16.
 */
public class WechatIds_700 extends WechatIdsBase {

    public WechatIds_700() {
    }

    @Override
    public String getChatListItemMsgContentId() {
        return "com.tencent.mm:id/b4q";
    }

    @Override
    public String getChatListItemMsgTextUnreadId() {
        return "com.tencent.mm:id/mm";
    }

    @Override
    public String getChatListItemMsgImgUnreadId() {
        return "com.tencent.mm:id/b4n";
    }

    @Override
    public String getChatDetailLuckyTextId() {
        return "com.tencent.mm:id/apf";
    }

    @Override
    public String getChatDetailLuckyOpenId() {
        return "com.tencent.mm:id/cv0";
    }

    @Override
    public String getChatDetailEditorId() {
        return "com.tencent.mm:id/alm";
    }

    @Override
    public String getChatDetailSendId() {
        return "com.tencent.mm:id/als";
    }
}
