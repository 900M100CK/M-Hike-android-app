package com.example.m_hikeapp.export;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import androidx.core.content.FileProvider;

import com.example.m_hikeapp.R;
import com.example.m_hikeapp.model.Hike;
import com.example.m_hikeapp.model.Observation;
import com.example.m_hikeapp.util.DurationCalculator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Generates a shareable PDF report for a single {@link Hike}.
 *
 * <h3>Architecture role</h3>
 * <p>The PDF is written on a background thread and surfaced through a typed
 * callback on the <strong>main thread</strong>, safe for UI updates.</p>
 *
 * <h3>Threading model</h3>
 * <ul>
 *   <li>Document generation is dispatched to a single-threaded
 *       {@link ExecutorService}.  Serialised execution keeps writes to the
 *       cache directory conflict-free.</li>
 *   <li>Results are posted back to the main thread via
 *       {@code Handler(mainLooper)} — <strong>no ANR risk</strong>.</li>
 * </ul>
 *
 * <h3>Output contract</h3>
 * <p>The report is written to {@code getCacheDir()/reports/&lt;hikeId&gt;.pdf}
 * and exposed through this app's {@link FileProvider}
 * (authority {@code &lt;applicationId&gt;.fileprovider}, mapped in
 * {@code res/xml/file_paths.xml} under the {@code hike_reports} cache path).
 * Consumers may pass the resulting {@link Uri} to an
 * {@link Intent#ACTION_SEND} share sheet — always include
 * {@link Intent#FLAG_GRANT_READ_URI_PERMISSION}.</p>
 *
 * <p>All user-facing strings are resolved from {@code R.string} resources so
 * the document stays localisable.  Duration values reuse the G3
 * {@link DurationCalculator} formatter; weather conditions are stored as
 * canonical strings (G4) and printed verbatim.</p>
 */
public final class PdfReportBuilder {

    // =========================================================================
    // Constants
    // =========================================================================

    /** FileProvider authority suffix, mirrored in {@code AndroidManifest.xml}. */
    private static final String AUTHORITY_SUFFIX = ".fileprovider";

    /** Directory (relative to {@code getCacheDir()}) where reports are stored. */
    private static final String REPORT_SUBDIR = "reports";

    /** PDF page size: A4 in PostScript points (1/72 inch). */
    private static final int PAGE_WIDTH = 595;
    private static final int PAGE_HEIGHT = 842;

    private static final int MARGIN = 48;
    private static final int CONTENT_WIDTH = PAGE_WIDTH - (2 * MARGIN);

    private static final int TEXT_SIZE_TITLE = 22;
    private static final int TEXT_SIZE_SECTION = 15;
    private static final int TEXT_SIZE_BODY = 12;

    /** Vertical gap added after a section header (points). */
    private static final int SECTION_SPACING = 28;

    private static final int COLOR_HEADER = Color.rgb(56, 106, 31);
    private static final int COLOR_SECTION = Color.rgb(85, 98, 76);
    private static final int COLOR_TEXT = Color.rgb(27, 31, 22);

    /** Item kinds used by the layout pass. */
    private static final int TYPE_TEXT = 0;
    private static final int TYPE_SECTION = 1;

    /** Executor used to serialise report generation. */
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    private final Context appContext;

    // =========================================================================
    // Callback interface
    // =========================================================================

    /**
     * Delivers the generated report on the main thread.
     * Exactly one method is invoked per request.
     */
    public interface PdfCallback {
        /** Called when the report was written successfully. */
        void onSuccess(File file, Uri uri);

        /** Called when report generation failed. */
        void onError(Exception e);
    }

    /** A single drawable line produced by the layout pass. */
    private static final class Item {
        final int type;
        final Paint paint;
        final String text;
        final int lineHeight;

        Item(int type, Paint paint, String text, int lineHeight) {
            this.type = type;
            this.paint = paint;
            this.text = text;
            this.lineHeight = lineHeight;
        }
    }

    // =========================================================================
    // Construction
    // =========================================================================

    /**
     * @param context any context; the application context is retained so the
     *                builder can outlive the originating Activity.
     */
    public PdfReportBuilder(Context context) {
        this.appContext = context.getApplicationContext();
    }

    // =========================================================================
    // Public API
    // =========================================================================

    /**
     * Builds a PDF report for the given hike and delivers the result to
     * {@code callback} on the main thread.
     *
     * @param hike         the hike to report on (may be {@code null}, in which
     *                     case a minimal placeholder report is produced).
     * @param observations observations belonging to the hike; may be empty.
     * @param callback     the main-thread recipient of the result.
     */
    public void buildReport(Hike hike, List<Observation> observations, PdfCallback callback) {
        EXECUTOR.execute(() -> {
            File file = null;
            try {
                File dir = new File(appContext.getCacheDir(), REPORT_SUBDIR);
                if (!dir.exists() && !dir.mkdirs()) {
                    throw new IOException("Unable to create report directory");
                }
                String fileName = (hike != null ? hike.getId() : "hike") + ".pdf";
                file = new File(dir, fileName);

                byte[] data = renderPdf(hike, observations);
                try (FileOutputStream out = new FileOutputStream(file)) {
                    out.write(data);
                }

                Uri uri = FileProvider.getUriForFile(
                        appContext, appContext.getPackageName() + AUTHORITY_SUFFIX, file);

                File resultFile = file;
                MAIN.post(() -> callback.onSuccess(resultFile, uri));
            } catch (Exception e) {
                if (file != null && file.exists()) {
                    //noinspection ResultOfMethodCallIgnored
                    file.delete();
                }
                MAIN.post(() -> callback.onError(e));
            }
        });
    }

    // =========================================================================
    // Rendering
    // =========================================================================

    /**
     * Runs the layout pass to produce a list of drawable lines, then paginates
     * those lines onto A4 pages. Returns the complete document as raw PDF bytes.
     */
    private byte[] renderPdf(Hike hike, List<Observation> observations) throws IOException {
        List<Item> items = new ArrayList<>();

        Paint titlePaint = makePaint(TEXT_SIZE_TITLE, COLOR_HEADER, Typeface.BOLD);
        Paint sectionPaint = makePaint(TEXT_SIZE_SECTION, COLOR_SECTION, Typeface.BOLD);
        Paint labelPaint = makePaint(TEXT_SIZE_BODY, COLOR_TEXT, Typeface.BOLD);
        Paint valuePaint = makePaint(TEXT_SIZE_BODY, COLOR_TEXT, Typeface.NORMAL);

        // --- Title ----------------------------------------------------------
        String title = appContext.getString(R.string.pdf_report_title);
        if (hike != null && hike.getName() != null && !hike.getName().isEmpty()) {
            title = title + " — " + hike.getName();
        }
        addWrapped(items, title, titlePaint, MARGIN);

        // --- Details section -------------------------------------------------
        addSection(items, sectionPaint, appContext.getString(R.string.pdf_section_details));
        if (hike == null) {
            addBody(items, labelPaint, valuePaint,
                    appContext.getString(R.string.error_hike_not_found));
            items.add(blankItem(SECTION_SPACING));
            return paginate(items);
        }

        addDetail(items, labelPaint, valuePaint, R.string.pdf_label_name, hike.getName());
        addDetail(items, labelPaint, valuePaint, R.string.pdf_label_location, hike.getLocation());
        addDetail(items, labelPaint, valuePaint, R.string.pdf_label_date, hike.getDate());
        addDetail(items, labelPaint, valuePaint, R.string.pdf_label_parking, parkingValue(hike));
        addDetail(items, labelPaint, valuePaint, R.string.pdf_label_length, lengthValue(hike));
        addDetail(items, labelPaint, valuePaint, R.string.pdf_label_difficulty, hike.getDifficulty());
        addDetail(items, labelPaint, valuePaint, R.string.pdf_label_estimated_duration,
                estimatedDurationValue(hike));
        addDetail(items, labelPaint, valuePaint, R.string.pdf_label_actual_duration,
                actualDurationValue(hike));
        addDetail(items, labelPaint, valuePaint, R.string.pdf_label_weather_condition,
                weatherConditionValue(hike));
        addDetail(items, labelPaint, valuePaint, R.string.pdf_label_weather_notes,
                hike.getWeatherNotes());
        addDetail(items, labelPaint, valuePaint, R.string.pdf_label_trail_rating, ratingValue(hike));
        addDetail(items, labelPaint, valuePaint, R.string.pdf_label_trail_notes,
                hike.getCustomField2());
        addDetail(items, labelPaint, valuePaint, R.string.pdf_label_description,
                hike.getDescription());

        // --- Observations section ---------------------------------------------
        addSection(items, sectionPaint, appContext.getString(R.string.pdf_section_observations));
        if (observations == null || observations.isEmpty()) {
            addBody(items, labelPaint, valuePaint,
                    appContext.getString(R.string.msg_no_observations));
        } else {
            for (Observation obs : observations) {
                String time = obs.getObsTime();
                addBody(items, labelPaint, valuePaint,
                        appContext.getString(R.string.pdf_label_time) + ": "
                                + (time == null ? "" : time));
                addDetail(items, labelPaint, valuePaint, R.string.pdf_label_comment,
                        obs.getComment());
                items.add(blankItem(12));
            }
        }

        return paginate(items);
    }

    // =========================================================================
    // Value formatters
    // =========================================================================

    /** Parking availability text: "Yes"/"No" from shared resources. */
    private String parkingValue(Hike hike) {
        return hike.isParkingAvailable()
                ? appContext.getString(R.string.label_parking_yes)
                : appContext.getString(R.string.label_parking_no);
    }

    /** Length in km, formatted with the shared "%1$.2f %2$s" pattern. */
    private String lengthValue(Hike hike) {
        if (hike.getLengthKm() <= 0) {
            return "";
        }
        return appContext.getString(R.string.pdf_length_format, hike.getLengthKm(),
                appContext.getString(R.string.suffix_km));
    }

    /** Estimated duration, reusing the G3 calculator's formatter. */
    private String estimatedDurationValue(Hike hike) {
        int minutes = hike.getEstimatedDurationMin();
        return minutes <= 0 ? "" : DurationCalculator.formatMinutes(minutes);
    }

    /** Actual duration, reusing the G3 calculator's formatter. */
    private String actualDurationValue(Hike hike) {
        int minutes = hike.getActualDurationMin();
        return minutes <= 0 ? "" : DurationCalculator.formatMinutes(minutes);
    }

    /** Canonical G4 weather condition, printed verbatim. */
    private String weatherConditionValue(Hike hike) {
        String condition = hike.getWeatherCondition();
        return condition == null ? "" : condition;
    }

    /** Trail rating: "4 / 5" via the shared format pattern. */
    private String ratingValue(Hike hike) {
        String rating = hike.getCustomField1();
        if (rating == null || rating.isEmpty()) {
            return "";
        }
        try {
            return appContext.getString(R.string.pdf_rating_format, Integer.parseInt(rating));
        } catch (NumberFormatException e) {
            return "";
        }
    }

    // =========================================================================
    // Layout helpers
    // =========================================================================

    private static Paint makePaint(int textSize, int color, int style) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(color);
        paint.setTextSize(textSize);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, style));
        return paint;
    }

    private static int lineHeight(Paint paint) {
        return (int) (paint.descent() - paint.ascent()) + 8;
    }

    /** Appends a section header line followed by extra vertical spacing. */
    private void addSection(List<Item> items, Paint paint, String text) {
        items.add(new Item(TYPE_SECTION, paint, text, lineHeight(paint) + 12));
        items.add(blankItem(SECTION_SPACING));
    }

    /** Appends a label line and, when the value is non-empty, its value line(s). */
    private void addDetail(List<Item> items, Paint labelPaint, Paint valuePaint,
                           int labelRes, String value) {
        items.add(new Item(TYPE_TEXT, labelPaint,
                appContext.getString(labelRes) + ": ", lineHeight(labelPaint)));
        if (value != null && !value.isEmpty()) {
            addWrapped(items, value, valuePaint, MARGIN);
        }
    }

    /** Appends a body paragraph, wrapping on word boundaries when needed. */
    private void addBody(List<Item> items, Paint labelPaint, Paint valuePaint, String text) {
        addWrapped(items, text, valuePaint, MARGIN);
    }

    /** Appends blank spacer with the given height. */
    private static Item blankItem(int height) {
        return new Item(TYPE_TEXT, null, "", height);
    }

    /**
     * Appends {@code text} to {@code items}, splitting it into multiple lines
     * when it exceeds {@code CONTENT_WIDTH} at the given paint size.
     */
    private static void addWrapped(List<Item> items, String text, Paint paint, float x) {
        if (text == null || text.isEmpty()) {
            return;
        }
        int height = lineHeight(paint);
        while (!text.isEmpty()) {
            int count = paint.breakText(text, true, CONTENT_WIDTH, null);
            if (count <= 0) {
                count = 1;
            }
            String line = text.substring(0, Math.min(count, text.length()));
            items.add(new Item(TYPE_TEXT, paint, line, height));
            text = text.substring(line.length());
        }
    }

    // =========================================================================
    // Pagination
    // =========================================================================

    /** Draws all laid-out items across A4 pages and returns the PDF bytes. */
    private byte[] paginate(List<Item> items) throws IOException {
        PdfDocument document = new PdfDocument();
        try {
            int pageNumber = 1;
            PdfDocument.Page page = document.startPage(newPage(pageNumber));
            Canvas canvas = page.getCanvas();
            Paint rulePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            rulePaint.setColor(COLOR_SECTION);
            rulePaint.setStrokeWidth(2f);

            int y = MARGIN;
            for (Item item : items) {
                if (y + item.lineHeight > PAGE_HEIGHT - MARGIN) {
                    document.finishPage(page);
                    page = document.startPage(newPage(++pageNumber));
                    canvas = page.getCanvas();
                    y = MARGIN;
                }
                if (item.type == TYPE_SECTION) {
                    canvas.drawText(item.text, MARGIN, y, item.paint);
                    canvas.drawLine(MARGIN, y + 6, PAGE_WIDTH - MARGIN, y + 6, rulePaint);
                } else if (item.paint != null) {
                    canvas.drawText(item.text, MARGIN, y, item.paint);
                }
                y += item.lineHeight;
            }
            document.finishPage(page);

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            document.writeTo(buffer);
            return buffer.toByteArray();
        } finally {
            document.close();
        }
    }

    private static PdfDocument.PageInfo newPage(int number) {
        return new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, number).create();
    }
}
