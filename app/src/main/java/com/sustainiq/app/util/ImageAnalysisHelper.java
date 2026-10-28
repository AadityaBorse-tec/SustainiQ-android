package com.sustainiq.app.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import androidx.annotation.NonNull;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * On-device image analysis helper for SUSTAINIQ civic reporting.
 * Uses ML Kit Image Labeling to detect visible scene features and suggest
 * likely issue categories and editable descriptions without claiming definitive
 * legal or environmental violation status.
 */
public class ImageAnalysisHelper {

    public static class AnalysisResult {
        public final String category;               // "WASTE", "WATER", "AIR", "GREEN_SPACES", "OTHER"
        public final String suggestedTitle;
        public final String suggestedDescription;
        public final List<String> detectedLabels;    // E.g. "Plastic (88%)"
        public final float confidence;
        public final String disclaimer;

        public AnalysisResult(String category,
                              String suggestedTitle,
                              String suggestedDescription,
                              List<String> detectedLabels,
                              float confidence) {
            this.category = category;
            this.suggestedTitle = suggestedTitle;
            this.suggestedDescription = suggestedDescription;
            this.detectedLabels = detectedLabels;
            this.confidence = confidence;
            this.disclaimer = "Suggested by on-device visual analysis. Please review and confirm before submitting.";
        }
    }

    public interface Callback {
        void onSuccess(@NonNull AnalysisResult result);
        void onError(@NonNull String errorMessage);
    }

    // Category Keyword Mappings
    private static final String[] WASTE_KEYWORDS = {
            "waste", "trash", "garbage", "litter", "plastic", "bottle", "debris",
            "dump", "landfill", "scrap", "bag", "rubble", "packaging", "cardboard",
            "waste container", "bin", "can", "junk", "refuse"
    };

    private static final String[] WATER_KEYWORDS = {
            "water", "drain", "drainage", "pipe", "puddle", "stream", "river",
            "leak", "sewage", "plumbing", "flood", "canal", "gutter", "fluid",
            "liquid", "fountain", "wet", "water resource", "waterway"
    };

    private static final String[] AIR_KEYWORDS = {
            "smoke", "smog", "haze", "pollution", "chimney", "exhaust", "factory",
            "emission", "dust", "fog", "cloud", "steam", "vapor", "fire"
    };

    private static final String[] GREEN_KEYWORDS = {
            "tree", "plant", "grass", "flower", "forest", "wood", "trunk",
            "garden", "vegetation", "park", "leaf", "foliage", "shrub", "nature",
            "branch", "flora", "botany", "lawn"
    };

    public static void analyzeUri(@NonNull Context context, @NonNull Uri imageUri, @NonNull Callback callback) {
        try {
            InputImage image = InputImage.fromFilePath(context, imageUri);
            processImage(image, callback);
        } catch (IOException e) {
            callback.onError("Failed to read image for analysis: " + e.getLocalizedMessage());
        } catch (Exception e) {
            callback.onError("Image analysis error: " + e.getLocalizedMessage());
        }
    }

    public static void analyzeBitmap(@NonNull Bitmap bitmap, @NonNull Callback callback) {
        try {
            InputImage image = InputImage.fromBitmap(bitmap, 0);
            processImage(image, callback);
        } catch (Exception e) {
            callback.onError("Image analysis error: " + e.getLocalizedMessage());
        }
    }

