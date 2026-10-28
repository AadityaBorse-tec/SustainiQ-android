package com.sustainiq.app;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit test validating Voice Assistant command routing & Image Analysis keyword categorization logic.
 */
public class VoiceAndAnalysisLogicTest {

    // Helper simulating voice router intent detection
    private String routeVoiceIntent(String raw) {
        if (raw == null) return "UNKNOWN";
        String q = raw.toLowerCase(Locale.ROOT)
                .replace("'", "")
                .replace("\"", "")
                .trim();

        if (containsAny(q, "stop listening", "stop", "ruko", "ruk jao", "bas karo", "band karo")) {
            return "STOP_LISTENING";
        }
        if (containsAny(q, "water leak", "pani leak", "pani ki samasya", "water problem", "leak", "drain overflow")) {
            return "REPORT_WATER";
        }
        if (containsAny(q, "report garbage near my house", "garbage near my house", "kachre ki complaint", "kachra", "garbage", "waste")) {
            return "REPORT_WASTE";
        }
        if (containsAny(q, "air pollution", "smoke", "burning", "hawa pradushan", "dhuan", "smog")) {
            return "REPORT_AIR";
        }
        if (containsAny(q, "tree cutting", "illegal felling", "fallen tree", "ped katna", "green space", "tree", "ped")) {
            return "REPORT_GREEN";
        }
        if (containsAny(q, "i want to report an issue", "mujhe complaint karni hai", "mujhe report karni hai",
                "report an issue", "report issue", "file a report", "complaint karni hai", "shikayat karni hai")) {
            return "REPORT_GENERAL";
        }
        if (containsAny(q, "open my reports", "meri reports dikhao", "meri report", "show my reports", "my reports")) {
            return "MY_REPORTS";
        }
        if (containsAny(q, "show community updates", "community updates", "community", "community dikhao")) {
            return "COMMUNITY";
        }
        if (containsAny(q, "open environmental impact", "environmental impact", "impact", "analytics", "impact dikhao")) {
            return "IMPACT";
        }
        if (containsAny(q, "open my profile", "my profile", "profile", "meri profile", "profile dikhao")) {
            return "PROFILE";
        }
        return "QA_FALLBACK";
    }

    private boolean containsAny(String text, String... keywords) {
        for (String kw : keywords) {
            if (text.contains(kw.toLowerCase(Locale.ROOT))) return true;
        }
        return false;
    }

    @Test
    public void testVoiceCommand_ReportIssue_EnglishAndHindi() {
        assertEquals("REPORT_GENERAL", routeVoiceIntent("I want to report an issue."));
        assertEquals("REPORT_GENERAL", routeVoiceIntent("Mujhe complaint karni hai."));
        assertEquals("REPORT_GENERAL", routeVoiceIntent("File a report"));
    }

    @Test
    public void testVoiceCommand_MyReports_EnglishAndHindi() {
        assertEquals("MY_REPORTS", routeVoiceIntent("Open my reports."));
        assertEquals("MY_REPORTS", routeVoiceIntent("Meri reports dikhao."));
    }

    @Test
    public void testVoiceCommand_Community_EnglishAndHindi() {
        assertEquals("COMMUNITY", routeVoiceIntent("Show community updates."));
        assertEquals("COMMUNITY", routeVoiceIntent("Community dikhao"));
    }

    @Test
    public void testVoiceCommand_ImpactAndProfile() {
        assertEquals("IMPACT", routeVoiceIntent("Open environmental impact."));
        assertEquals("PROFILE", routeVoiceIntent("Open my profile."));
    }

    @Test
    public void testVoiceCommand_StopListening() {
        assertEquals("STOP_LISTENING", routeVoiceIntent("Stop listening."));
        assertEquals("STOP_LISTENING", routeVoiceIntent("Ruko"));
        assertEquals("STOP_LISTENING", routeVoiceIntent("Bas karo"));
    }

    @Test
    public void testVoiceCommand_SpecificReportingCategories() {
        assertEquals("REPORT_WATER", routeVoiceIntent("Help me report a water leak."));
        assertEquals("REPORT_WASTE", routeVoiceIntent("Report garbage near my house."));
        assertEquals("REPORT_WASTE", routeVoiceIntent("Mujhe kachre ki complaint karni hai."));
        assertEquals("REPORT_AIR", routeVoiceIntent("Air pollution near market"));
        assertEquals("REPORT_GREEN", routeVoiceIntent("Illegal tree cutting"));
    }

    @Test
    public void testImageLabelCategoryClassification() {
        List<String> wasteLabels = Arrays.asList("Plastic", "Waste", "Litter");
        String matchedCat = classifyLabels(wasteLabels);
        assertEquals("WASTE", matchedCat);

        List<String> waterLabels = Arrays.asList("Water", "Puddle", "Drainage");
        assertEquals("WATER", classifyLabels(waterLabels));

        List<String> greenLabels = Arrays.asList("Tree", "Vegetation", "Plant");
        assertEquals("GREEN_SPACES", classifyLabels(greenLabels));
    }

    private String classifyLabels(List<String> labels) {
        int waste = 0, water = 0, air = 0, green = 0;
        for (String l : labels) {
            String lower = l.toLowerCase(Locale.ROOT);
            if (lower.contains("waste") || lower.contains("plastic") || lower.contains("litter") || lower.contains("trash")) waste++;
            if (lower.contains("water") || lower.contains("puddle") || lower.contains("drain")) water++;
            if (lower.contains("smoke") || lower.contains("smog") || lower.contains("air")) air++;
            if (lower.contains("tree") || lower.contains("plant") || lower.contains("vegetation")) green++;
        }
        if (waste >= water && waste >= air && waste >= green && waste > 0) return "WASTE";
        if (water >= waste && water >= air && water >= green && water > 0) return "WATER";
        if (air >= waste && air >= water && air >= green && air > 0) return "AIR";
        if (green >= waste && green >= water && green >= air && green > 0) return "GREEN_SPACES";
        return "OTHER";
    }
}
