package com.chimple.parentalcontrol.view;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chimple.parentalcontrol.adapter.AppListAdapter;
import com.chimple.parentalcontrol.databinding.ActivityAppListBinding;
import com.chimple.parentalcontrol.model.AppModel;
import com.chimple.parentalcontrol.util.AsyncTaskHelper;
import com.chimple.parentalcontrol.util.CProgressDialog;
import com.chimple.parentalcontrol.util.VUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AppListActivity extends AppCompatActivity {

    private ActivityAppListBinding binding;
    private RecyclerView recyclerView;
    private AppListAdapter adapter;
    private List<AppModel> originalAppList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAppListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        recyclerView = binding.appListRecyclerview;
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        CProgressDialog.mShow(this);

        new Handler().postDelayed(this::loadAppList, 500);

        binding.backBtn.setOnClickListener(v -> finish());
        binding.searchAppEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterAppList(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadAppList() {
        originalAppList = VUtil.loadAppList(getPackageManager());
        updateAppList(originalAppList);
    }

    private void filterAppList(String query) {
        List<AppModel> filteredList = new ArrayList<>();
        for (AppModel app : originalAppList) {
            if (app.getAppName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(app);
            }
        }
        updateAppList(filteredList);
    }

    private void updateAppList(List<AppModel> appList) {
        runOnUiThread(() -> {
            adapter = new AppListAdapter(this, appList);
            recyclerView.setAdapter(adapter);
            CProgressDialog.mDismiss();
        });
    }
}
