package com.sustainiq.app.util;

import android.os.Handler;
import android.os.Looper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

public class PinCodeHelper {

    public static class PinLocation {
        public final String pinCode;
        public final String district;
        public final String city;
        public final String state;

        public PinLocation(String pinCode, String district, String city, String state) {
            this.pinCode  = pinCode;
            this.district = district;
            this.city     = city;
            this.state    = state;
        }
    }

    public interface Callback {
        void onResult(PinLocation location);
        void onError(String message);
    }

    private static final Map<String, String[]> MH_OFFLINE_PINS = new HashMap<>();

    static {
        // Prefix -> [District, City]
        MH_OFFLINE_PINS.put("400", new String[]{"Mumbai", "Mumbai"});
        MH_OFFLINE_PINS.put("401", new String[]{"Palghar", "Palghar"});
        MH_OFFLINE_PINS.put("402", new String[]{"Raigad", "Alibaug"});
        MH_OFFLINE_PINS.put("410", new String[]{"Pune", "Lonavala / Talegaon"});
        MH_OFFLINE_PINS.put("411", new String[]{"Pune", "Pune"});
        MH_OFFLINE_PINS.put("412", new String[]{"Pune", "Haveli"});
        MH_OFFLINE_PINS.put("413", new String[]{"Solapur", "Solapur"});
        MH_OFFLINE_PINS.put("414", new String[]{"Ahmednagar", "Ahmednagar"});
        MH_OFFLINE_PINS.put("415", new String[]{"Satara", "Satara"});
        MH_OFFLINE_PINS.put("416", new String[]{"Kolhapur", "Kolhapur"});
        MH_OFFLINE_PINS.put("421", new String[]{"Thane", "Kalyan / Dombivli"});
        MH_OFFLINE_PINS.put("422", new String[]{"Nashik", "Nashik"});
        MH_OFFLINE_PINS.put("423", new String[]{"Nashik", "Malegaon"});
        MH_OFFLINE_PINS.put("424", new String[]{"Dhule", "Dhule"});
        MH_OFFLINE_PINS.put("425", new String[]{"Jalgaon", "Jalgaon"});
        MH_OFFLINE_PINS.put("431", new String[]{"Chh. Sambhajinagar", "Aurangabad"});
        MH_OFFLINE_PINS.put("440", new String[]{"Nagpur", "Nagpur"});
        MH_OFFLINE_PINS.put("441", new String[]{"Nagpur", "Ramtek"});
        MH_OFFLINE_PINS.put("442", new String[]{"Wardha", "Wardha"});
        MH_OFFLINE_PINS.put("443", new String[]{"Buldhana", "Buldhana"});
        MH_OFFLINE_PINS.put("444", new String[]{"Akola", "Akola / Amravati"});
        MH_OFFLINE_PINS.put("445", new String[]{"Yavatmal", "Yavatmal"});
    }

    /**
     * Looks up location for a given 6-digit PIN code.
     * Checks online API first, falls back immediately to offline Maharashtra dictionary.
     */
    public static void lookup(String pinCode, Callback callback) {
        if (pinCode == null || pinCode.trim().length() != 6) {
            callback.onError("PIN code must be 6 digits");
            return;
        }

        String pin = pinCode.trim();
        Handler mainHandler = new Handler(Looper.getMainLooper());

        Executors.newSingleThreadExecutor().execute(() -> {
            // Attempt postal API
            try {
                URL url = new URL("https://api.postalpincode.in/pincode/" + pin);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(3000);
                conn.setReadTimeout(3000);

                if (conn.getResponseCode() == 200) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) sb.append(line);
                    reader.close();

                    JSONArray arr = new JSONArray(sb.toString());
                    if (arr.length() > 0) {
                        JSONObject obj = arr.getJSONObject(0);
                        if ("Success".equalsIgnoreCase(obj.optString("Status"))) {
                            JSONArray postOffices = obj.optJSONArray("PostOffice");
                            if (postOffices != null && postOffices.length() > 0) {
                                JSONObject po = postOffices.getJSONObject(0);
                                String district = po.optString("District", "Maharashtra");
                                String block    = po.optString("Block", district);
                                String state    = po.optString("State", "Maharashtra");
                                PinLocation loc = new PinLocation(pin, district, block, state);
                                mainHandler.post(() -> callback.onResult(loc));
                                return;
                            }
                        }
                    }
                }
            } catch (Exception ignored) {
                // Network unavailable or timeout; fallback to offline
            }

            // Offline fallback
            String prefix3 = pin.substring(0, 3);
            if (MH_OFFLINE_PINS.containsKey(prefix3)) {
                String[] info = MH_OFFLINE_PINS.get(prefix3);
                PinLocation loc = new PinLocation(pin, info[0], info[1], "Maharashtra");
                mainHandler.post(() -> callback.onResult(loc));
            } else if (pin.startsWith("4")) {
                PinLocation loc = new PinLocation(pin, "Maharashtra Region", "City", "Maharashtra");
                mainHandler.post(() -> callback.onResult(loc));
            } else {
                mainHandler.post(() -> callback.onError("Location not found for PIN " + pin));
            }
        });
    }
}
