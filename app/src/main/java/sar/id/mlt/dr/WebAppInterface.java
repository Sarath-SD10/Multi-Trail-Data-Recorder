package sar.id.mlt.dr;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.webkit.JavascriptInterface;
import android.widget.Toast;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.IOException;

public class WebAppInterface {
    private final Context mContext;

    WebAppInterface(Context c) {
        mContext = c;
    }

    @JavascriptInterface
    public void saveFile(String base64Data, String fileName, String mimeType, String trialFolderName) {
        try {
            byte[] fileBytes = Base64.decode(base64Data, Base64.DEFAULT);
            boolean isImage = mimeType != null && mimeType.startsWith("image/");

            String primaryDir = isImage ? Environment.DIRECTORY_PICTURES : Environment.DIRECTORY_DOWNLOADS;
            String relativePath = primaryDir;

            if (isImage && trialFolderName != null && !trialFolderName.trim().isEmpty()) {
                relativePath = primaryDir + File.separator + trialFolderName.trim();
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                saveWithMediaStore(fileBytes, fileName, mimeType, relativePath, isImage);
            } else {
                saveLegacyWithMediaStore(fileBytes, fileName, mimeType, relativePath, isImage);
            }
        } catch (Exception e) {
            Log.e("WebAppInterface", "Failed to save file", e);
            showToast("Save failed: " + e.getMessage());
        }
    }

    /**
     * Scoped storage save for Android 10+ using MediaStore.
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void saveWithMediaStore(byte[] fileBytes, String fileName, String mimeType, String relativePath, boolean isImage) throws Exception {
        ContentResolver resolver = mContext.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
        values.put(MediaStore.MediaColumns.MIME_TYPE, mimeType);
        values.put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath);

        Uri collection = isImage
                ? MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                : MediaStore.Downloads.EXTERNAL_CONTENT_URI; // Safe because this branch is only API 29+

        Uri fileUri = resolver.insert(collection, values);
        if (fileUri == null) throw new IOException("Failed to create MediaStore entry.");

        try (OutputStream out = resolver.openOutputStream(fileUri)) {
            if (out == null) throw new IOException("OutputStream is null");
            out.write(fileBytes);
        }

        showToast("File saved to " + relativePath);
    }

    /**
     * MediaStore save for Android 9 and below (minSdkVersion 24).
     * Uses MediaStore.Files for compatibility.
     */
    private void saveLegacyWithMediaStore(byte[] fileBytes, String fileName, String mimeType, String relativePath, boolean isImage) throws Exception {
        ContentResolver resolver = mContext.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
        values.put(MediaStore.MediaColumns.MIME_TYPE, mimeType);

        // Absolute path for legacy
        String fullPath = Environment.getExternalStoragePublicDirectory(relativePath).getAbsolutePath() + File.separator + fileName;
        values.put(MediaStore.MediaColumns.DATA, fullPath);

        Uri collection = MediaStore.Files.getContentUri("external");
        Uri fileUri = resolver.insert(collection, values);
        if (fileUri == null) throw new IOException("Failed to insert into MediaStore.");

        try (OutputStream out = resolver.openOutputStream(fileUri)) {
            if (out == null) throw new IOException("OutputStream is null");
            out.write(fileBytes);
        }

        showToast("File saved to " + fullPath);
    }

    /**
     * UI Thread toast helper
     */
    private void showToast(final String message) {
        new android.os.Handler(mContext.getMainLooper()).post(
                () -> Toast.makeText(mContext, message, Toast.LENGTH_LONG).show()
        );
    }
}
