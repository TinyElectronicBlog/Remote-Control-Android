package com.tinyelectronicblog.remotecontrol.ui.remotecontrol.on_off;

/**
 * MIT License
 * Copyright (c) 2022 TinyElectronicBlog
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.tinyelectronicblog.remotecontrol.databinding.FragmentOnOffBinding;

public class OnOffFragment extends Fragment {

    FragmentOnOffBinding binding;
    CheckBox checkBox;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentOnOffBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        checkBox = binding.CheckBox;
        checkBox.setVisibility(View.INVISIBLE);
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        OnOffAlgorithm.onResume(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        OnOffAlgorithm.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        checkBox = null;
        binding = null;
    }

}