package com.app.wprestapi.fragments;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.app.wprestapi.R;
import com.app.wprestapi.utils.NativeTemplateStyle;
import com.app.wprestapi.utils.TemplateView;
import com.app.wprestapi.utils.Tools;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.nativead.MediaView;

public class FragmentSampleNative extends Fragment {

    View root_view;
    private TemplateView native_template;
    private MediaView mediaView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root_view = inflater.inflate(R.layout.fragment_sample_native, container, false);
        initView();
        loadAdMobNativeAd();
        return root_view;
    }

    private void initView() {
        native_template = root_view.findViewById(R.id.native_admob_container);
        mediaView = root_view.findViewById(R.id.media_view);
    }

    public void loadAdMobNativeAd() {
            AdLoader adLoader = new AdLoader.Builder(getActivity(), "ca-app-pub-3940256099942544/2247696110")
                    .forNativeAd(NativeAd -> {
                        ColorDrawable colorDrawable = new ColorDrawable(ContextCompat.getColor(getActivity(), R.color.colorWhite));
                        NativeTemplateStyle styles = new NativeTemplateStyle.Builder().withMainBackgroundColor(colorDrawable).build();
                        native_template.setStyles(styles);
                        mediaView.setImageScaleType(ImageView.ScaleType.CENTER_CROP);
                        native_template.setNativeAd(NativeAd);
                        native_template.setVisibility(View.VISIBLE);
                    })
                    .withAdListener(new AdListener() {
                        @Override
                        public void onAdFailedToLoad(LoadAdError adError) {
                            native_template.setVisibility(View.GONE);
                        }
                    })
                    .build();

            adLoader.loadAd(Tools.getAdRequest());
    }

}
