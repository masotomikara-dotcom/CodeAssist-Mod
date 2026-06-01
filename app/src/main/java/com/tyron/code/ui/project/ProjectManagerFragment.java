
package com.tyron.code.ui.project;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewKt;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.TransitionManager;

import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.transition.MaterialFade;
import com.google.android.material.transition.MaterialFadeThrough;
import com.google.android.material.transition.MaterialSharedAxis;
import com.tyron.code.Application;
import com.tyron.code.R;
import com.tyron.code.ui.main.MainActivity;
import com.tyron.code.ui.project.adapter.ProjectAdapter;
import com.tyron.code.util.AndroidUtilities;
import com.tyron.code.util.ProjectUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ProjectManagerFragment extends Fragment {

    public static final String TAG = "ProjectManagerFragment";

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    loadProjects();
                } else {
                    ExtendedFloatingActionButton actionButton = requireView().findViewById(R.id.create_project_button);
                    actionButton.setEnabled(false);
                }
            });

    private final ActivityResultLauncher<String[]> requestPermissionsLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                boolean allGranted = true;
                for (Boolean isGranted : result.values()) {
                    if (!isGranted) {
                        allGranted = false;
                        break;
                    }
                }
                if (allGranted) {
                    loadProjects();
                } else {
                    ExtendedFloatingActionButton actionButton = requireView().findViewById(R.id.create_project_button);
                    actionButton.setEnabled(false);
                }
            });

    private ProjectAdapter adapter;
    private SharedPreferences sharedPreferences;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setEnterTransition(new MaterialFadeThrough());
        setExitTransition(new MaterialFadeThrough());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.project_manager_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());

        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);

        RecyclerView recyclerView = view.findViewById(R.id.projects_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ProjectAdapter();
        adapter.setOnItemClickListener(new ProjectAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(File file) {
                try {
                    Object project = ProjectUtils.getProjectFromDirectory(file);
                    if (project != null) {
                        openProject(project);
                    }
                } catch (Exception ignored) {
                }
            }

            @Override
            public void onItemLongClick(File file) {
                showMenuDialog(file);
            }
        });
        recyclerView.setAdapter(adapter);

        ExtendedFloatingActionButton actionButton = view.findViewById(R.id.create_project_button);
        actionButton.setOnClickListener(v -> {
            WizardFragment wizardFragment = new WizardFragment();
            wizardFragment.setOnProjectCreatedListener(project -> {
                try {
                    java.lang.reflect.Method openProjectMethod = ProjectManagerFragment.this.getClass().getDeclaredMethod("openProject", project.getClass());
                    openProjectMethod.setAccessible(true);
                    openProjectMethod.invoke(ProjectManagerFragment.this, project);
                } catch (Exception e) {
                    try {
                        for (java.lang.reflect.Method method : ProjectManagerFragment.this.getClass().getDeclaredMethods()) {
                            if (method.getName().equals("openProject") && method.getParameterCount() == 1) {
                                method.setAccessible(true);
                                method.invoke(ProjectManagerFragment.this, project);
                                break;
                            }
                        }
                    } catch (Exception ignored) {
                    }
                }
            });
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, wizardFragment)
                    .addToBackStack(null)
                    .commit();
        });

        NestedScrollView scrollView = view.findViewById(R.id.scroll_view);
        scrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if (scrollY > oldScrollY + 12) {
                actionButton.shrink();
            }
            if (scrollY < oldScrollY - 12) {
                actionButton.extend();
            }
            if (scrollY == 0) {
                actionButton.extend();
            }
        });

        checkPermissions();
    }

    private void openProject(Object project) {
        try {
            java.lang.reflect.Method getIdMethod = project.getClass().getMethod("getId");
            getIdMethod.setAccessible(true);
            String projectId = (String) getIdMethod.invoke(project);
            sharedPreferences.edit().putString("last_project_id", projectId).apply();
        } catch (Exception ignored) {
        }

        Intent intent = new Intent(requireContext(), MainActivity.class);
        startActivity(intent);
        requireActivity().finish();
    }

    private void showMenuDialog(File file) {
        String[] items = new String[]{
                getString(R.string.delete)
        };
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(file.getName())
                .setItems(items, (dialog, which) -> {
                    if (which == 0) {
                        showDeleteDialog(file);
                    }
                })
                .show();
    }

    private void showDeleteDialog(File file) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.delete_project_title)
                .setMessage(getString(R.string.delete_project_message, file.getName()))
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    try {
                        AndroidUtilities.deleteFile(file);
                        loadProjects();
                    } catch (IOException e) {
                        new MaterialAlertDialogBuilder(requireContext())
                                .setTitle(R.string.error)
                                .setMessage(e.getMessage())
                                .setPositiveButton(android.R.string.ok, null)
                                .show();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                loadProjects();
            } else {
                showPermissionDialog();
            }
        } else {
            String[] permissions = new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            };
            boolean allGranted = true;
            for (String permission : permissions) {
                if (ContextCompat.checkSelfPermission(requireContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted) {
                loadProjects();
            } else {
                requestPermissionsLauncher.launch(permissions);
            }
        }
    }

    private void showPermissionDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.permission_dialog_title)
                .setMessage(R.string.permission_dialog_message)
                .setPositiveButton(R.string.grant, (dialog, which) -> {
                    Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                    startActivity(intent);
                })
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> requireActivity().finish())
                .setCancelable(false)
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                loadProjects();
            }
        }
    }

    private void loadProjects() {
        toggleLoading(true);
        Application.getProjectManager().execute(() -> {
            File root = new File(Environment.getExternalStorageDirectory(), "CodeAssist");
            if (!root.exists()) {
                if (!root.mkdirs()) {
                    toggleLoading(false);
                    return;
                }
            }

            File[] files = root.listFiles();
            if (files == null) {
                toggleLoading(false);
                return;
            }

            List<File> projectFiles = new ArrayList<>();
            for (File file : files) {
                if (file.isDirectory()) {
                    File config = new File(file, ".codeassist");
                    if (config.exists()) {
                        projectFiles.add(file);
                    }
                }
            }

            Collections.sort(projectFiles, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));

            if (getActivity() == null) {
                return;
            }
            requireActivity().runOnUiThread(() -> {
                adapter.setFiles(projectFiles);
                toggleLoading(false);
                toggleEmptyView(projectFiles);
            });
        });
    }

    private void toggleEmptyView(List<File> projects) {
        if (getActivity() == null || isDetached()) {
            return;
        }
        View view = getView();
        if (view == null) {
            return;
        }
        View recycler = view.findViewById(R.id.projects_recycler);
        View empty = view.findViewById(R.id.empty_projects);

        if (recycler != null && empty != null) {
            TransitionManager.beginDelayedTransition((ViewGroup) recycler.getParent(), new MaterialFade());
            if (projects.size() == 0) {
                recycler.setVisibility(View.GONE);
                empty.setVisibility(View.VISIBLE);
            } else {
                recycler.setVisibility(View.VISIBLE);
                empty.setVisibility(View.GONE);
            }
        }
    }

    private void toggleLoading(boolean show) {
        if (getActivity() == null || isDetached()) {
            return;
        }
        View view = getView();
        if (view == null) {
            return;
        }
        View recycler = view.findViewById(R.id.projects_recycler);
        View empty = view.findViewById(R.id.empty_container);
        View empty_project = view.findViewById(R.id.empty_projects);

        if (empty_project != null) {
            empty_project.setVisibility(View.GONE);
        }

        if (recycler != null && empty != null) {
            TransitionManager.beginDelayedTransition((ViewGroup) recycler.getParent(), new MaterialFade());
            if (show) {
                recycler.setVisibility(View.GONE);
                empty.setVisibility(View.VISIBLE);
            } else {
                recycler.setVisibility(View.VISIBLE);
                empty.setVisibility(View.GONE);
            }
        }
    }
}
