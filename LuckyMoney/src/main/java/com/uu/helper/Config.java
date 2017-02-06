package com.uu.helper;

import android.content.Context;
import android.content.SharedPreferences;

import com.uu.helper.util.SharedPreferencesUtil;

public class Config {
    public static final String KEY_WECHAT_SUM = "wechat_sum";
    public static final String KEY_QQ_SUM = "qq_sum";
    public static final String KEY_WECHAT_ENABLE = "wechat_enable";
    public static final String KEY_QQ_ENABLE = "qq_enable";
    public static final String KEY_NOTIFY_SOUND = "KEY_NOTIFY_SOUND";//声音提醒
    public static final String KEY_NOTIFY_VIBRATE = "KEY_NOTIFY_VIBRATE";//震动
    public static final String KEY_NOTIFY_NIGHT_ENABLE = "KEY_NOTIFY_NIGHT_ENABLE";//夜间免打扰
    public static final String KEY_NOTIFICATION_SERVICE_ENABLE = "KEY_NOTIFICATION_SERVICE_ENABLE";//是否启动通知栏监听

    public static final String KEY_WECHAT_AFTER_OPEN_HONGBAO = "KEY_WECHAT_AFTER_OPEN_HONGBAO";
    public static final String KEY_WECHAT_DELAY_TIME = "KEY_WECHAT_DELAY_TIME";
    public static final String KEY_WECHAT_AFTER_GET_HONGBAO = "KEY_WECHAT_AFTER_GET_HONGBAO";
    public static final String KEY_WECHAT_MODE = "KEY_WECHAT_MODE";


    public static final int WX_AFTER_OPEN_HONGBAO = 0;//拆红包
    public static final int WX_AFTER_OPEN_SEE = 1; //看大家手气
    public static final int WX_AFTER_OPEN_NONE = 2; //静静地看着

    public static final int WX_AFTER_GET_GOHOME = 0; //返回桌面
    public static final int WX_AFTER_GET_NONE = 1;

    public static final int WX_MODE_0 = 0;//自动抢
    public static final int WX_MODE_1 = 1;//抢单聊红包,群聊红包只通知
    public static final int WX_MODE_2 = 2;//抢群聊红包,单聊红包只通知
    public static final int WX_MODE_3 = 3;//通知手动抢

    private static Config current;

    public static synchronized Config getConfig(Context context) {
        if(current == null) {
            current = new Config(context.getApplicationContext());
        }
        return current;
    }

    private Context mContext;

    private Config(Context context) {
        mContext = context;
    }

    /** 是否启动微信抢红包*/
    public boolean isEnableWechat() {
        return SharedPreferencesUtil.getBoolean(mContext, KEY_WECHAT_ENABLE);
    }

    /** 微信打开红包后的事件*/
    public int getWechatAfterOpenHongBaoEvent() {
        return SharedPreferencesUtil.getInt(mContext, KEY_WECHAT_AFTER_OPEN_HONGBAO);
    }

    /** 微信抢到红包后的事件*/
    public int getWechatAfterGetHongBaoEvent() {
        return SharedPreferencesUtil.getIntWithDefault(mContext, KEY_WECHAT_AFTER_GET_HONGBAO, 1);
    }

    /** 微信打开红包后延时时间*/
    public int getWechatOpenDelayTime() {
        return SharedPreferencesUtil.getIntWithDefault(mContext, KEY_WECHAT_DELAY_TIME, 0);
    }

    /** 获取抢微信红包的模式*/
    public int getWechatMode() {
        return SharedPreferencesUtil.getIntWithDefault(mContext, KEY_WECHAT_MODE, 0);
    }

    /** 是否启动通知栏模式*/
    public boolean isEnableNotificationService() {
        return SharedPreferencesUtil.getBooleanWithDefault(mContext, KEY_NOTIFICATION_SERVICE_ENABLE, false);
    }

    public void setNotificationServiceEnable(boolean enable) {
        SharedPreferencesUtil.saveBoolean(mContext, KEY_NOTIFICATION_SERVICE_ENABLE, enable);
    }

    /** 是否开启声音*/
    public boolean isNotifySound() {
        return SharedPreferencesUtil.getBooleanWithDefault(mContext, KEY_NOTIFY_SOUND, true);
    }

    /** 是否开启震动*/
    public boolean isNotifyVibrate() {
        return SharedPreferencesUtil.getBooleanWithDefault(mContext, KEY_NOTIFY_VIBRATE, true);
    }

    /** 是否开启夜间免打扰模式*/
    public boolean isNotifyNight() {
        return SharedPreferencesUtil.getBooleanWithDefault(mContext, KEY_NOTIFY_NIGHT_ENABLE, false);
    }

}
