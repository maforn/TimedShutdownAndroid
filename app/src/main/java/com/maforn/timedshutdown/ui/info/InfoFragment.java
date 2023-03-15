package com.maforn.timedshutdown.ui.info;

import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.maforn.timedshutdown.databinding.FragmentInfoBinding;

public class InfoFragment extends Fragment {

    private FragmentInfoBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentInfoBinding.inflate(inflater, container, false);

        TextView contribute = (TextView) binding.contributeTextView;
        contribute.setMovementMethod(LinkMovementMethod.getInstance());

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}