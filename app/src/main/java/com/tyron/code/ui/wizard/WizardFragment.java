package com.tyron.code.ui.wizard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.tyron.code.ui.wizard.adapter.WizardTemplateAdapter;
import java.util.ArrayList;
import java.util.List;

public class WizardFragment extends Fragment {

    private WizardTemplateAdapter mAdapter;

    public interface OnProjectCreatedListener {
        void onProjectCreated();
    }

    public void setOnProjectCreatedListener(OnProjectCreatedListener listener) {
    }
    public void setOnProjectCreatedListener(Object listener) {
    }

    private void setupTemplates() {
        final List<WizardTemplate> templates = new ArrayList<>();
        WizardTemplate importTemp = new WizardTemplate();
        importTemp.setName("import_project");
        templates.add(importTemp);

        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mAdapter != null) {
                        mAdapter.submitList(templates);
                    }
                }
            });
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setupTemplates();
        return new View(getContext());
    }
}
