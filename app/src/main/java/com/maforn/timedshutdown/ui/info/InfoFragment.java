package com.maforn.timedshutdown.ui.info;

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
import com.maforn.timedshutdown.databinding.FragmentInfoBinding;

import java.util.Objects;

public class InfoFragment extends Fragment {

    private FragmentInfoBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentInfoBinding.inflate(inflater, container, false);

        TextView contribute = binding.contributeTextView;
        contribute.setMovementMethod(LinkMovementMethod.getInstance());

        ImageView thumbnailImageView = binding.thumbnailImageView;

        String videoPath = "android.resource://" + Objects.requireNonNull(getContext()).getPackageName() + "/" + R.raw.screen_record;

        thumbnailImageView.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), FullscreenActivity.class);
            intent.putExtra("videoPath", videoPath);
            startActivity(intent);
        });

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}