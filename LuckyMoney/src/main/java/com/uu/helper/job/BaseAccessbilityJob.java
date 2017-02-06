package com.uu.helper.job;

import android.content.Context;

import com.uu.helper.Config;
import com.uu.helper.service.LuckyMoneyService;

public abstract class BaseAccessbilityJob implements AccessbilityJob {

    private LuckyMoneyService service;

    @Override
    public void onCreateJob(LuckyMoneyService service) {
        this.service = service;
    }

    public Context getContext() {
        return service.getApplicationContext();
    }

    public Config getConfig() {
        return service.getConfig();
    }

    public LuckyMoneyService getService() {
        return service;
    }
}
