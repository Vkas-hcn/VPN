package com.vpn.supervpnfree.activities;

import static com.vpn.supervpnfree.utils.BillConfig.BUNDLE;
import static com.vpn.supervpnfree.utils.BillConfig.COUNTRY_DATA;
import static com.vpn.supervpnfree.utils.BillConfig.SELECTED_COUNTRY;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Process;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.airbnb.lottie.LottieAnimationView;
import com.google.gson.Gson;
import com.vpn.supervpnfree.MainApp;
import com.vpn.supervpnfree.R;
import com.vpn.supervpnfree.dialog.CountryData;
import com.vpn.supervpnfree.dialog.LoginDialog;
import com.vpn.supervpnfree.utils.AdsManager;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;

public class MainActivity extends UIActivity  {


    private String selectedCountry = "";
    private String ServerIPaddress = "00.000.000.00";
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.uploading_graph)
    protected LottieAnimationView uploading_state_animation;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.downloading_graph)
    protected LottieAnimationView downloading_state_animation;

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = getIntent();
        if (intent != null) {
            Gson gson = new Gson();
            Bundle args = intent.getBundleExtra(BUNDLE);
            if (args != null) {
                CountryData item = gson.fromJson(args.getString(COUNTRY_DATA), CountryData.class);
                onRegionSelected(item);
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 3000) {
            if (resultCode == RESULT_OK) {
                Gson gson = new Gson();
                Bundle args = data.getBundleExtra(BUNDLE);
                CountryData item = gson.fromJson(args.getString(COUNTRY_DATA), CountryData.class);
                onRegionSelected(item);
            }
        }
    }


    public void onRegionSelected(CountryData item) {


    }


    @Override
    public void onClick(View view) {

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity
                .this);
        alertDialog.setTitle("Leave Application?");
        alertDialog.setMessage("Are you sure you want to leave the application?");
        alertDialog.setIcon(R.mipmap.ic_launcher);
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                moveTaskToBack(true);
                Process.killProcess(Process.myPid());
                finish();
            }
        });
        alertDialog.setNegativeButton("NO", null);
        Dialog d = alertDialog.show();
        int dividerId = d.getContext().getResources().getIdentifier("android:id/titleDivider", null, null);
        View divider = d.findViewById(dividerId);

        int textViewId = d.getContext().getResources().getIdentifier("android:id/alertTitle", null, null);
        TextView tv = (TextView) d.findViewById(textViewId);
    }

}
