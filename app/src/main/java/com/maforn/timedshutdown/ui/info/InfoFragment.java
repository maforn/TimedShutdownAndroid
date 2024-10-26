package com.maforn.timedshutdown.ui.info;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.maforn.timedshutdown.FullscreenActivity;
import com.maforn.timedshutdown.R;
import com.maforn.timedshutdown.ShutdownWidgetProvider;
import com.maforn.timedshutdown.databinding.FragmentInfoBinding;

public class InfoFragment extends Fragment {

    private FragmentInfoBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        // bind the fragment to the main navigation activity
        binding = FragmentInfoBinding.inflate(inflater, container, false);

        // set up links in textView
        TextView contribute = binding.contributeTextView;
        contribute.setMovementMethod(LinkMovementMethod.getInstance());

        // set up the tutorial video link
        ImageView thumbnailImageView = binding.thumbnailImageView;
        String videoPath = "android.resource://" + requireContext().getPackageName() + "/" + R.raw.screen_record;

        thumbnailImageView.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), FullscreenActivity.class);
            intent.putExtra("videoPath", videoPath);
            startActivity(intent);
        });

        binding.addWidget.setOnClickListener(v -> {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(requireContext());
            if (appWidgetManager.isRequestPinAppWidgetSupported()) {
                Intent pinnedWidgetCallbackIntent = new Intent(requireContext(), ShutdownWidgetProvider.class);
                ComponentName shutdownWidgetProvider = new ComponentName(requireContext(), ShutdownWidgetProvider.class);
                PendingIntent successCallback = PendingIntent.getBroadcast(requireContext(), 0,
                        pinnedWidgetCallbackIntent, PendingIntent.FLAG_MUTABLE);
                appWidgetManager.requestPinAppWidget(shutdownWidgetProvider, null, successCallback);
            }
        });

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}