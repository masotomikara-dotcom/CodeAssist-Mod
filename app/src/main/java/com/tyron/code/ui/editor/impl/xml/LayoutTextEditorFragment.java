package com.tyron.code.ui.editor.impl.xml;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.io.File;

public class LayoutTextEditorFragment extends Fragment {

    public static LayoutTextEditorFragment newInstance(File file) {
        LayoutTextEditorFragment fragment = new LayoutTextEditorFragment();
        Bundle args = new Bundle();
        if (file != null) {
            args.putString("path", file.absolutePath);
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return new View(getContext());
    }
}
