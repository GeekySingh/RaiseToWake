package com.gagan.raise.to.wake;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;

import com.applovin.adview.AppLovinInterstitialAd;
import com.applovin.adview.AppLovinInterstitialAdDialog;
import com.applovin.sdk.AppLovinSdk;
import com.gagan.raise.to.wake.persistant.AppPref;
import com.gagan.raise.to.wake.service.RaiseToWakeService;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.NativeExpressAdView;

/**
 * Main activity
 */
public class MainActivity extends AppCompatActivity
        implements
        Switch.OnCheckedChangeListener {

    private AppPref mPref;
    private LinearLayout mLlProgressBar;

    private InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPref = AppPref.getInstance(this);

        setContentView(R.layout.activity_main);
        mLlProgressBar = (LinearLayout) findViewById(R.id.ll_progress_bar);

        Switch btnStartService = (Switch) findViewById(R.id.btn_start_service);
        Switch btnAutoStart = (Switch) findViewById(R.id.btn_auto_start);
        Switch btnProximitySensor = (Switch) findViewById(R.id.btn_enable_proximity_sensor);

        // load saved settings
        btnStartService.setChecked(RaiseToWakeService.mIsServiceRunning);
        btnAutoStart.setChecked(mPref.isAutoStart());
        btnProximitySensor.setChecked(mPref.isProximitySensorEnabled());

        // set check change event listener
        btnStartService.setOnCheckedChangeListener(this);
        btnAutoStart.setOnCheckedChangeListener(this);
        btnProximitySensor.setOnCheckedChangeListener(this);

        // initialize applovin sdk
        AppLovinSdk.initializeSdk(this);

        showInterstitial();
        showNativeAd();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        showInterstitial();
        showNativeAd();
        switch (buttonView.getId()) {
            case R.id.btn_start_service:
                if (isChecked)
                    startService(new Intent(this, RaiseToWakeService.class));
                else
                    stopService(new Intent(this, RaiseToWakeService.class));
                break;

            case R.id.btn_auto_start:
                mPref.setAutoStart(isChecked);
                break;

            case R.id.btn_enable_proximity_sensor:
                mPref.setEnableProximitySensor(isChecked);
                break;
        }
    }

    private void showInterstitial() {
        // Show the ad if it's ready. Otherwise toast and reload the ad.
        if (mInterstitialAd != null && mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        }
        mInterstitialAd = newInterstitialAd();
        loadInterstitial();
        showApplovinInterstital();
    }

    private InterstitialAd newInterstitialAd() {
        InterstitialAd interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId(getString(R.string.interstital_ad_unit_id));
        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                if (mLlProgressBar.getVisibility() == View.VISIBLE)
                    mLlProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                if (mLlProgressBar.getVisibility() == View.VISIBLE)
                    mLlProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onAdClosed() {
            }
        });
        return interstitialAd;
    }

    private void loadInterstitial() {
        AdRequest adRequest = new AdRequest.Builder().build();
        mInterstitialAd.loadAd(adRequest);
    }

    private void showApplovinInterstital() {
        if (AppLovinInterstitialAd.isAdReadyToDisplay(this)) {
            // An ad is available to display.  It's safe to call show.
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    AppLovinInterstitialAd.show(MainActivity.this);
                }
            }, 1000);
        } else {
            AppLovinInterstitialAdDialog dialog = AppLovinInterstitialAd.create(AppLovinSdk.getInstance(this), this);
            dialog.show();
        }
    }

    private void showNativeAd() {
        NativeExpressAdView mAdView = (NativeExpressAdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }
}
