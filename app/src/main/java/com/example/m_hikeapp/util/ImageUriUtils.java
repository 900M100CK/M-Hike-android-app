package com.example.m_hikeapp.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * File/URI helpers for the G2 photo capture &amp; storage feature.
 *
 * <p>A hike photo is captured with {@code MediaStore.ACTION_IMAGE_CAPTURE} and
 * written at full resolution to an app-specific file inside
 * {@code getExternalFilesDir(Pictures)} — no storage permission is required for
 * that directory.  The {@code content://} URI exposed by the app's
 * {@link FileProvider} (authority {@code <applicationId>.fileprovider}, mapped
 * in {@code res/xml/file_paths.xml} under {@code hike_photos}) is the value that
 * survives in the {@code hikes.photo_uri} column, because it is stable across
 * permission-scoped reads and process restarts.</p>
 *
 * <h3>Design</h3>
 * <ul>
 *   <li>Static methods only — matches the {@code util} package convention.</li>
 *   <li>Null-aware: a hike without a photo must never crash the UI, and photo
 *       cleanup is strictly best-effort (swallows missing files).</li>
 * </ul>
 */
public final class ImageUriUtils {

    /** App-specific sub-directory (inside external files) where photos live. */
    public static final String PHOTO_DIRECTORY = "Pictures";

    /** File name prefix for captured photos. */
    private static final String FILE_PREFIX = "hike_photo_";

    /** File extension used for captured photos. */
    private static final String FILE_EXTENSION = ".jpg";

    /** Suffix appended to the application id to build the FileProvider authority. */
    private static final String AUTHORITY_SUFFIX = ".fileprovider";

    // =========================================================================
    // Private constructor — utility class
    // =========================================================================

    private ImageUriUtils() {
        throw new AssertionError("No ImageUriUtils instances");
    }

    // =========================================================================
    // Creation helpers
    // =========================================================================

    /**
     * Creates a unique, empty image file inside the app-specific Pictures
     * directory.
     *
     * <p>Call this immediately before launching {@code ACTION_IMAGE_CAPTURE} and
     * hand the returned file's {@link #toContentUri(Context, File)} to
     * {@code MediaStore.EXTRA_OUTPUT} so the full-resolution image is saved
     * rather than just a thumbnail.</p>
     *
     * @param context any context.
     * @return the created (empty) {@code .jpg} file.
     * @throws IOException if the Pictures directory cannot be created or the
     *                     file cannot be created.
     */
    public static File createPhotoFile(Context context) throws IOException {
        File dir = context.getExternalFilesDir(PHOTO_DIRECTORY);
        if (dir == null) {
            throw new IOException("getExternalFilesDir(Pictures) returned null");
        }
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Could not create photo directory: " + dir);
        }
        File photo = new File(dir, FILE_PREFIX + System.currentTimeMillis() + FILE_EXTENSION);
        if (!photo.createNewFile()) {
            throw new IOException("Could not create photo file: " + photo);
        }
        return photo;
    }

    // =========================================================================
    // URI conversion helpers
    // =========================================================================

    /**
     * Returns the {@code content://} URI for {@code file} through the app's
     * {@link FileProvider}.
     *
     * @param context any context.
     * @param file    the photo file inside {@link #PHOTO_DIRECTORY}.
     * @return a stable, grantable {@code content://} URI.
     */
    public static Uri toContentUri(Context context, File file) {
        return FileProvider.getUriForFile(context, getAuthority(context), file);
    }

    /**
     * Convenience overload of {@link #toContentUri(Context, File)} that accepts
     * the raw absolute file path.
     *
     * @param context  any context.
     * @param filePath absolute path of the photo file.
     * @return a stable, grantable {@code content://} URI.
     */
    public static Uri toContentUri(Context context, String filePath) {
        return toContentUri(context, new File(filePath));
    }

    /**
     * Best-effort resolution of a stored photo value back to a {@link File}.
     *
     * <p>Used for cleanup: when a hike is deleted its image file is removed.
     * Returns {@code null} when the value is {@code null}, is not a
     * {@code content://} URI owned by this app's provider, or cannot be
     * decoded — callers must treat {@code null} as "nothing to delete".</p>
     *
     * @param context   any context.
     * @param photoUri  the stored {@code photo_uri} value, may be {@code null}.
     * @return the underlying file, or {@code null} if not resolvable.
     */
    @Nullable
    public static File toFile(Context context, @Nullable String photoUri) {
        if (photoUri == null || photoUri.isEmpty()) {
            return null;
        }
        try {
            Uri uri = Uri.parse(photoUri);
            if (uri.getAuthority() == null
                    || !uri.getAuthority().equals(getAuthority(context))) {
                return null;
            }
            String path = uri.getPath();
            if (path == null) {
                return null;
            }
            File file = new File(path);
            return file.exists() ? file : null;
        } catch (RuntimeException e) {
            return null;
        }
    }

    /**
     * Best-effort deletion of a hike's stored photo file.
     *
     * <p>Missing files, unresolvable URIs and {@code null} values are silently
     * ignored; only a genuine I/O failure on an existing file is swallowed too,
     * so deletion never blocks or crashes the repository callers.</p>
     *
     * @param context  any context.
     * @param photoUri the stored {@code photo_uri} value, may be {@code null}.
     */
    public static void deletePhoto(Context context, @Nullable String photoUri) {
        File file = toFile(context, photoUri);
        if (file != null) {
            //noinspection ResultOfMethodCallIgnored
            file.delete();
        }
    }

    // =========================================================================
    // Intent helpers
    // =========================================================================

    /**
     * Returns {@code true} iff at least one activity can handle a standard
     * {@code ACTION_IMAGE_CAPTURE} intent.
     *
     * <p>Guards the capture button: when no camera app is installed the button
     * must be disabled rather than launching an intent that cannot be resolved.
     * {@code MATCH_ALL} is used so the check keeps working on API 30+.</p>
     *
     * @param context any context.
     */
    public static boolean hasCameraApp(Context context) {
        Intent capture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        List<ResolveInfo> matches = context.getPackageManager()
                .queryIntentActivities(capture, PackageManager.MATCH_ALL);
        return matches != null && !matches.isEmpty();
    }

    /**
     * Adds the read-permission flags required to share an exported file.
     *
     * <p>Call before {@code startActivity} for a {@code ACTION_SEND} intent that
     * carries a {@code content://} {@code EXTRA_STREAM}.</p>
     *
     * @param context any context.
     * @param uri     the {@code content://} URI being shared.
     * @param intent  the outgoing intent to flag.
     */
    public static void grantReadPermission(Context context, Uri uri, Intent intent) {
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setClipData(android.content.ClipData.newRawUri("share", uri));
    }

    // =========================================================================
    // FileProvider authority
    // =========================================================================

    /**
     * Resolves this app's FileProvider authority from the application id,
     * e.g. {@code com.example.m_hikeapp.fileprovider}. Kept in sync with the
     * manifest's {@code android:authorities="${applicationId}.fileprovider"}.
     *
     * @param context any context.
     */
    public static String getAuthority(Context context) {
        return context.getPackageName() + AUTHORITY_SUFFIX;
    }

    /**
     * Loads a local URI or remote ImgBB URL into an ImageView seamlessly using Glide.
     *
     * @param context     any context.
     * @param imageView   target ImageView.
     * @param photoUriStr local file/content URI or remote HTTP(S) URL.
     */
    public static void loadImage(Context context, android.widget.ImageView imageView, @Nullable String photoUriStr) {
        if (context == null || imageView == null) return;
        if (photoUriStr == null || photoUriStr.trim().isEmpty()) {
            imageView.setImageDrawable(null);
            imageView.setVisibility(android.view.View.GONE);
            return;
        }
        imageView.setVisibility(android.view.View.VISIBLE);
        try {
            com.bumptech.glide.Glide.with(context)
                    .load(photoUriStr)
                    .into(imageView);
        } catch (Exception e) {
            try {
                imageView.setImageURI(Uri.parse(photoUriStr));
            } catch (Exception ignored) {
            }
        }
    }
}

