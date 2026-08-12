package com.example.m_hikeapp.util;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * ImgBB Image Upload Helper.
 * <p>Uploads local image URIs (file:// or content://) to ImgBB cloud storage
 * via key: 2ea4985346df75ad3ea1619d50d811f3 and returns the public web URL (https://i.ibb.co/...).</p>
 */
public class ImgBbHelper {
    private static final String TAG = "ImgBbHelper";
    public static final String API_KEY = "2ea4985346df75ad3ea1619d50d811f3";
    private static final String UPLOAD_URL = "https://api.imgbb.com/1/upload?key=" + API_KEY;
    private static final OkHttpClient client = new OkHttpClient();

    public interface UploadCallback {
        void onSuccess(String imageUrl);
        void onError(Exception e);
    }

    /**
     * Uploads an image URI to ImgBB asynchronously.
     * Callbacks are dispatched on the UI main thread.
     *
     * @param context     context for resolving ContentResolver URIs.
     * @param photoUriStr local file or content URI string.
     * @param callback    result callback.
     */
    public static void uploadImage(Context context, String photoUriStr, UploadCallback callback) {
        if (photoUriStr == null || photoUriStr.trim().isEmpty()) {
            if (callback != null) callback.onError(new IllegalArgumentException("Photo URI is empty"));
            return;
        }

        // If already a remote web URL, return directly
        if (photoUriStr.startsWith("http://") || photoUriStr.startsWith("https://")) {
            if (callback != null) callback.onSuccess(photoUriStr);
            return;
        }

        Context appContext = context.getApplicationContext();
        Handler mainHandler = new Handler(Looper.getMainLooper());

        new Thread(() -> {
            try {
                byte[] imageBytes = readBytes(appContext, photoUriStr);
                if (imageBytes == null || imageBytes.length == 0) {
                    mainHandler.post(() -> {
                        if (callback != null) callback.onError(new Exception("Failed to read image bytes from " + photoUriStr));
                    });
                    return;
                }

                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("image", "photo_" + System.currentTimeMillis() + ".jpg",
                                RequestBody.create(MediaType.parse("image/*"), imageBytes))
                        .build();

                Request request = new Request.Builder()
                        .url(UPLOAD_URL)
                        .post(requestBody)
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful() && response.body() != null) {
                        String jsonString = response.body().string();
                        JSONObject json = new JSONObject(jsonString);
                        if (json.optBoolean("success")) {
                            JSONObject data = json.getJSONObject("data");
                            String url = data.optString("url", data.optString("display_url"));
                            Log.d(TAG, "Uploaded successfully to ImgBB: " + url);
                            mainHandler.post(() -> {
                                if (callback != null) callback.onSuccess(url);
                            });
                            return;
                        }
                    }
                    String errMessage = "ImgBB API returned non-success response: HTTP " + response.code();
                    Log.w(TAG, errMessage);
                    mainHandler.post(() -> {
                        if (callback != null) callback.onError(new Exception(errMessage));
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "Error uploading image to ImgBB", e);
                mainHandler.post(() -> {
                    if (callback != null) callback.onError(e);
                });
            }
        }).start();
    }

    private static byte[] readBytes(Context context, String photoUriStr) {
        try {
            InputStream inputStream = null;
            try {
                Uri uri = Uri.parse(photoUriStr);
                inputStream = context.getContentResolver().openInputStream(uri);
            } catch (Exception ignored) {
            }

            if (inputStream == null) {
                File file = new File(photoUriStr);
                if (file.exists()) {
                    inputStream = new FileInputStream(file);
                }
            }

            if (inputStream == null) return null;

            ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }
            inputStream.close();
            return byteBuffer.toByteArray();
        } catch (Exception e) {
            Log.e(TAG, "Error reading bytes for " + photoUriStr, e);
            return null;
        }
    }
}
