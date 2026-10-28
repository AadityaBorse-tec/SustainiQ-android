package com.sustainiq.app.ui.reports;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.sustainiq.app.R;
import com.sustainiq.app.data.db.ReportEntity;
import com.sustainiq.app.data.repository.ReportRepository;
import com.sustainiq.app.ui.main.MainActivity;
import com.sustainiq.app.util.AppUtils;
import com.sustainiq.app.util.ImageAnalysisHelper;
import com.sustainiq.app.util.PinCodeHelper;
import com.sustainiq.app.util.SessionManager;

import java.io.File;

public class CreateReportFragment extends Fragment {

    public static final String ARG_CATEGORY = "arg_initial_category";
    public static final String ARG_TITLE = "arg_initial_title";

    private Chip chipWaste, chipWater, chipAir, chipGreen, chipOther;
    private TextView tvCategoryError;
    private TextInputLayout tilTitle, tilDescription, tilPin;
    private TextInputEditText etTitle, etDescription, etPin, etLocality, etCity, etDistrict;
    private MaterialButton btnCamera, btnGallery, btnUseGps, btnSubmit;
    private ImageView imgPreview;
    private CardView cardPhotoPreview;
    private MaterialButton btnAnalyseImage, btnRemovePhoto;
    private LinearLayout layoutAnalysisLoading;
    private TextView tvAnalysisError;
    private CardView cardAiSuggestion;
    private TextView tvAiTags, tvAiCategory, tvAiTitle, tvAiDescription;
    private MaterialButton btnApplyAiSuggestion;

    private CardView cardLocationResult;
    private TextView tvLocationResult;
    private ProgressBar progressPin, progressSubmit;

    private String selectedCategory = "";
    private String capturedPhotoPath = "";
    private Uri currentPhotoUri;
    private ImageAnalysisHelper.AnalysisResult currentAnalysisResult;
    private ReportRepository repository;
    private SessionManager session;

