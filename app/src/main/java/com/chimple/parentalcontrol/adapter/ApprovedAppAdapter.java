package com.chimple.parentalcontrol.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.chimple.parentalcontrol.R;
import com.chimple.parentalcontrol.model.AppModel;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class ApprovedAppAdapter extends RecyclerView.Adapter<ApprovedAppAdapter.AppViewHolder> {

    private final Context context;
    private final List<AppModel> approvedAppsList;

    public ApprovedAppAdapter(Context context, List<AppModel> approvedAppsList) {
        this.context = context;
        this.approvedAppsList = approvedAppsList;
    }

    @NonNull
    @Override
    public AppViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cl_item_approved_app, parent, false);
        return new AppViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppViewHolder holder, int position) {
        AppModel appInfo = approvedAppsList.get(position);
        holder.appNameTextView.setText(appInfo.getAppName());
        holder.appIconImageView.setImageDrawable(appInfo.getAppIcon());

        holder.openApp.setOnClickListener(v -> {
            // Get the package name of the app
            String packageName = appInfo.getPackageName();
            // Create an intent to launch the app
            Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
            if (launchIntent != null) {
                // Launch the app
                context.startActivity(launchIntent);
            } else {
                // If launchIntent is null, the app cannot be launched
                Toast.makeText(context, "Cannot open app", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return approvedAppsList.size();
    }

    public static class AppViewHolder extends RecyclerView.ViewHolder {
        TextView appNameTextView;
        ImageView appIconImageView;
        MaterialCardView openApp;

        public AppViewHolder(@NonNull View itemView) {
            super(itemView);
            appNameTextView = itemView.findViewById(R.id.appName);
            appIconImageView = itemView.findViewById(R.id.appIcon);
            openApp = itemView.findViewById(R.id.openBtn);
        }
    }
}
