package com.vpn.supervpnfree.activities;

import static com.vpn.supervpnfree.utils.BillConfig.PRIMIUM_STATE;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.airbnb.lottie.LottieAnimationView;
import com.github.shadowsocks.Core;
import com.github.shadowsocks.aidl.ShadowsocksConnection;
import com.github.shadowsocks.preference.DataStore;
import com.github.shadowsocks.preference.OnPreferenceDataStoreChangeListener;
import com.vpn.supervpnfree.BuildConfig;
import com.vpn.supervpnfree.Preference;
import com.vpn.supervpnfree.R;
import com.vpn.supervpnfree.data.Hot;
import com.vpn.supervpnfree.data.KeyAppFun;
import com.vpn.supervpnfree.data.ServiceData;
import com.vpn.supervpnfree.updata.UpDataUtils;
import com.vpn.supervpnfree.utils.Converter;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import kotlin.Unit;


public abstract class UIActivity extends BaseActivity implements View.OnClickListener, ShadowsocksConnection.Callback,
        OnPreferenceDataStoreChangeListener {

    protected static final String TAG = MainActivity.class.getSimpleName();
    public String SKU_DELAROY_YEARLY = "";

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

    @BindView(R.id.connection_layout)
    ConstraintLayout connection_layout;

    @BindView(R.id.lav_guide)
    LottieAnimationView lav_guide;
    @BindView(R.id.view_guide_1)
    View view_guide_1;

    @BindView(R.id.con_loading)
    ConstraintLayout con_loading;

    @BindView(R.id.img_disconnect)
    ImageView img_disconnect;
    @BindView(R.id.img_yuan_1)
    ImageView img_yuan_1;
    @BindView(R.id.img_yuan_2)
    ImageView img_yuan_2;
    @BindView(R.id.img_yuan_3)
    ImageView img_yuan_3;
    @BindView(R.id.tv_date)
    TextView tv_date;
    @BindView(R.id.ad_layout)
    public ConstraintLayout ad_layout;
    @BindView(R.id.img_oc_ad)
    public ImageView img_oc_ad;
    @BindView(R.id.ad_layout_admob)
    public FrameLayout ad_layout_admob;

    @BindView(R.id.privacybtn)
    LinearLayout privacybtn;
    Preference preference;
    private Handler mUIHandler = new Handler(Looper.getMainLooper());
    public ShadowsocksConnection connection;

    final Runnable mUIUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            mUIHandler.postDelayed(mUIUpdateRunnable, 10000);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        preference = new Preference(this);
        connection = new ShadowsocksConnection(true);
        connection.connect(this, this);
        DataStore.INSTANCE.getPublicStore().registerChangeListener(this);
        ServiceData bean = Hot.INSTANCE.getCLickServiceData(this);
        if (bean == null) {
            Hot.INSTANCE.initVPNSet(preference, null);
        } else {
            Hot.INSTANCE.initVPNSet(preference, bean);
            setVpnUi(bean);
        }
    }


    public void setVpnUi(ServiceData bean) {
        country_flag.setImageResource(KeyAppFun.INSTANCE.getFlagImageData(bean.getWIqcDNWy()));
        selectedServerTextView.setText(bean.getWIqcDNWy());
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
    }

    @Override
    protected void onStop() {
        super.onStop();
        connection.setBandwidthTimeout(0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DataStore.INSTANCE.getPublicStore().unregisterChangeListener(this);
        connection.disconnect(this);
    }


    public void startVpnProcess() {
        Core.INSTANCE.startService();
    }

    public void stopVpnProcess() {
        Core.INSTANCE.stopService();
    }


}