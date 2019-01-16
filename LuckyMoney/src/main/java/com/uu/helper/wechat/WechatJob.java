package com.uu.helper.wechat;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.uu.helper.Config;
import com.uu.helper.ids.WechatIdsBase;
import com.uu.helper.ids.WechatIds_667;
import com.uu.helper.ids.WechatIds_673;
import com.uu.helper.ids.WechatIds_700;
import com.uu.helper.job.BaseAccessbilityJob;
import com.uu.helper.service.LuckyMoneyService;
import com.uu.helper.util.AccessibilityHelper;
import com.uu.helper.util.NotifyHelper;
import com.uu.helper.util.SharedPreferencesUtil;

import java.util.List;

public class WechatJob extends BaseAccessbilityJob {

    private static final String TAG = "WechatJob";

    /** 微信的包名*/
    public static final String WECHAT_PACKAGENAME = "com.tencent.mm";

    /** 红包消息的关键字*/
    private static final String HONGBAO_TEXT_KEY = "[微信红包]";

    private static final String BUTTON_CLASS_NAME = "android.widget.Button";

    /** 不能再使用文字匹配的最小版本号 */
    private static final int USE_ID_MIN_VERSION = 700;// 6.3.8 对应code为680,6.3.9对应code为700

//    private static final int WINDOW_NONE = 0;
//    private static final int WINDOW_LUCKYMONEY_RECEIVEUI = 1;
//    private static final int WINDOW_LUCKYMONEY_DETAIL = 2;
//    private static final int WINDOW_LAUNCHER = 3;
//    private static final int WINDOW_OTHER = -1;

//    private int mCurrentWindow = WINDOW_NONE;
    private WechatPage curPage = WechatPage.OTHER;

    private PackageInfo mWechatPackageInfo = null;
    private Handler mHandler = null;


    /**
     * 微信的控件id集合
     */
    WechatIdsBase mIds;

    /**
     * 冷却中
     */
    private boolean cooling = false;

