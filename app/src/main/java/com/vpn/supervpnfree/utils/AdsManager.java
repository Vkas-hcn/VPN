package com.vpn.supervpnfree.utils;

import android.app.Activity;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.VideoOptions;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.vpn.supervpnfree.BuildConfig;
import com.vpn.supervpnfree.R;


public class AdsManager {
    private static AdsManager ourInstance = new AdsManager();
    Activity context;
    InterstitialAd mInterstitialAd;


    public static AdsManager getInstance() {
        return ourInstance;
    }

    public void initAds(final Activity context1) {
        this.context = context1;
        MobileAds.initialize(context, initializationStatus -> {
        });
        LoadInterstitialAd(context);
    }

    public void LoadInterstitialAd(Activity ctx) {
        this.context = ctx;
        if (BuildConfig.GOOGlE_AD) {
            AdRequest adRequest = new AdRequest.Builder().build();
            InterstitialAd.load(ctx, (BuildConfig.GOOGLE_INTERSTITIAL), adRequest, new InterstitialAdLoadCallback() {
                @Override
                public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                    mInterstitialAd = interstitialAd;
                }

                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    mInterstitialAd = null;
                }
            });

        }
    }


    public void showInterstitialAd(Activity activity) {
        context = activity;
        if (mInterstitialAd != null) {
            mInterstitialAd.show(context);
        } else {
            LoadInterstitialAd(context);
        }

    }

    public void loadcustomnative(Activity activity, RelativeLayout frameLayout) {
        AdLoader.Builder builder = new AdLoader.Builder(activity, BuildConfig.GOOGLE_NATIVE);
        builder.forNativeAd(new NativeAd.OnNativeAdLoadedListener() {
            @Override
            public void onNativeAdLoaded(NativeAd nativeAd) {
                NativeAdView adView = (NativeAdView) activity.getLayoutInflater().inflate(R.layout.admob_banner_native_quick, frameLayout, false);
                populateNativeAdView(nativeAd, adView);
                frameLayout.removeAllViews();
                frameLayout.setPadding(5, 5, 5, 5);
                frameLayout.setBackgroundResource(R.drawable.banner_ad_back_layout);
                frameLayout.addView(adView);
                frameLayout.setVisibility(View.VISIBLE);

            }
        });

        VideoOptions videoOptions = new VideoOptions.Builder().setStartMuted(true).build();

        NativeAdOptions adOptions = new NativeAdOptions.Builder().setVideoOptions(videoOptions).build();

        builder.withNativeAdOptions(adOptions);

        AdLoader adLoader = builder.withAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(LoadAdError loadAdError) {

            }
        }).build();

        adLoader.loadAd(new AdRequest.Builder().build());
    }

    private void populateNativeAdView(NativeAd nativeAd, NativeAdView adView) {
        adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
        adView.setBodyView(adView.findViewById(R.id.ad_body));
        adView.setCallToActionView(adView.findViewById(R.id.ad_call_to_action));
        adView.setIconView(adView.findViewById(R.id.ad_app_icon));
        ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());
        if (nativeAd.getBody() == null) {
            adView.getBodyView().setVisibility(View.INVISIBLE);
        } else {
            adView.getBodyView().setVisibility(View.VISIBLE);
            ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
        }
        if (nativeAd.getCallToAction() == null) {
            adView.getCallToActionView().setVisibility(View.INVISIBLE);
        } else {
            adView.getCallToActionView().setVisibility(View.VISIBLE);
            ((TextView) adView.getCallToActionView()).setText(nativeAd.getCallToAction());
        }
        if (nativeAd.getIcon() == null) {
            adView.getIconView().setVisibility(View.GONE);
        } else {
            ((ImageView) adView.getIconView()).setImageDrawable(nativeAd.getIcon().getDrawable());
            adView.getIconView().setVisibility(View.VISIBLE);
        }
        adView.setNativeAd(nativeAd);
    }
}
