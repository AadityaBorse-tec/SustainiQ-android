package com.sustainiq.app.ui.assistant;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.sustainiq.app.R;
import com.sustainiq.app.data.db.AppDatabase;
import com.sustainiq.app.data.db.ChatMessageEntity;
import com.sustainiq.app.ui.main.MainActivity;
import com.sustainiq.app.ui.notifications.NotificationsActivity;
import com.sustainiq.app.ui.reports.ReportDetailActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

/**
 * SUSTAINIQ Voice + Text AI Environmental Assistant.
 *
 * Voice state machine: IDLE → LISTENING → PROCESSING → SUCCESS/ERROR
 * Navigation commands are executed safely (no auto-submit of reports).
 * TTS confirmation is spoken after every voice command.
 */
public class AssistantActivity extends AppCompatActivity {

    // Voice state
    private enum VoiceState { IDLE, LISTENING, PROCESSING, SUCCESS, ERROR }

    private MaterialToolbar toolbar;
    private RecyclerView rvMessages;
    private ChatAdapter adapter;
    private TextInputEditText etInput;
    private ImageView btnSend;
    private FloatingActionButton btnVoice;
    private TextView tvVoiceStatus;
    private Chip chipWaste, chipMpcb, chipStatus, chipAir;

    private SpeechRecognizer speechRecognizer;
    private TextToSpeech tts;
    private AppDatabase db;
    private VoiceState voiceState = VoiceState.IDLE;

    // ─── Navigation destination keys ─────────────────────────────────────────
    private static final int NAV_NONE     = 0;
    private static final int NAV_REPORT   = 1;
    private static final int NAV_REPORTS  = 2;
    private static final int NAV_NOTIFS   = 3;
    private static final int NAV_IMPACT   = 4;
    private static final int NAV_PROFILE  = 5;
    private static final int NAV_COMMUNITY= 6;

