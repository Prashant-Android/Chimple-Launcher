package com.chimple.parentalcontrol.util;

import android.os.Handler;
import android.os.Looper;
import java.util.concurrent.Callable;

public class AsyncTaskHelper {

    public interface AsyncTaskCallback<T> {
        void onPostExecute(T result);

        default void onPreExecute() {
            // Default implementation for onPreExecute method
        }

        default void onError(Exception e) {
            // Default implementation for onError method
        }
    }

    private static final Handler handler = new Handler(Looper.getMainLooper());

    public static void runInBackground(Runnable runnable) {
        executeAsyncTask(() -> {
            runnable.run();
            return null;
        }, null);
    }

    public static void runOnUiThread(Runnable runnable) {
        handler.post(runnable);
    }

    private static <T> void executeAsyncTask(Callable<T> callable, AsyncTaskCallback<T> callback) {
        if (callback != null) {
            callback.onPreExecute();
        }

        try {
            T result = callable.call();
            if (callback != null) {
                runOnUiThread(() -> callback.onPostExecute(result));
            }
        } catch (Exception e) {
            if (callback != null) {
                runOnUiThread(() -> callback.onError(e));
            }
        }
    }
}