    private static void processImage(@NonNull InputImage image, @NonNull Callback callback) {
        ImageLabelerOptions options =
                new ImageLabelerOptions.Builder()
                        .setConfidenceThreshold(0.40f)
                        .build();

        ImageLabeler labeler = ImageLabeling.getClient(options);

        labeler.process(image)
                .addOnSuccessListener(labels -> {
                    if (labels == null || labels.isEmpty()) {
                        // Honest fallback
                        callback.onSuccess(buildFallbackResult(new ArrayList<>()));
                        return;
                    }

                    List<String> detectedTags = new ArrayList<>();
                    Map<String, Float> categoryScores = new HashMap<>();
                    categoryScores.put("WASTE", 0f);
                    categoryScores.put("WATER", 0f);
                    categoryScores.put("AIR", 0f);
                    categoryScores.put("GREEN_SPACES", 0f);

                    float highestConfidence = 0f;

                    for (ImageLabel label : labels) {
                        String text = label.getText();
                        float conf = label.getConfidence();
                        if (conf > highestConfidence) highestConfidence = conf;
                        String lower = text.toLowerCase(Locale.ROOT);

                        detectedTags.add(String.format(Locale.US, "%s (%d%%)", text, Math.round(conf * 100)));

                        // Score categories
                        for (String kw : WASTE_KEYWORDS) {
                            if (lower.contains(kw)) {
                                categoryScores.put("WASTE", categoryScores.get("WASTE") + conf);
                            }
                        }
                        for (String kw : WATER_KEYWORDS) {
                            if (lower.contains(kw)) {
                                categoryScores.put("WATER", categoryScores.get("WATER") + conf);
                            }
                        }
                        for (String kw : AIR_KEYWORDS) {
                            if (lower.contains(kw)) {
                                categoryScores.put("AIR", categoryScores.get("AIR") + conf);
                            }
                        }
                        for (String kw : GREEN_KEYWORDS) {
                            if (lower.contains(kw)) {
                                categoryScores.put("GREEN_SPACES", categoryScores.get("GREEN_SPACES") + conf);
                            }
                        }
                    }

                    // Determine top category
                    String topCategory = "OTHER";
                    float topScore = 0f;

                    for (Map.Entry<String, Float> entry : categoryScores.entrySet()) {
                        if (entry.getValue() > topScore) {
                            topScore = entry.getValue();
                            topCategory = entry.getKey();
                        }
                    }

                    // Generate suggestion based on top category and labels
                    AnalysisResult result = buildCategorizedResult(topCategory, detectedTags, topScore, highestConfidence);
                    callback.onSuccess(result);
                })
                .addOnFailureListener(e -> {
                    // Provide honest fallback on failure
                    callback.onError("Analysis could not complete: " + e.getLocalizedMessage() + ". You can enter details manually.");
                });
    }

    private static AnalysisResult buildCategorizedResult(String category,
                                                         List<String> tags,
                                                         float categoryScore,
                                                         float maxConfidence) {
        String tagSummary = tags.isEmpty() ? "Visual scene" : String.join(", ", tags.subList(0, Math.min(3, tags.size())));
        String title;
        String description;

        if (categoryScore < 0.35f) {
            // Low association with environmental problem categories
            return buildFallbackResult(tags);
        }

        switch (category) {
            case "WASTE":
                title = "Waste Accumulation / Garbage Disposal";
                description = "Visible waste or debris identified on-site (" + tagSummary + "). Debris accumulated in public space requires municipal cleanup and clearance.";
                break;
            case "WATER":
                title = "Water Leakage or Drainage Issue";
                description = "Water accumulation or drainage concern observed (" + tagSummary + "). Likely pipe overflow, standing water, or gutter issue requiring inspection.";
                break;
            case "AIR":
                title = "Air Quality / Smoke Emission Concern";
                description = "Visible atmospheric haze, smoke, or emission detected (" + tagSummary + "). Observed emission in the vicinity requires environmental monitoring.";
                break;
            case "GREEN_SPACES":
                title = "Tree / Green Space Maintenance Issue";
                description = "Vegetation or tree maintenance concern detected (" + tagSummary + "). Observed overgrowth, fallen branch, or tree care requirement.";
                break;
            default:
                return buildFallbackResult(tags);
        }

        return new AnalysisResult(category, title, description, tags, maxConfidence);
    }

    private static AnalysisResult buildFallbackResult(List<String> tags) {
        String tagSummary = tags.isEmpty() ? "No specific environmental tags identified" : String.join(", ", tags.subList(0, Math.min(3, tags.size())));
        String title = "Civic Environmental Issue";
        String description = "Photographic evidence attached (" + tagSummary + "). Observed civic issue requiring attention. Please add specific location and details.";
        return new AnalysisResult("OTHER", title, description, tags, 0.40f);
    }
}
