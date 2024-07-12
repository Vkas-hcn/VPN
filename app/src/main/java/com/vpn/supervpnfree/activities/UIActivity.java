package com.vpn.supervpnfree.activities;

import static com.vpn.supervpnfree.utils.BillConfig.INAPPSKUUNIT;
import static com.vpn.supervpnfree.utils.BillConfig.One_Year_Sub;
import static com.vpn.supervpnfree.utils.BillConfig.PRIMIUM_STATE;
import static com.vpn.supervpnfree.utils.BillConfig.PURCHASETIME;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.vpn.supervpnfree.BuildConfig;
import com.vpn.supervpnfree.Preference;
import com.vpn.supervpnfree.R;
import com.vpn.supervpnfree.utils.AdsManager;
import com.vpn.supervpnfree.utils.Converter;

import java.util.ArrayList;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public abstract class UIActivity extends AppCompatActivity implements View.OnClickListener {

    protected static final String TAG = MainActivity.class.getSimpleName();
    public String SKU_DELAROY_YEARLY="";
    @BindView(R.id.server_ip)
    TextView server_ip;
    @BindView(R.id.img_connect)
    ImageView img_connect;
    @BindView(R.id.connection_state)
    ImageView connectionStateTextView;

    @BindView(R.id.optimal_server_btn)
    LinearLayout currentServerBtn;
    @BindView(R.id.selected_server)
    TextView selectedServerTextView;
    @BindView(R.id.country_flag)
    ImageView country_flag;
    @BindView(R.id.uploading_speed)
    TextView uploading_speed_textview;
    @BindView(R.id.downloading_speed)
    TextView downloading_speed_textview;


    @BindView(R.id.premium)
    ImageView premium;
    Preference preference;
    boolean mSubscribedToDelaroy = false;
    boolean connected = false;
    String mDelaroySku = "";
    boolean mAutoRenewEnabled = false;


    int[] Onconnect = {R.drawable.ic_on};
    int[] Ondisconnect = {R.drawable.ic_off};
    private Handler mUIHandler = new Handler(Looper.getMainLooper());

    final Runnable mUIUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            updateUI();
            mUIHandler.postDelayed(mUIUpdateRunnable, 10000);
        }
    };

    void complain(String message) {
        alert("Error: " + message);
    }

    void alert(String message) {
        android.app.AlertDialog.Builder bld = new android.app.AlertDialog.Builder(this);
        bld.setMessage(message);
        bld.setNeutralButton("OK", null);
        bld.create().show();
    }

    private void unlockdata() {
        if (mSubscribedToDelaroy) {
            unlock();
        } else {
            preference.setBooleanpreference(PRIMIUM_STATE, false);
        }
        if (!preference.isBooleenPreference(PRIMIUM_STATE)) {
            premium.setVisibility(View.VISIBLE);

        } else {
            premium.setVisibility(View.GONE);

        }


        LoadBannerAd();


    }

    public void unlock() {
        preference.setBooleanpreference(PRIMIUM_STATE, true);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) LinearLayout img_rate = findViewById(R.id.privacybtn);
        img_rate.setOnClickListener(v -> startActivity(new Intent("android.intent.action.VIEW", Uri.parse("https://maxisoftapps.blogspot.com/"))));
        preference = new Preference(this);
    }


    @Override
    public void setTitle(CharSequence title) {
        getSupportActionBar().setTitle(title);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }


    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
        stopUIUpdateTask();
    }

    @OnClick(R.id.premium)
    public void premiumMenu(View v) {
    }

    @OnClick(R.id.img_connect)
    public void onConnectBtnClick(View v) {

    }

    @OnClick(R.id.optimal_server_btn)
    public void onServerChooserClick(View v) {
    }


    protected void startUIUpdateTask() {
        stopUIUpdateTask();
        mUIHandler.post(mUIUpdateRunnable);
    }

    protected void stopUIUpdateTask() {
        mUIHandler.removeCallbacks(mUIUpdateRunnable);
        updateUI();
    }


    protected void updateUI() {
//        UnifiedSdk.getVpnState(new Callback<VpnState>() {
//            @Override
//            public void success(@NonNull VpnState vpnState) {
//                switch (vpnState) {
//                    case IDLE: {
//                        Log.e(TAG, "success: IDLE");
//                        connectionStateTextView.setImageResource(R.drawable.disc);
//                        /*getip();*/
//                        if (connected) {
//                            connected = false;
//                            animate(img_connect, Ondisconnect, 0, false);
//                        }
//                        country_flag.setImageDrawable(getResources().getDrawable(R.drawable.ic_earth));
//                        selectedServerTextView.setText(R.string.select_country);
//                        ChangeBlockVisibility();
//                        uploading_speed_textview.setText("");
//                        downloading_speed_textview.setText("");
//
//                        hideConnectProgress();
//                        break;
//                    }
//                    case CONNECTED: {
//                        Log.e(TAG, "success: CONNECTED");
//                        if (!connected) {
//                            connected = true;
//                            animate(img_connect, Onconnect, 0, false);
//                        }
//                        connectionStateTextView.setImageResource(R.drawable.conne);
//                        hideConnectProgress();
//                        break;
//                    }
//                    case CONNECTING_VPN:
//                    case CONNECTING_CREDENTIALS:
//                    case CONNECTING_PERMISSIONS: {
//                        connectionStateTextView.setImageResource(R.drawable.connecting);
//                        ChangeBlockVisibility();
//                        country_flag.setImageDrawable(getResources().getDrawable(R.drawable.ic_earth));
//                        selectedServerTextView.setText(R.string.select_country);
//                        showConnectProgress();
//                        break;
//                    }
//                    case PAUSED: {
//                        Log.e(TAG, "success: PAUSED");
//                        ChangeBlockVisibility();
//                        country_flag.setImageDrawable(getResources().getDrawable(R.drawable.ic_earth));
//                        selectedServerTextView.setText(R.string.select_country);
//                        break;
//                    }
//                }
//            }
//
//            @Override
//            public void failure(@NonNull VpnException e) {
//                country_flag.setImageDrawable(getResources().getDrawable(R.drawable.ic_earth));
//                selectedServerTextView.setText(R.string.select_country);
//            }
//        });
//        getCurrentServer(new Callback<String>() {
//            @Override
//            public void success(@NonNull final String currentServer) {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        country_flag.setImageDrawable(getResources().getDrawable(R.drawable.ic_earth));
//                        selectedServerTextView.setText(R.string.select_country);
//                        if (!currentServer.equals("")) {
//                            Locale locale = new Locale("", currentServer);
//                            Resources resources = getResources();
//                            String sb = "drawable/" + currentServer.toLowerCase();
//                            country_flag.setImageResource(resources.getIdentifier(sb, null, getPackageName()));
//                            selectedServerTextView.setText(locale.getDisplayCountry());
//                        } else {
//                            country_flag.setImageDrawable(getResources().getDrawable(R.drawable.ic_earth));
//                            selectedServerTextView.setText(R.string.select_country);
//                        }
//                    }
//                });
//            }
//
//            @Override
//            public void failure(@NonNull VpnException e) {
//                country_flag.setImageDrawable(getResources().getDrawable(R.drawable.ic_earth));
//                selectedServerTextView.setText(R.string.select_country);
//            }
//        });
    }



    private void animate(final ImageView imageView, final int images[], final int imageIndex, final boolean forever) {


        int fadeInDuration = 500;
        int timeBetween = 3000;
        int fadeOutDuration = 1000;

        imageView.setVisibility(View.VISIBLE);
        imageView.setImageResource(images[imageIndex]);

        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new DecelerateInterpolator());
        fadeIn.setDuration(fadeInDuration);

        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new AccelerateInterpolator());
        fadeOut.setStartOffset(fadeInDuration + timeBetween);
        fadeOut.setDuration(fadeOutDuration);

        AnimationSet animation = new AnimationSet(false);
        animation.addAnimation(fadeIn);

        animation.setRepeatCount(1);
        imageView.setAnimation(animation);

        animation.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationEnd(Animation animation) {
                if (images.length - 1 > imageIndex) {
                    animate(imageView, images, imageIndex + 1, forever); //Calls itself until it gets to the end of the array
                } else {
                    if (forever) {
                        animate(imageView, images, 0, forever);  //Calls itself to start the animation all over again in a loop if forever = true
                    }
                }
            }

            public void onAnimationRepeat(Animation animation) {
            }

            public void onAnimationStart(Animation animation) {
            }
        });
    }


    protected void updateTrafficStats(long outBytes, long inBytes) {
        String outString = Converter.humanReadableByteCountOld(outBytes, false);
        String inString = Converter.humanReadableByteCountOld(inBytes, false);

        uploading_speed_textview.setText(inString);
        downloading_speed_textview.setText(outString);

    }



    protected void ShowIPaddera(String ipaddress) {
        server_ip.setText(ipaddress);
    }


    protected void showConnectProgress() {

    }

    protected void hideConnectProgress() {

    }

    protected void showMessage(String msg) {
        Toast.makeText(UIActivity.this, msg, Toast.LENGTH_SHORT).show();
    }


    public void LoadBannerAd() {
        RelativeLayout adContainer = findViewById(R.id.adView);
        if (BuildConfig.GOOGlE_AD) {
            AdsManager.getInstance().loadcustomnative(UIActivity.this, adContainer);
        }
    }


}