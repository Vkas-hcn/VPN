package com.vpn.supervpnfree.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.vpn.supervpnfree.BuildConfig;
import com.vpn.supervpnfree.R;
import com.vpn.supervpnfree.adapters.LocationListAdapter;
import com.vpn.supervpnfree.dialog.CountryData;
import com.google.gson.Gson;
import com.vpn.supervpnfree.utils.AdsManager;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.vpn.supervpnfree.utils.BillConfig.BUNDLE;
import static com.vpn.supervpnfree.utils.BillConfig.COUNTRY_DATA;

public class ServerActivity extends AppCompatActivity {
    private static InterstitialAd mInterstitialAd;
    @BindView(R.id.regions_recycler_view)
    RecyclerView regionsRecyclerView;

    @BindView(R.id.regions_progress)
    ProgressBar regionsProgressBar;

    private LocationListAdapter regionAdapter;
    private RegionChooserInterface regionChooserInterface;
    ImageView backToActivity;
    TextView activity_name;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);
        ButterKnife.bind(this);
        LoadBannerAd();
        LoadInterstitialAd();
        activity_name = findViewById(R.id.activity_name);
        backToActivity = findViewById(R.id.finish_activity);
        activity_name.setText("Select Country/Region");
        backToActivity.setOnClickListener(view -> finish());
        regionChooserInterface = item -> {
            if (!item.isPro()) {
                Intent intent = new Intent(ServerActivity.this,MainActivity.class);
                Bundle args = new Bundle();
                Gson gson = new Gson();
                String json = gson.toJson(item);
                args.putString(COUNTRY_DATA, json);
                intent.putExtra(BUNDLE, args);
                startActivity(intent);
                finish();
                showInterstial();

            }
        };

        regionsRecyclerView.setHasFixedSize(true);
        regionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        regionAdapter = new LocationListAdapter(item -> regionChooserInterface.onRegionSelected(item), ServerActivity.this);
        regionsRecyclerView.setAdapter(regionAdapter);
        loadServers();
    }

    private void loadServers() {
        showProgress();
//        UnifiedSdk.getInstance().getBackend().countries(new Callback<AvailableCountries>() {
//            @Override
//            public void success(@NonNull final AvailableCountries countries) {
//                hideProress();
//                regionAdapter.setRegions(countries.getCountries());
//            }
//
//            @Override
//            public void failure(@NonNull VpnException e) {
//                hideProress();
//            }
//        });
    }

    public void LoadBannerAd() {
        RelativeLayout adContainer = findViewById(R.id.adView);
        if (BuildConfig.GOOGlE_AD) {
            AdsManager.getInstance().loadcustomnative(ServerActivity.this, adContainer);
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
        if (BuildConfig.GOOGlE_AD) {
            AdRequest adRequest = new AdRequest.Builder().build();
            InterstitialAd.load(this, (BuildConfig.GOOGLE_INTERSTITIAL), adRequest, new InterstitialAdLoadCallback() {
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


    public void showInterstial() {
        if (mInterstitialAd != null) {
            mInterstitialAd.show(ServerActivity.this);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public interface RegionChooserInterface {
        void onRegionSelected(CountryData item);
    }
}