    private final ActivityResultLauncher<Uri> takePictureLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
                if (Boolean.TRUE.equals(success)) {
                    if (currentPhotoUri != null) {
                        capturedPhotoPath = currentPhotoUri.toString();
                        imgPreview.setImageURI(currentPhotoUri);
                        cardPhotoPreview.setVisibility(View.VISIBLE);
                        cardAiSuggestion.setVisibility(View.GONE);
                        tvAnalysisError.setVisibility(View.GONE);
                    }
                } else {
                    Toast.makeText(requireContext(), "Camera capture cancelled", Toast.LENGTH_SHORT).show();
                }
            });

    private final ActivityResultLauncher<String> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    currentPhotoUri = uri;
                    capturedPhotoPath = uri.toString();
                    imgPreview.setImageURI(uri);
                    cardPhotoPreview.setVisibility(View.VISIBLE);
                    cardAiSuggestion.setVisibility(View.GONE);
                    tvAnalysisError.setVisibility(View.GONE);
                }
            });

    private final ActivityResultLauncher<String> cameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    launchCamera();
                } else {
                    Toast.makeText(requireContext(), "Camera permission is needed to take evidence photos", Toast.LENGTH_SHORT).show();
                }
            });

    public static CreateReportFragment newInstance(String category) {
        return newInstance(category, null);
    }

    public static CreateReportFragment newInstance(String category, String initialTitle) {
        CreateReportFragment fragment = new CreateReportFragment();
        Bundle args = new Bundle();
        if (category != null) args.putString(ARG_CATEGORY, category);
        if (initialTitle != null) args.putString(ARG_TITLE, initialTitle);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_report, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        repository = new ReportRepository(requireContext());
        session    = new SessionManager(requireContext());

        bindViews(view);
        setupCategoryChips();
        setupPhotoButtons();
        setupAnalysisActions();
        setupPinLookup();
        setupGpsButton();
        setupSubmitButton();

        // Check if preselected category or title was passed
        if (getArguments() != null) {
            String initialCat = getArguments().getString(ARG_CATEGORY);
            if (initialCat != null && !initialCat.isEmpty()) selectCategory(initialCat);
            String initialTitle = getArguments().getString(ARG_TITLE);
            if (initialTitle != null && !initialTitle.isEmpty()) {
                etTitle.setText(initialTitle);
            }
        }
    }

    private void bindViews(View v) {
        chipWaste          = v.findViewById(R.id.chip_waste);
        chipWater          = v.findViewById(R.id.chip_water);
        chipAir            = v.findViewById(R.id.chip_air);
        chipGreen          = v.findViewById(R.id.chip_green);
        chipOther          = v.findViewById(R.id.chip_other);
        tvCategoryError    = v.findViewById(R.id.tv_category_error);

        tilTitle           = v.findViewById(R.id.til_title);
        etTitle            = v.findViewById(R.id.et_title);
        tilDescription     = v.findViewById(R.id.til_description);
        etDescription      = v.findViewById(R.id.et_description);

        btnCamera          = v.findViewById(R.id.btn_camera);
        btnGallery         = v.findViewById(R.id.btn_gallery);
        imgPreview         = v.findViewById(R.id.img_preview);
        cardPhotoPreview   = v.findViewById(R.id.card_photo_preview);
        btnAnalyseImage    = v.findViewById(R.id.btn_analyse_image);
        btnRemovePhoto     = v.findViewById(R.id.btn_remove_photo);
        layoutAnalysisLoading = v.findViewById(R.id.layout_analysis_loading);
        tvAnalysisError    = v.findViewById(R.id.tv_analysis_error);
        cardAiSuggestion   = v.findViewById(R.id.card_ai_suggestion);
        tvAiTags           = v.findViewById(R.id.tv_ai_tags);
        tvAiCategory       = v.findViewById(R.id.tv_ai_category);
        tvAiTitle          = v.findViewById(R.id.tv_ai_title);
        tvAiDescription    = v.findViewById(R.id.tv_ai_description);
        btnApplyAiSuggestion = v.findViewById(R.id.btn_apply_ai_suggestion);

        tilPin             = v.findViewById(R.id.til_pin);
        etPin              = v.findViewById(R.id.et_pin);
        cardLocationResult = v.findViewById(R.id.card_location_result);
        tvLocationResult   = v.findViewById(R.id.tv_location_result);
        progressPin        = v.findViewById(R.id.progress_pin);

        etLocality         = v.findViewById(R.id.et_locality);
        etCity             = v.findViewById(R.id.et_city);
        etDistrict         = v.findViewById(R.id.et_district);
        btnUseGps          = v.findViewById(R.id.btn_use_gps);

        btnSubmit          = v.findViewById(R.id.btn_submit_report);
        progressSubmit     = v.findViewById(R.id.progress_submit);
    }

    private void setupCategoryChips() {
        chipWaste.setOnClickListener(v -> selectCategory("WASTE"));
        chipWater.setOnClickListener(v -> selectCategory("WATER"));
        chipAir.setOnClickListener(v -> selectCategory("AIR"));
        chipGreen.setOnClickListener(v -> selectCategory("GREEN_SPACES"));
        chipOther.setOnClickListener(v -> selectCategory("OTHER"));
    }

    private void selectCategory(String cat) {
        selectedCategory = cat;
        tvCategoryError.setVisibility(View.GONE);

        chipWaste.setChecked("WASTE".equals(cat));
        chipWater.setChecked("WATER".equals(cat));
        chipAir.setChecked("AIR".equals(cat));
        chipGreen.setChecked("GREEN_SPACES".equals(cat));
        chipOther.setChecked("OTHER".equals(cat));
    }

    private void setupPhotoButtons() {
        btnCamera.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {
                launchCamera();
            } else {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
            }
        });

        btnGallery.setOnClickListener(v -> galleryLauncher.launch("image/*"));
    }

    private void launchCamera() {
        try {
            File photoFile = new File(requireContext().getCacheDir(), "evidence_" + System.currentTimeMillis() + ".jpg");
            currentPhotoUri = FileProvider.getUriForFile(
                    requireContext(),
                    requireContext().getPackageName() + ".fileprovider",
                    photoFile);
            takePictureLauncher.launch(currentPhotoUri);
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Camera could not be launched: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void setupAnalysisActions() {
        btnRemovePhoto.setOnClickListener(v -> {
            currentPhotoUri = null;
            capturedPhotoPath = "";
            currentAnalysisResult = null;
            imgPreview.setImageDrawable(null);
            cardPhotoPreview.setVisibility(View.GONE);
            cardAiSuggestion.setVisibility(View.GONE);
            tvAnalysisError.setVisibility(View.GONE);
            layoutAnalysisLoading.setVisibility(View.GONE);
        });

        btnAnalyseImage.setOnClickListener(v -> {
            if (currentPhotoUri == null) {
                Toast.makeText(requireContext(), "Please capture or select an image first", Toast.LENGTH_SHORT).show();
                return;
            }
            layoutAnalysisLoading.setVisibility(View.VISIBLE);
            cardAiSuggestion.setVisibility(View.GONE);
            tvAnalysisError.setVisibility(View.GONE);
            btnAnalyseImage.setEnabled(false);

            ImageAnalysisHelper.analyzeUri(requireContext(), currentPhotoUri, new ImageAnalysisHelper.Callback() {
                @Override
                public void onSuccess(@NonNull ImageAnalysisHelper.AnalysisResult result) {
                    if (!isAdded()) return;
                    requireActivity().runOnUiThread(() -> {
                        layoutAnalysisLoading.setVisibility(View.GONE);
                        btnAnalyseImage.setEnabled(true);
                        currentAnalysisResult = result;
                        displayAnalysisResult(result);
                    });
                }

                @Override
                public void onError(@NonNull String errorMessage) {
                    if (!isAdded()) return;
                    requireActivity().runOnUiThread(() -> {
                        layoutAnalysisLoading.setVisibility(View.GONE);
                        btnAnalyseImage.setEnabled(true);
                        tvAnalysisError.setVisibility(View.VISIBLE);
                        tvAnalysisError.setText(errorMessage);
                    });
                }
            });
        });

        btnApplyAiSuggestion.setOnClickListener(v -> {
            if (currentAnalysisResult == null) return;
            if (currentAnalysisResult.category != null && !currentAnalysisResult.category.isEmpty()) {
                selectCategory(currentAnalysisResult.category);
            }
            if (currentAnalysisResult.suggestedTitle != null) {
                etTitle.setText(currentAnalysisResult.suggestedTitle);
            }
            if (currentAnalysisResult.suggestedDescription != null) {
                etDescription.setText(currentAnalysisResult.suggestedDescription);
            }
            Toast.makeText(requireContext(), "AI suggestions applied. Please review and edit before submitting.", Toast.LENGTH_LONG).show();
            tilTitle.requestFocus();
        });
    }

    private void displayAnalysisResult(ImageAnalysisHelper.AnalysisResult result) {
        cardAiSuggestion.setVisibility(View.VISIBLE);
        String tags = (result.detectedLabels == null || result.detectedLabels.isEmpty())
                ? "No specific visual tags"
                : String.join(", ", result.detectedLabels);
        tvAiTags.setText("Visual cues: " + tags);
        tvAiCategory.setText("Suggested Category: " + getCategoryDisplayName(result.category));
        tvAiTitle.setText("Title: " + result.suggestedTitle);
        tvAiDescription.setText("Description: " + result.suggestedDescription);
    }

    private String getCategoryDisplayName(String cat) {
        if ("WASTE".equals(cat)) return "Waste";
        if ("WATER".equals(cat)) return "Water";
        if ("AIR".equals(cat)) return "Air";
        if ("GREEN_SPACES".equals(cat)) return "Green Spaces";
        return "Other";
    }

    private void setupPinLookup() {
        etPin.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null && s.length() == 6) {
                    performPinLookup(s.toString());
                } else {
                    cardLocationResult.setVisibility(View.GONE);
                }
            }
        });
    }

    private void performPinLookup(String pin) {
        progressPin.setVisibility(View.VISIBLE);
        cardLocationResult.setVisibility(View.VISIBLE);
        tvLocationResult.setText("Searching location for PIN " + pin + "...");

        PinCodeHelper.lookup(pin, new PinCodeHelper.Callback() {
            @Override
            public void onResult(PinCodeHelper.PinLocation location) {
                if (!isAdded()) return;
                progressPin.setVisibility(View.GONE);
                tvLocationResult.setText(location.district + ", " + location.city + " (" + location.state + ")");
                if (etCity.getText() == null || etCity.getText().toString().isEmpty()) {
                    etCity.setText(location.city);
                }
                if (etDistrict.getText() == null || etDistrict.getText().toString().isEmpty()) {
                    etDistrict.setText(location.district);
                }
            }

            @Override
            public void onError(String error) {
                if (!isAdded()) return;
                progressPin.setVisibility(View.GONE);
                tvLocationResult.setText("Location not found automatically. Please enter below.");
            }
        });
    }

    private void setupGpsButton() {
        btnUseGps.setOnClickListener(v -> {
            // Demo location prefill for Maharashtra
            etPin.setText("411001");
            etLocality.setText("Shivajinagar");
            etCity.setText("Pune");
            etDistrict.setText("Pune");
            Toast.makeText(requireContext(), "GPS location set: Pune, Maharashtra", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupSubmitButton() {
        btnSubmit.setOnClickListener(v -> validateAndSubmit());
    }

    private void validateAndSubmit() {
        boolean valid = true;

        if (selectedCategory.isEmpty()) {
            tvCategoryError.setVisibility(View.VISIBLE);
            valid = false;
        } else {
            tvCategoryError.setVisibility(View.GONE);
        }

        String title = etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
        if (title.isEmpty()) {
            tilTitle.setError("Title is required");
            valid = false;
        } else {
            tilTitle.setError(null);
        }

        String desc = etDescription.getText() != null ? etDescription.getText().toString().trim() : "";
        if (desc.isEmpty()) {
            tilDescription.setError("Description is required");
            valid = false;
        } else {
            tilDescription.setError(null);
        }

        String pin = etPin.getText() != null ? etPin.getText().toString().trim() : "";
        if (pin.length() != 6) {
            tilPin.setError("Enter 6-digit PIN code");
            valid = false;
        } else {
            tilPin.setError(null);
        }

        if (!valid) return;

        btnSubmit.setEnabled(false);
        progressSubmit.setVisibility(View.VISIBLE);

        String locality = etLocality.getText() != null ? etLocality.getText().toString().trim() : "";
        String city = etCity.getText() != null ? etCity.getText().toString().trim() : "Maharashtra";
        String district = etDistrict.getText() != null ? etDistrict.getText().toString().trim() : "Maharashtra";

        ReportEntity report = new ReportEntity();
        report.reportId    = AppUtils.generateReportId();
        report.authorUserId = session.getUserId() != -1 ? session.getUserId() : 1;
        report.authorName  = session.getUserName();
        report.title       = title;
        report.description = desc;
        report.category    = selectedCategory;
        report.status      = "REPORTED";
        report.priority    = "HIGH";
        report.photoUris   = capturedPhotoPath;
        report.pinCode     = pin;
        report.locality    = locality;
        report.city        = city;
        report.district    = district;
        report.state       = "Maharashtra";
        report.latitude    = 18.5204;
        report.longitude   = 73.8567;
        report.isPublic    = true;
        report.upvotes     = 1;

        repository.submitReport(report, () -> {
            if (!isAdded()) return;
            requireActivity().runOnUiThread(() -> {
                progressSubmit.setVisibility(View.GONE);
                btnSubmit.setEnabled(true);

                showSuccessDialog(report.reportId);
            });
        }, () -> {
            if (!isAdded()) return;
            requireActivity().runOnUiThread(() -> {
                progressSubmit.setVisibility(View.GONE);
                btnSubmit.setEnabled(true);
                Toast.makeText(requireContext(), "Failed to submit report. Please retry.", Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void showSuccessDialog(String reportId) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Report Submitted Successfully")
                .setMessage("Your environmental report has been registered.\n\nTracking ID:\n" + reportId + "\n\nAuthorities will be notified for verification and triage.")
                .setPositiveButton("View in My Reports", (dialog, which) -> {
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).setSelectedTab(R.id.nav_my_reports);
                    } else if (getActivity() != null) {
                        getActivity().getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, new MyReportsFragment())
                                .commit();
                    }
                })
                .setNegativeButton("Submit Another", (dialog, which) -> {
                    // Reset form
                    etTitle.setText("");
                    etDescription.setText("");
                    imgPreview.setImageDrawable(null);
                    cardPhotoPreview.setVisibility(View.GONE);
                    cardAiSuggestion.setVisibility(View.GONE);
                    tvAnalysisError.setVisibility(View.GONE);
                    capturedPhotoPath = "";
                    currentPhotoUri = null;
                    currentAnalysisResult = null;
                })
                .setCancelable(false)
                .show();
    }
}