    private final ActivityResultLauncher<String> recordAudioPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    startVoiceListening();
                } else {
                    setVoiceState(VoiceState.ERROR);
                    addBotMessage("Microphone permission was denied. Please enable it in Settings > Apps > SUSTAINIQ > Permissions to use voice commands. You can still type your question below.");
                }
            });

    private final ActivityResultLauncher<Intent> speechIntentLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    ArrayList<String> matches = result.getData().getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if (matches != null && !matches.isEmpty()) {
                        String recognized = matches.get(0);
                        ChatMessageEntity userMsg = new ChatMessageEntity();
                        userMsg.text = "\"" + recognized + "\"";
                        userMsg.isBot = false;
                        userMsg.timestamp = System.currentTimeMillis();
                        adapter.addMessage(userMsg);
                        rvMessages.scrollToPosition(adapter.getItemCount() - 1);
                        handleVoiceCommand(recognized);
                        return;
                    }
                }
                setVoiceState(VoiceState.IDLE);
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assistant);

        db = AppDatabase.getInstance(this);
        initTts();

        bindViews();
        setupToolbar();
        setupRecyclerView();
        setupChips();
        setupInput();
        setupVoice();

        loadInitialWelcome();
        String initialQuery = getIntent().getStringExtra("extra_initial_query");
        if (initialQuery != null && !initialQuery.trim().isEmpty()) {
            sendUserMessage(initialQuery.trim());
        }
    }

    private void initTts() {
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(new Locale("en", "IN")); // Indian English
            }
        });
    }

    private void speak(String text) {
        if (tts != null) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "sustainiq_tts");
        }
    }

    private void bindViews() {
        toolbar      = findViewById(R.id.toolbar_assistant);
        rvMessages   = findViewById(R.id.rv_chat_messages);
        etInput      = findViewById(R.id.et_chat_input);
        btnSend      = findViewById(R.id.btn_send_message);
        btnVoice     = findViewById(R.id.btn_voice_input);
        tvVoiceStatus= findViewById(R.id.tv_voice_status);

        chipWaste  = findViewById(R.id.chip_q_waste);
        chipMpcb   = findViewById(R.id.chip_q_mpcb);
        chipStatus = findViewById(R.id.chip_q_status);
        chipAir    = findViewById(R.id.chip_q_air);
    }

    private void setupToolbar() {
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new ChatAdapter();
        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setStackFromEnd(true);
        rvMessages.setLayoutManager(lm);
        rvMessages.setAdapter(adapter);
    }

    private void setupChips() {
        chipWaste.setOnClickListener(v -> sendUserMessage("What are the municipal waste segregation rules?"));
        chipMpcb.setOnClickListener(v -> sendUserMessage("How do I contact Maharashtra Pollution Control Board?"));
        chipStatus.setOnClickListener(v -> sendUserMessage("What happens after I submit a report?"));
        chipAir.setOnClickListener(v -> sendUserMessage("How to handle open garbage burning and air smog?"));
    }

    private void setupInput() {
        btnSend.setOnClickListener(v -> submitQuery());
        etInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                submitQuery();
                return true;
            }
            return false;
        });
    }

    private void submitQuery() {
        if (etInput.getText() == null) return;
        String text = etInput.getText().toString().trim();
        if (text.isEmpty()) return;
        etInput.setText("");
        sendUserMessage(text);
    }

    // ─── Voice ───────────────────────────────────────────────────────────────

    private void setupVoice() {
        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
            speechRecognizer.setRecognitionListener(buildRecognitionListener());
        }

        btnVoice.setOnClickListener(v -> {
            if (voiceState == VoiceState.LISTENING) {
                // Second tap cancels
                if (speechRecognizer != null) speechRecognizer.stopListening();
                setVoiceState(VoiceState.IDLE);
                return;
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                    == PackageManager.PERMISSION_GRANTED) {
                startVoiceListening();
            } else {
                addBotMessage("This app needs microphone permission for voice commands. Tap the mic again after granting access in the system dialog.");
                recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO);
            }
        });
    }

    private RecognitionListener buildRecognitionListener() {
        return new RecognitionListener() {
            @Override public void onReadyForSpeech(Bundle params) {
                setVoiceState(VoiceState.LISTENING);
            }
            @Override public void onBeginningOfSpeech() {}
            @Override public void onRmsChanged(float rmsdB) {}
            @Override public void onBufferReceived(byte[] buffer) {}
            @Override public void onEndOfSpeech() {
                setVoiceState(VoiceState.PROCESSING);
            }
            @Override
            public void onError(int error) {
                setVoiceState(VoiceState.ERROR);
                String msg;
                switch (error) {
                    case SpeechRecognizer.ERROR_NO_MATCH:
                        msg = "No speech recognized. Please try again or type your command below.";
                        break;
                    case SpeechRecognizer.ERROR_NETWORK:
                    case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                        msg = "Network unavailable for speech recognition. Please type your command.";
                        break;
                    case SpeechRecognizer.ERROR_AUDIO:
                        msg = "Microphone error. Please check your device's microphone.";
                        break;
                    default:
                        msg = "Voice input was not captured. Please try again.";
                }
                addBotMessage(msg);
            }
            @Override
            public void onResults(Bundle results) {
                setVoiceState(VoiceState.SUCCESS);
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String recognized = matches.get(0);
                    // Show what was understood
                    ChatMessageEntity userMsg = new ChatMessageEntity();
                    userMsg.text = "\"" + recognized + "\"";
                    userMsg.isBot = false;
                    userMsg.timestamp = System.currentTimeMillis();
                    adapter.addMessage(userMsg);
                    rvMessages.scrollToPosition(adapter.getItemCount() - 1);

                    // Route voice command
                    handleVoiceCommand(recognized);
                } else {
                    addBotMessage("I could not understand that. Please try again or type your question.");
                }
            }
            @Override public void onPartialResults(Bundle partialResults) {}
            @Override public void onEvent(int eventType, Bundle params) {}
        };
    }

    private void startVoiceListening() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-IN");
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en-IN,hi-IN");
        intent.putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, false);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak environmental query or command...");

        if (speechRecognizer != null && SpeechRecognizer.isRecognitionAvailable(this)) {
            try {
                speechRecognizer.startListening(intent);
                return;
            } catch (Exception ignored) {}
        }
        try {
            speechIntentLauncher.launch(intent);
        } catch (Exception e) {
            setVoiceState(VoiceState.ERROR);
            addBotMessage("Could not launch voice recognition: " + e.getMessage() + ". You can type your command below.");
        }
    }

    private void setVoiceState(VoiceState state) {
        voiceState = state;
        if (tvVoiceStatus == null) return;
        switch (state) {
            case IDLE:
                tvVoiceStatus.setVisibility(View.GONE);
                btnVoice.setImageResource(R.drawable.ic_mic);
                break;
            case LISTENING:
                tvVoiceStatus.setVisibility(View.VISIBLE);
                tvVoiceStatus.setText("Listening… speak now");
                btnVoice.setImageResource(R.drawable.ic_mic); // stays same; tint changes via animator
                break;
            case PROCESSING:
                tvVoiceStatus.setVisibility(View.VISIBLE);
                tvVoiceStatus.setText("Processing…");
                break;
            case SUCCESS:
                tvVoiceStatus.setVisibility(View.VISIBLE);
                tvVoiceStatus.setText("Got it!");
                btnVoice.postDelayed(() -> setVoiceState(VoiceState.IDLE), 1500);
                break;
            case ERROR:
                tvVoiceStatus.setVisibility(View.VISIBLE);
                tvVoiceStatus.setText("Not recognised — try again");
                btnVoice.postDelayed(() -> setVoiceState(VoiceState.IDLE), 2000);
                break;
        }
    }

    // ─── Voice command router ─────────────────────────────────────────────────

    private void handleVoiceCommand(String raw) {
        if (raw == null) return;
        String q = raw.toLowerCase(Locale.ROOT)
                .replace("'", "")
                .replace("\"", "")
                .trim();

        // 1. Stop listening command
        if (containsAny(q, "stop listening", "stop", "ruko", "ruk jao", "bas karo", "band karo", "बंद करो", "रुक जाओ")) {
            if (speechRecognizer != null) speechRecognizer.stopListening();
            setVoiceState(VoiceState.IDLE);
            addBotMessage("Voice listening stopped.");
            speak("Listening stopped.");
            return;
        }

        // 2. Specific reporting commands with category intent
        if (containsAny(q, "water leak", "pani leak", "pani ki samasya", "water problem",
                "leak", "drain overflow", "sewage leak", "पानी का रिसाव", "पानी की समस्या")) {
            navigateToReport("WATER", "Water Leakage Issue",
                    "Opening report form for water issue. Please review details, capture a photo, and confirm before submitting.");
            return;
        }

        if (containsAny(q, "report garbage near my house", "garbage near my house",
                "kachre ki complaint", "kachra", "garbage", "trash", "waste dump", "litter",
                "कचरे की शिकायत", "कचरा")) {
            navigateToReport("WASTE", "Garbage / Waste Issue",
                    "Opening report form for waste issue. Please review details, capture a photo, and confirm before submitting.");
            return;
        }

        if (containsAny(q, "air pollution", "smoke", "burning", "hawa pradushan", "dhuan", "smog",
                "हवा प्रदूषण", "धुआं")) {
            navigateToReport("AIR", "Air Pollution Issue",
                    "Opening report form for air pollution. Please review details, capture a photo, and confirm before submitting.");
            return;
        }

        if (containsAny(q, "tree cutting", "illegal felling", "fallen tree", "ped katna", "green space",
                "पेड़ काटना", "पेड़")) {
            navigateToReport("GREEN_SPACES", "Tree / Green Space Issue",
                    "Opening report form for green spaces. Please review details, capture a photo, and confirm before submitting.");
            return;
        }

        // 3. General report commands (English & Hindi)
        if (containsAny(q, "i want to report an issue", "mujhe complaint karni hai", "mujhe report karni hai",
                "report an issue", "report issue", "file a report", "complaint karni hai",
                "shikayat karni hai", "new report", "create report", "शिकायत करनी है", "रिपोर्ट करनी है")) {
            navigateToReport(null, null,
                    "Opening report form. Please select category, fill details, and confirm before submitting.");
            return;
        }

        // 4. My Reports commands
        if (containsAny(q, "open my reports", "meri reports dikhao", "meri report", "show my reports",
                "my reports", "my complaints", "track my reports", "मेरी रिपोर्ट दिखाओ", "मेरी रिपोर्ट")) {
            navigateTo(NAV_REPORTS);
            return;
        }

        // 5. Community updates commands
        if (containsAny(q, "show community updates", "community updates", "community", "public feed",
                "community dikhao", "कम्युनिटी दिखाओ", "कम्युनिटी")) {
            navigateTo(NAV_COMMUNITY);
            return;
        }

        // 6. Environmental impact commands
        if (containsAny(q, "open environmental impact", "environmental impact", "impact", "analytics",
                "statistics", "impact dikhao", "पर्यावरण प्रभाव", "प्रभाव")) {
            navigateTo(NAV_IMPACT);
            return;
        }

        // 7. Profile commands
        if (containsAny(q, "open my profile", "my profile", "profile", "account", "settings",
                "meri profile", "profile dikhao", "मेरी प्रोफाइल", "प्रोफाइल")) {
            navigateTo(NAV_PROFILE);
            return;
        }

        // 8. Notifications commands
        if (containsAny(q, "show notifications", "notifications", "alerts", "unread", "सूचनाएं")) {
            navigateTo(NAV_NOTIFS);
            return;
        }

        // If not a navigation/reporting command, treat as Q&A
        sendUserMessage(raw);
    }

    private boolean containsAny(String text, String... keywords) {
        for (String kw : keywords) {
            if (text.contains(kw.toLowerCase(Locale.ROOT))) return true;
        }
        return false;
    }

    private void navigateToReport(String category, String title, String confirmation) {
        addBotMessage("Navigating: " + confirmation);
        speak(confirmation);
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("nav_tab", "report");
        if (category != null) intent.putExtra("category", category);
        if (title != null) intent.putExtra("title", title);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    private void navigateTo(int target) {
        String confirmation;
        Intent intent;

        switch (target) {
            case NAV_REPORT:
                confirmation = "Opening Report an Issue. Please confirm details before submission.";
                intent = new Intent(this, MainActivity.class);
                intent.putExtra("nav_tab", "report");
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                break;
            case NAV_REPORTS:
                confirmation = "Opening My Reports.";
                intent = new Intent(this, MainActivity.class);
                intent.putExtra("nav_tab", "my_reports");
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                break;
            case NAV_NOTIFS:
                confirmation = "Opening Notifications.";
                intent = new Intent(this, NotificationsActivity.class);
                break;
            case NAV_IMPACT:
                confirmation = "Opening Environmental Impact Dashboard.";
                intent = new Intent(this, MainActivity.class);
                intent.putExtra("nav_tab", "impact");
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                break;
            case NAV_PROFILE:
                confirmation = "Opening Profile.";
                intent = new Intent(this, MainActivity.class);
                intent.putExtra("nav_tab", "profile");
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                break;
            case NAV_COMMUNITY:
                confirmation = "Opening Community Updates.";
                intent = new Intent(this, MainActivity.class);
                intent.putExtra("nav_tab", "community");
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                break;
            default:
                return;
        }

        addBotMessage("Navigating: " + confirmation);
        speak(confirmation);
        startActivity(intent);
    }

    // ─── Chat engine ──────────────────────────────────────────────────────────

    private void loadInitialWelcome() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<ChatMessageEntity> history = db.chatDao().getAllMessages();
            if (history == null || history.isEmpty()) {
                ChatMessageEntity welcome = new ChatMessageEntity();
                welcome.text = "Namaste! I am your SUSTAINIQ Civic Environmental Assistant.\n\nYou can:\n• Tap the mic and speak a command (e.g. \"Show my reports\" or \"Report an issue\")\n• Ask environmental questions about waste, air, water, or MPCB contacts\n• Or tap a quick suggestion above.\n\n[Demo mode — responses are rule-based, not AI-generated]";
                welcome.isBot = true;
                welcome.timestamp = System.currentTimeMillis();
                db.chatDao().insertMessage(welcome);
                runOnUiThread(() -> adapter.addMessage(welcome));
            } else {
                runOnUiThread(() -> {
                    adapter.setMessages(history);
                    rvMessages.scrollToPosition(adapter.getItemCount() - 1);
                });
            }
        });
    }

    private void addBotMessage(String text) {
        ChatMessageEntity msg = new ChatMessageEntity();
        msg.text = text;
        msg.isBot = true;
        msg.timestamp = System.currentTimeMillis();
        adapter.addMessage(msg);
        rvMessages.scrollToPosition(adapter.getItemCount() - 1);
        Executors.newSingleThreadExecutor().execute(() -> db.chatDao().insertMessage(msg));
    }

    private void sendUserMessage(String query) {
        long now = System.currentTimeMillis();

        ChatMessageEntity userMsg = new ChatMessageEntity();
        userMsg.text = query;
        userMsg.isBot = false;
        userMsg.timestamp = now;

        adapter.addMessage(userMsg);
        rvMessages.scrollToPosition(adapter.getItemCount() - 1);

        Executors.newSingleThreadExecutor().execute(() -> {
            db.chatDao().insertMessage(userMsg);
            String response = generateBotResponse(query);
            ChatMessageEntity botMsg = new ChatMessageEntity();
            botMsg.text = response;
            botMsg.isBot = true;
            botMsg.timestamp = System.currentTimeMillis();
            db.chatDao().insertMessage(botMsg);
            runOnUiThread(() -> {
                adapter.addMessage(botMsg);
                rvMessages.scrollToPosition(adapter.getItemCount() - 1);
            });
        });
    }

    private String generateBotResponse(String q) {
        String query = q.toLowerCase(Locale.ENGLISH);

        if (containsAny(query, "waste", "kachra", "कचरा", "segregat", "garbage", "dumping", "litter")) {
            return "Waste Management in Maharashtra\n\n" +
                    "• Green Bin — Wet waste: food scraps, vegetable peels, garden leaves (collected daily).\n" +
                    "• Blue Bin — Dry waste: paper, cardboard, plastic bottles, metal cans.\n" +
                    "• Red Bag — Hazardous: diapers, batteries, sanitary pads, broken glass.\n\n" +
                    "Open dumping or mixing waste violates Maharashtra Solid Waste Management Byelaws.\n" +
                    "Spotted an overflowing dump? Use the 'Report Issue' tab.";
        }
        if (containsAny(query, "mpcb", "pollution control", "helpline", "contact", "phone", "board")) {
            return "Maharashtra Pollution Control Board (MPCB)\n\n" +
                    "• Head Office (Sion, Mumbai): 022-24010437\n" +
                    "• Toll-Free Helpline: 1800-22-0161\n" +
                    "• Pune Regional Office: 020-25811694\n" +
                    "• Nagpur Regional Office: 0712-2560155\n" +
                    "• Website: mpcb.gov.in\n\n" +
                    "SUSTAINIQ reports with GPS coordinates are tagged for jurisdictional referral.";
        }
        if (containsAny(query, "status", "happen after", "track", "workflow", "process", "lifecycle")) {
            return "Report Accountability Lifecycle\n\n" +
                    "1. REPORTED — Citizen submits issue with evidence\n" +
                    "2. TRIAGED — Admin verifies jurisdiction & priority\n" +
                    "3. ASSIGNED — Dispatched to ward supervisor or field crew\n" +
                    "4. IN PROGRESS — Field workers mobilised on-site\n" +
                    "5. RESOLUTION SUBMITTED — Field worker submits evidence\n" +
                    "6. VERIFIED — Citizen or admin confirms resolution\n\n" +
                    "Tap any report in My Reports to see its live timeline.";
        }
        if (containsAny(query, "air", "smoke", "burn", "smog", "aqi", "हवा", "pollution")) {
            return "Air Quality & Open Burning\n\n" +
                    "• Open burning of municipal solid waste, plastic, or leaves is prohibited under Section 15, Environment Protection Act, 1986.\n" +
                    "• Spot fines: up to ₹5,000.\n" +
                    "• Industrial emissions must meet MPCB-specified stack norms.\n\n" +
                    "File an 'Air' report with a photo whenever you observe open burning.";
        }
        if (containsAny(query, "water", "leak", "pipe", "river", "drain", "sewage", "पाणी")) {
            return "Water Protection & Leakages\n\n" +
                    "• Pipeline bursts waste hundreds of litres per minute — report immediately under 'Water' category.\n" +
                    "• Sewage discharge into rivers (Mithi, Mutha, Godavari) is penalised under Water (Prevention and Control of Pollution) Act, 1974.\n" +
                    "• Include landmark details and a photo for faster triage.";
        }
        if (containsAny(query, "tree", "park", "green", "forest", "झाड")) {
            return "Green Spaces & Tree Authority\n\n" +
                    "• Cutting any tree with trunk girth >10 cm requires prior permission from the local Tree Authority (Maharashtra Felling of Trees Act, 1964).\n" +
                    "• Unauthorised felling is a cognizable offense.\n" +
                    "• Report illegal felling under 'Green Spaces' with GPS location.";
        }
        if (containsAny(query, "hi", "hello", "namaste", "hey", "नमस्ते")) {
            return "Hello! How can I help you today?\n\nAsk about environmental rules, MPCB contacts, report workflows — or say a voice command like 'Show my reports' to navigate.";
        }
        if (containsAny(query, "hindi", "language", "change language", "हिंदी")) {
            return "To change the app language to Hindi:\n\n1. Tap the Profile tab (bottom right)\n2. Tap 'Language Preference'\n3. Select 'हिंदी'\n\nThe app will reload in Hindi. Full Marathi support is planned for a future release.";
        }

        return "I can assist with:\n\n" +
                "• Waste segregation guidelines\n" +
                "• MPCB and municipal helplines\n" +
                "• Report status workflows\n" +
                "• Air quality and open burning rules\n" +
                "• Water leakage and sewage reporting\n" +
                "• Green spaces and tree protection\n\n" +
                "Or say a command like:\n\"Show my reports\" | \"Report an issue\" | \"Open notifications\" | \"Show impact\"\n\n" +
                "[Responses are rule-based. No data is sent to external servers.]";
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (speechRecognizer != null) {
            speechRecognizer.stopListening();
        }
        setVoiceState(VoiceState.IDLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speechRecognizer != null) speechRecognizer.destroy();
        if (tts != null) { tts.stop(); tts.shutdown(); }
    }
}
