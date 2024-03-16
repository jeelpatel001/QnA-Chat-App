package com.jeelpatel.qnachat.helper;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

public class InterstitialAdHelper {

    private InterstitialAd mInterstitialAd;
    private final Context context;
    private final String ad_unit_id;
    public InterstitialAdHelper(Context context, String ad_unit_id) {
        this.context = context;
        this.ad_unit_id = ad_unit_id;
    }

    public void loadInterstitialAd() {

        MobileAds.initialize(context, initializationStatus -> {
        });
        AdRequest adRequest = new AdRequest.Builder().build();

        InterstitialAd.load(context, ad_unit_id, adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                // The mInterstitialAd reference will be null until
                // an ad is loaded.
                mInterstitialAd = interstitialAd;
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                // Handle the error
                mInterstitialAd = null;
            }
        });
    }

    public void showAd() {
        if (mInterstitialAd != null) {
            mInterstitialAd.show((Activity) context);
            loadInterstitialAd();
        }
    }
}