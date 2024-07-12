package com.vpn.supervpnfree.activities;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.ump.ConsentForm;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.UserMessagingPlatform;
import com.vpn.supervpnfree.Preference;
import com.vpn.supervpnfree.R;
import com.vpn.supervpnfree.utils.AdsManager;

import java.util.concurrent.atomic.AtomicBoolean;

import butterknife.ButterKnife;

@SuppressLint("CustomSplashScreen")
public  class SplashActivity extends AppCompatActivity {
    Handler handler;
    Preference preference;
    private final AtomicBoolean isMobileAdsInitializeCalled = new AtomicBoolean(false);
    ConsentInformation consentInformation;


    @SuppressLint("SetTextI18n")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        preference = new Preference(this);
        handler = new Handler();
        ConsentRequestParameters params = new ConsentRequestParameters.Builder().setTagForUnderAgeOfConsent(false).build();
        consentInformation = UserMessagingPlatform.getConsentInformation(this);
        consentInformation.requestConsentInfoUpdate(this, params, () -> {
            UserMessagingPlatform.loadAndShowConsentFormIfRequired(this, loadAndShowError -> {
                if (loadAndShowError != null) {
                }
                if (consentInformation.canRequestAds()) {
                    if (isMobileAdsInitializeCalled.getAndSet(true)) {
                        return;
                    }
                    AdsManager.getInstance().initAds(this);
                }
            });
        }, requestConsentError -> {
        });
        if (consentInformation.canRequestAds()) {
            if (isMobileAdsInitializeCalled.getAndSet(true)) {
                return;
            }
            AdsManager.getInstance().initAds(this);
        }
        ButterKnife.bind(this);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                finish();
                AdsManager.getInstance().showInterstitialAd(SplashActivity.this);
            }
        }, 4000);

        SplashFun.INSTANCE.getFirebaseDataMeteor(this);
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }


}
