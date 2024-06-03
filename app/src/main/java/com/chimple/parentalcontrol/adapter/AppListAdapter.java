package com.chimple.parentalcontrol.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.chimple.parentalcontrol.R;
import com.chimple.parentalcontrol.model.AppModel;
import com.chimple.parentalcontrol.util.AsyncTaskHelper;
import com.chimple.parentalcontrol.util.LocalPreference;

import java.util.List;

public class AppListAdapter extends RecyclerView.Adapter<AppListAdapter.AppViewHolder> {

    private final Context context;
    private final List<AppModel> appList;

    public AppListAdapter(Context context, List<AppModel> appList) {
        this.context = context;
        this.appList = appList;
    }

    @NonNull
    @Override
    public AppViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cl_app_list, parent, false);
        return new AppViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppViewHolder holder, int position) {
        AppModel appInfo = appList.get(position);
        holder.appSwitch.setText(appInfo.getAppName());
        holder.appIconImageView.setImageDrawable(appInfo.getAppIcon());

        holder.appSwitch.setOnCheckedChangeListener(null);
        holder.appSwitch.setChecked(LocalPreference.isAppApproved(context, appInfo.getPackageName()));

        holder.appSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            AsyncTaskHelper.runInBackground(new Runnable() {
                @Override
                public void run() {
                    if (isChecked) {
                        LocalPreference.addAppToApprovedList(context, appInfo.getPackageName());
                    } else {
                        LocalPreference.removeAppFromApprovedList(context, appInfo.getPackageName());
                    }
                }
            });

        });
    }

    @Override
    public int getItemCount() {
        return appList.size();
    }

    public static class AppViewHolder extends RecyclerView.ViewHolder {
        SwitchCompat appSwitch;
        ImageView appIconImageView;

        public AppViewHolder(@NonNull View itemView) {
            super(itemView);
            appSwitch = itemView.findViewById(R.id.appSwitch);
            appIconImageView = itemView.findViewById(R.id.appDrawable);
        }
    }
}
