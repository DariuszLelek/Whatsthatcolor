package com.omikronsoft.whatsthatcolor.ad;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.omikronsoft.whatsthatcolor.R;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Dariusz Lelek on 10/9/2017.
 * dariusz.lelek@gmail.com
 */

public class AdHolder extends Application{
    private static AdHolder instance;
    private static final Lock lock = new ReentrantLock();
    private static AdView adView;

    public static AdHolder getInstance(){
        if(instance == null){
            synchronized (lock){
                instance = new AdHolder();
            }
        }
        return instance;
    }

    public AdView getAdView(Context context, Resources res){
        if(adView == null){
            MobileAds.initialize(context, res.getString(R.string.app_id));

            RelativeLayout.LayoutParams adParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            adParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            adParams.addRule(RelativeLayout.CENTER_HORIZONTAL);

            adView = new AdView(context);
            adView.setAdSize(AdSize.BANNER);
            adView.setAdUnitId(res.getString(R.string.add_unit_id));
            adView.setBackgroundColor(Color.TRANSPARENT);
            adView.setLayoutParams(adParams);

            // Test Ads
            // AdRequest adRequest = new AdRequest.Builder().addTestDevice(res.getString(R.string.test_device_id)).build();
            AdRequest adRequest = new AdRequest.Builder().build();

            adView.loadAd(adRequest);
        }

        removeParent();
        return adView;
    }

    public void destroy(){
        if(adView != null){
            adView.destroy();
            adView = null;
        }
    }

    private void removeParent(){
        if (adView.getParent() != null) {
            ViewGroup tempVg = (ViewGroup) adView.getParent();
            tempVg.removeView(adView);
        }
    }
}
