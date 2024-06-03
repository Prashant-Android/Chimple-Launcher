package com.chimple.parentalcontrol.services;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.chimple.parentalcontrol.util.LocalPreference;

import java.util.List;

public class NotificationBlockAccessibilityService extends AccessibilityService {

    private static final String TAG = "NotificationBlockService";
    private static final long COLLAPSE_DELAY_MS = 50; // 50 milliseconds

    private Handler handler;

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            List<CharSequence> eventText = event.getText();
            for (CharSequence text : eventText) {
                if (text != null && (text.toString().contains("Notification shade") || text.toString().contains("Notification panel"))) {
                    if (LocalPreference.getChildModeStatus().equals("on")) {
                        collapseNotificationBar();
                        handler.postDelayed(this::collapseNotificationBar, COLLAPSE_DELAY_MS);
                    }
                }


                AccessibilityNodeInfo rootNode = getRootInActiveWindow();
                if (rootNode != null) {
                    if (containsTextInHierarchy(rootNode, "Wi-Fi")) {
                        collapseNotificationBar();
                    }
                    rootNode.recycle();
                }
            }
        }
    }

    @Override
    public void onInterrupt() {
        // Handle interruption
    }

    private void collapseNotificationBar() {
        try {

            performGlobalAction(GESTURE_SWIPE_UP);

        } catch (Exception e) {
            Log.e(TAG, "Error collapsing status bar", e);
        }

    }
    private boolean containsTextInHierarchy(AccessibilityNodeInfo node, String text) {
        if (node == null) {
            return false;
        }
        if (node.getText() != null && node.getText().toString().contains(text)) {
            return true;
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            if (containsTextInHierarchy(node.getChild(i), text)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED | AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_ALL_MASK;
        info.notificationTimeout = 50;
        setServiceInfo(info);
    }
}
