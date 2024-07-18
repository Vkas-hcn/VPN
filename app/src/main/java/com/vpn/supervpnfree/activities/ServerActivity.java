package com.vpn.supervpnfree.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.vpn.supervpnfree.BuildConfig;
import com.vpn.supervpnfree.MainApp;
import com.vpn.supervpnfree.Preference;
import com.vpn.supervpnfree.R;
import com.vpn.supervpnfree.adapters.LocationListAdapter;
import com.vpn.supervpnfree.data.Hot;
import com.vpn.supervpnfree.data.KeyAppFun;
import com.vpn.supervpnfree.data.ServiceData;
import com.vpn.supervpnfree.dialog.CountryData;
import com.google.gson.Gson;

import butterknife.BindView;
import butterknife.ButterKnife;
import kotlin.Unit;

import static com.vpn.supervpnfree.utils.BillConfig.BUNDLE;
import static com.vpn.supervpnfree.utils.BillConfig.COUNTRY_DATA;

import java.util.List;

public class ServerActivity extends BaseActivity {

    @BindView(R.id.regions_recycler_view)
    RecyclerView regionsRecyclerView;

    @BindView(R.id.tv_no_data)
    TextView regionsProgressBar;

    @BindView(R.id.con_load_ad)
    public ConstraintLayout con_load_ad;

    private LocationListAdapter regionAdapter;
    ImageView backToActivity;
    TextView activity_name;

    List<ServiceData> allListData;

    Preference preference;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);
        ButterKnife.bind(this);
        LoadInterstitialAd();
        activity_name = findViewById(R.id.activity_name);
        backToActivity = findViewById(R.id.finish_activity);
        activity_name.setText("Server List");
        backToActivity.setOnClickListener(view -> Hot.INSTANCE.showReturnFun(ServerActivity.this));
        preference = new Preference(this);
        loadData();
        regionsRecyclerView.setHasFixedSize(true);
        regionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        regionAdapter = new LocationListAdapter(this, allListData);
        regionsRecyclerView.setAdapter(regionAdapter);
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Hot.INSTANCE.showReturnFun(ServerActivity.this);
            }
        });
    }


    public void loadData() {
        MainApp.adManager.loadAd(KeyAppFun.list_type);
        if (Hot.INSTANCE.isHaveVpnData(preference, null, () -> Unit.INSTANCE)) {
            allListData = Hot.INSTANCE.getAllData(preference);
            hideProress();
        } else {
            showProgress();
        }
    }

    private void showProgress() {
        regionsProgressBar.setVisibility(View.VISIBLE);
        regionsRecyclerView.setVisibility(View.INVISIBLE);
    }

    private void hideProress() {
        regionsProgressBar.setVisibility(View.GONE);
        regionsRecyclerView.setVisibility(View.VISIBLE);
    }

    private void LoadInterstitialAd() {

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
