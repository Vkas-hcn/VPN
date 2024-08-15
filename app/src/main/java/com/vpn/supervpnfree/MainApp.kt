package com.vpn.supervpnfree;

import android.annotation.SuppressLint;
import android.content.Context;
import androidx.multidex.MultiDexApplication;

import com.tencent.mmkv.MMKV;
import com.vpn.supervpnfree.data.Hot;
import com.vpn.supervpnfree.updata.UpDataUtils;
import com.vpn.supervpnfree.utils.AdManager;
import com.vpn.supervpnfree.utils.GlobalTimer;

public class MainApp extends MultiDexApplication {

    @SuppressLint("StaticFieldLeak")
    private static Context context;
    @SuppressLint("StaticFieldLeak")
    private static MainApp mAppInstance;

    public static Context getContext() {
        return context;
    }

    public static synchronized MainApp getAppInstance() {
        return mAppInstance;
    }

    public static AdManager adManager;
    public static GlobalTimer globalTimer;

    public static MMKV saveLoadManager;

    @Override
    public void onCreate() {
        super.onCreate();
        mAppInstance = this;
        context = this;
        initHydraSdk();
        Hot.INSTANCE.initCore(this);
        if(Hot.INSTANCE.isMainProcess(this)){
            adManager = new AdManager(this);
            Hot.INSTANCE.adAndFirebaseBase(this);
            Hot.INSTANCE.registerAppLifeCallback(this);
            globalTimer = new GlobalTimer();
        }
    }


    public void initHydraSdk() {
        MMKV.initialize(this);
        saveLoadManager =
                MMKV.mmkvWithID("EasyVPN", MMKV.MULTI_PROCESS_MODE);
    }


}