    /**
     * 冷却时间
     * 微信7.0.0，每次抢完之后要冷却一段时间才可以抢下一个
     */
    private long coolTime = 0;







    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //更新安装包信息
            getWechatInfo();
        }
    };

    @Override
    public void onCreateJob(LuckyMoneyService service) {
        super.onCreateJob(service);
        // 获取微信安装包的信息
        getWechatInfo();

        // 注册广播监听微信安装包的更新
        IntentFilter filter = new IntentFilter();
        filter.addDataScheme("package");
        filter.addAction("android.intent.action.PACKAGE_ADDED");
        filter.addAction("android.intent.action.PACKAGE_REPLACED");
        filter.addAction("android.intent.action.PACKAGE_REMOVED");
        getContext().registerReceiver(broadcastReceiver, filter);
    }


    /**
     * APP发生了交互行为
     *
     * @param event
     */
    @Override
    public void onReceiveJob(AccessibilityEvent event) {
        final int eventType = event.getEventType();
        Log.i(TAG, "Event : "+eventType);
        //通知栏事件
        if(eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
            Parcelable data = event.getParcelableData();
            if(data == null || !(data instanceof Notification)) {
                return;
            }
            if(LuckyMoneyService.isNotificationServiceRunning() && getConfig().isEnableNotificationService()) { //开启快速模式，不处理
                return;
            }
            List<CharSequence> texts = event.getText();
            if(!texts.isEmpty()) {
                String text = String.valueOf(texts.get(0));
                notificationEvent(text, (Notification) data);
            }
            // 新版本的不在使用dialog，而是一个新的Activity。
        } else if(eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            handleChatListAndDetail(event);
        } else if(eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            // 页面内容发生了改变
            handleChatListHongBao();
//            if(isReceivingHongbao) {
//                handleChatListHongBao();
//            }
        }
    }

    /**
     * 获取当前APP的页面
     * @param event
     */
    private void handleChatListAndDetail(AccessibilityEvent event) {
        if(WechatConstant.CHAT_LIST_OR_DETAIL.equals(event.getClassName())) {
            Log.i(TAG, "在聊天列表或者聊天详情，去点开红包");
            // 在聊天列表或者聊天详情，去点开红包
            curPage = WechatPage.CHAT_LIST_OR_DETAIL;
            if (!cooling) {
                handleChatListHongBao();
            }
        } else if(WechatConstant.LUCK_MONEY_OPEN.equals(event.getClassName())
                || WechatConstant.LUCK_MONEY_OPEN_700.equals(event.getClassName())) {
            Log.i(TAG, "点中了红包，下一步就是去拆红包");
            curPage = WechatPage.LUCK_OPEN;
            //点中了红包，下一步就是去拆红包
            handleLuckyMoneyReceive();
        } else if(WechatConstant.LUCK_MONEY_RESULT.equals(event.getClassName())) {
            Log.i(TAG, "拆完红包");
            // 拆完红包
            curPage = WechatPage.LUCK_RESULT;
            cooling = true;
            AccessibilityHelper.performBack(getService());
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    sendMsg();
                    cooling = false;
                }
            }, coolTime);
        } else {
            // 其它页面
            curPage = WechatPage.OTHER;
        }
    }


    /**
     * 收到聊天里的红包
     * */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void handleChatListHongBao() {
        int mode = getConfig().getWechatMode();
        if(mode == Config.WX_MODE_3) { //只通知模式
            return;
        }

        AccessibilityNodeInfo nodeInfo = getService().getRootInActiveWindow();
        if(nodeInfo == null) {
            Log.w(TAG, "rootWindow为空");
            return;
        }

        if(mode != Config.WX_MODE_0) {
            boolean isMember = isMemberChatUi(nodeInfo);
            if(mode == Config.WX_MODE_1 && isMember) {//过滤群聊
                return;
            } else if(mode == Config.WX_MODE_2 && !isMember) { //过滤单聊
                return;
            }
        }

//        List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText("领取红包");
        List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByViewId(mIds.getChatDetailLuckyTextId());
        if (list == null)
            return;
        if(list.isEmpty()) {
            // 从消息列表查找红包
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                List<AccessibilityNodeInfo> nodes = nodeInfo.findAccessibilityNodeInfosByViewId(mIds.getChatListItemMsgContentId());
                if(nodes != null) {
                    for (AccessibilityNodeInfo node : nodes) {
                        if(node != null && node.getText().toString().contains("[微信红包]")) {
                            //从最近联系人列表中点击有红包的聊天记录

                            // 判断一下是否是未读的消息，已读的就不处理了
                            AccessibilityNodeInfo parent = node.getParent().getParent().getParent().getParent();
                            AccessibilityNodeInfo unreadText = AccessibilityHelper.findNodeInfosById(parent, mIds.getChatListItemMsgTextUnreadId());
                            AccessibilityNodeInfo unreadImg = AccessibilityHelper.findNodeInfosById(parent, mIds.getChatListItemMsgImgUnreadId());
                            if ((unreadText != null && unreadText.isVisibleToUser())
                                    || (unreadImg != null && unreadImg.isVisibleToUser())) {
                                AccessibilityHelper.performClick(node);
                            }
                            break;
                        }
                    }
                }
            }
        } else {
            // 第一次修改
//            AccessibilityNodeInfo node = list.get(list.size() - 1);
//            AccessibilityHelper.performClick(node);

            // 第二次修改
//            for (AccessibilityNodeInfo node : list) {
//                if (node.getParent().getParent() != null) {
//                    if (node.getParent().getParent().getParent() != null) {
//                        AccessibilityNodeInfo parent = node.getParent().getParent().getParent().getParent();
//                        if (parent.getChild(0) != null && parent.getChild(0).getClassName().equals("android.widget.RelativeLayout")) { // 接收的别人的消息
//                            // 是未领取的红包
//                            AccessibilityNodeInfo unread = AccessibilityHelper.findNodeInfosByTexts(parent, "已领取", "红包已领取");
//                            if (unread == null || !unread.isVisibleToUser()) {
//                                AccessibilityHelper.performClick(node);
//                                break;
//                            }
//                        }
//                    }
//                }
//            }

            // 第三次修改
            AccessibilityNodeInfo node = list.get(list.size()-1);
            if (node.getParent().getParent() != null) {
                if (node.getParent().getParent().getParent() != null) {
                    AccessibilityNodeInfo parent = node.getParent().getParent().getParent().getParent();
                    if (parent.getChild(0) != null && parent.getChild(0).getClassName().equals("android.widget.RelativeLayout")) { // 接收的别人的消息
                        // 是未领取的红包
                        AccessibilityNodeInfo unread = AccessibilityHelper.findNodeInfosByTexts(parent, "已领取", "红包已领取");
                        if (unread == null || !unread.isVisibleToUser()) {
                            AccessibilityHelper.performClick(node);
                        }
                    }
                }
            }
        }
    }

    /**
     * 点击聊天里的红包消息，弹出"開"
     * */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void handleLuckyMoneyReceive() {
        AccessibilityNodeInfo nodeInfo = getService().getRootInActiveWindow();
        if(nodeInfo == null) {
            Log.w(TAG, "rootWindow为空");
            return;
        }

        AccessibilityNodeInfo targetNode = null;

        int event = getConfig().getWechatAfterOpenHongBaoEvent();
        int wechatVersion = getWechatVersion();
        if(event == Config.WX_AFTER_OPEN_HONGBAO) { //拆红包
            if (wechatVersion < USE_ID_MIN_VERSION) {
                targetNode = AccessibilityHelper.findNodeInfosByText(nodeInfo, "拆红包");
            } else {
                targetNode = AccessibilityHelper.findNodeInfosById(nodeInfo, mIds.getChatDetailLuckyOpenId());

                if(targetNode == null) {
                    //分别对应固定金额的红包 拼手气红包
                    AccessibilityNodeInfo textNode = AccessibilityHelper.findNodeInfosByTexts(nodeInfo, "发了一个红包", "给你发了一个红包", "发了一个红包，金额随机");

                    if(textNode != null) {
                        for (int i = 0; i < textNode.getChildCount(); i++) {
                            AccessibilityNodeInfo node = textNode.getChild(i);
                            if (BUTTON_CLASS_NAME.equals(node.getClassName())) {
                                targetNode = node;
                                break;
                            }
                        }
                    }
                }

                if(targetNode == null) { //通过组件查找
                    targetNode = AccessibilityHelper.findNodeInfosByClassName(nodeInfo, BUTTON_CLASS_NAME);
                }
            }
        } else if(event == Config.WX_AFTER_OPEN_SEE) { //看一看
            if(getWechatVersion() < USE_ID_MIN_VERSION) { //低版本才有 看大家手气的功能
                targetNode = AccessibilityHelper.findNodeInfosByText(nodeInfo, "看看大家的手气");
            }
        } else if(event == Config.WX_AFTER_OPEN_NONE) {
            return;
        }

        if(targetNode != null) {
            final AccessibilityNodeInfo n = targetNode;
//            long sDelayTime = getConfig().getWechatOpenDelayTime();
            long sDelayTime = 200;
            if(sDelayTime != 0) {
                getHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        AccessibilityHelper.performClick(n);
                    }
                }, sDelayTime);
            } else {
                AccessibilityHelper.performClick(n);
                int wechatSum = SharedPreferencesUtil.getInt(getContext(), Config.KEY_WECHAT_SUM);
                wechatSum++;
                SharedPreferencesUtil.saveInt(getContext(), Config.KEY_WECHAT_SUM, wechatSum);
            }
        }
    }


    /**
     * 发送一条消息
     */
    private void sendMsg() {
        AccessibilityNodeInfo nodeInfo = getService().getRootInActiveWindow();
        if(nodeInfo == null) {
            Log.w(TAG, "rootWindow为空");
            return;
        }
        AccessibilityNodeInfo textNode = AccessibilityHelper.findNodeInfosById(nodeInfo, mIds.getChatDetailEditorId());
        if (textNode != null) {
            Bundle arguments = new Bundle();
            arguments.putCharSequence(AccessibilityNodeInfo
                    .ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, "谢谢老板！");
            textNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);

            // 点击发送
            AccessibilityNodeInfo sendNode = AccessibilityHelper.findNodeInfosById(nodeInfo, mIds.getChatDetailSendId());
            if (sendNode != null) {
                AccessibilityHelper.performClick(sendNode);
            }
        }
    }
























    @Override
    public void onStopJob() {
        try {
            getContext().unregisterReceiver(broadcastReceiver);
        } catch (Exception e) {}
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void onNotificationPosted(Notification notification) {
        String text = String.valueOf(notification.tickerText);
        notificationEvent(text, notification);
    }

    @Override
    public boolean isEnable() {
        return getConfig().isEnableWechat();
    }

    @Override
    public String getTargetPackageName() {
        return WECHAT_PACKAGENAME;
    }

    /** 是否为群聊天*/
    private boolean isMemberChatUi(AccessibilityNodeInfo nodeInfo) {
        if(nodeInfo == null) {
            return false;
        }
        String id = "com.tencent.mm:id/ces";
        int wv = getWechatVersion();
        if(wv <= 680) {
            id = "com.tencent.mm:id/ew";
        } else if(wv <= 700) {
            id = "com.tencent.mm:id/cbo";
        }
        String title = null;
        AccessibilityNodeInfo target = AccessibilityHelper.findNodeInfosById(nodeInfo, id);
        if(target != null) {
            title = String.valueOf(target.getText());
        }
        List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText("返回");

        if(list != null && !list.isEmpty()) {
            AccessibilityNodeInfo parent = null;
            for(AccessibilityNodeInfo node : list) {
                if(!"android.widget.ImageView".equals(node.getClassName())) {
                    continue;
                }
                String desc = String.valueOf(node.getContentDescription());
                if(!"返回".equals(desc)) {
                    continue;
                }
                parent = node.getParent();
                break;
            }
            if(parent != null) {
                parent = parent.getParent();
            }
            if(parent != null) {
                if( parent.getChildCount() >= 2) {
                    AccessibilityNodeInfo node = parent.getChild(1);
                    if("android.widget.TextView".equals(node.getClassName())) {
                        title = String.valueOf(node.getText());
                    }
                }
            }
        }


        if(title != null && title.endsWith(")")) {
            return true;
        }
        return false;
    }

    /** 通知栏事件*/
    private void notificationEvent(String ticker, Notification nf) {
        String text = ticker;
        int index = text.indexOf(":");
        if(index != -1) {
            text = text.substring(index + 1);
        }
        text = text.trim();
        if(text.contains(HONGBAO_TEXT_KEY)) { //红包消息
            newHongBaoNotification(nf);
        }
    }

    /** 打开通知栏消息*/
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void newHongBaoNotification(Notification notification) {
//        isReceivingHongbao = true;
        //以下是精华，将微信的通知栏消息打开
        PendingIntent pendingIntent = notification.contentIntent;
        boolean lock = NotifyHelper.isLockScreen(getContext());

        if(!lock) {
            NotifyHelper.send(pendingIntent);
        } else {
            NotifyHelper.showNotify(getContext(), String.valueOf(notification.tickerText), pendingIntent);
        }

        if(lock || getConfig().getWechatMode() != Config.WX_MODE_0) {
            NotifyHelper.playEffect(getContext(), getConfig());
        }
    }

//    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
//    private void openHongBao(AccessibilityEvent event) {
//        if("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI".equals(event.getClassName())) {
//            mCurrentWindow = WINDOW_LUCKYMONEY_RECEIVEUI;
//            //点中了红包，下一步就是去拆红包
//            handleLuckyMoneyReceive();
//        } else if("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI".equals(event.getClassName())) {
//            mCurrentWindow = WINDOW_LUCKYMONEY_DETAIL;
//            //拆完红包后看详细的纪录界面
//            if(getConfig().getWechatAfterGetHongBaoEvent() == Config.WX_AFTER_GET_GOHOME) { //返回主界面，以便收到下一次的红包通知
//                AccessibilityHelper.performHome(getService());
//            }
//        } else if("com.tencent.mm.ui.LauncherUI".equals(event.getClassName())) {
//            mCurrentWindow = WINDOW_LAUNCHER;
//            //在聊天界面,去点中红包
//            handleChatListHongBao();
//        } else {
//            mCurrentWindow = WINDOW_OTHER;
//        }
//    }



    private Handler getHandler() {
        if(mHandler == null) {
            mHandler = new Handler(Looper.getMainLooper());
        }
        return mHandler;
    }

    /** 获取微信的版本*/
    private int getWechatVersion() {
        if(mWechatPackageInfo == null) {
            return 0;
        }
        return mWechatPackageInfo.versionCode;
    }

    /**
     * 获取微信包信息
     */
    private void getWechatInfo() {
        try {
            mWechatPackageInfo = getContext().getPackageManager().getPackageInfo(WECHAT_PACKAGENAME, 0);

            // 获取到微信版本信息，并进行相应初始化
            if (mWechatPackageInfo != null) {
                // 初始化Id管理器
                if(mWechatPackageInfo.versionCode == 1360) {
                    mIds = new WechatIds_673();
                } else if (mWechatPackageInfo.versionCode == 1380) {
                    mIds = new WechatIds_700();
                    coolTime = 3000;
                } else {
                    mIds = new WechatIds_667();
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }
}
