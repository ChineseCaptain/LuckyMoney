package com.uu.helper.job;

import android.app.Notification;
import android.view.accessibility.AccessibilityEvent;

import com.uu.helper.service.LuckyMoneyService;

public interface AccessbilityJob {
    String getTargetPackageName();
    void onCreateJob(LuckyMoneyService service);
    void onReceiveJob(AccessibilityEvent event);
    void onStopJob();
    void onNotificationPosted(Notification notification);
    boolean isEnable();
}
