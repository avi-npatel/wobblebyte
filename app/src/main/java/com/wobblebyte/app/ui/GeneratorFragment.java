package com.wobblebyte.app.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.slider.Slider;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.wobblebyte.app.R;
import com.wobblebyte.app.auth.VaultAccess;
import com.wobblebyte.app.data.Vault;
import com.wobblebyte.app.python.GeneratedPassword;
import com.wobblebyte.app.python.GeneratorOptions;
import com.wobblebyte.app.python.PasswordBridge;

/** Generate screen: pick the options, get a password from Python, copy it or save it. */
public class GeneratorFragment extends Fragment implements ShakeDetector.Listener {

    private TextView passwordText;
    private TextView strengthText;
    private TextView latencyText;
    private TextView lengthLabel;
    private Slider lengthSlider;
    private CheckBox lowerBox;
    private CheckBox upperBox;
    private CheckBox digitsBox;
    private CheckBox symbolsBox;
    private CheckBox ambiguousBox;
    private TextInputLayout siteLayout;
    private TextInputEditText siteInput;
    private TextInputEditText usernameInput;
    private ShakeDetector shakeDetector;

    private String currentPassword = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_generator, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        passwordText = view.findViewById(R.id.password_text);
        strengthText = view.findViewById(R.id.strength_text);
        latencyText = view.findViewById(R.id.latency_text);
        lengthLabel = view.findViewById(R.id.length_label);
        lengthSlider = view.findViewById(R.id.length_slider);
        lowerBox = view.findViewById(R.id.check_lower);
        upperBox = view.findViewById(R.id.check_upper);
        digitsBox = view.findViewById(R.id.check_digits);
        symbolsBox = view.findViewById(R.id.check_symbols);
        ambiguousBox = view.findViewById(R.id.check_ambiguous);
        siteLayout = view.findViewById(R.id.site_layout);
        siteInput = view.findViewById(R.id.site_input);
        usernameInput = view.findViewById(R.id.username_input);

        updateLengthLabel();
        lengthSlider.addOnChangeListener((slider, value, fromUser) -> updateLengthLabel());

        view.findViewById(R.id.button_generate).setOnClickListener(v -> generate());
        view.findViewById(R.id.button_copy).setOnClickListener(v -> copy());
        view.findViewById(R.id.button_save).setOnClickListener(v -> save());

        shakeDetector = new ShakeDetector(requireContext(), this);
        generate();
    }

    @Override
    public void onResume() {
        super.onResume();
        shakeDetector.start();
    }

    @Override
    public void onPause() {
        shakeDetector.stop();
        super.onPause();
    }

    @Override
    public void onShake() {
        generate();
    }

    private void updateLengthLabel() {
        lengthLabel.setText(getString(R.string.length_label, (int) lengthSlider.getValue()));
    }

    private GeneratorOptions readOptions() {
        GeneratorOptions options = new GeneratorOptions();
        options.length = (int) lengthSlider.getValue();
        options.lower = lowerBox.isChecked();
        options.upper = upperBox.isChecked();
        options.digits = digitsBox.isChecked();
        options.symbols = symbolsBox.isChecked();
        options.excludeAmbiguous = ambiguousBox.isChecked();
        return options;
    }

    private void generate() {
        GeneratorOptions options = readOptions();
        if (!options.hasAnyCharacterType()) {
            showMessage(getString(R.string.error_pick_type));
            return;
        }

        try {
            GeneratedPassword result = PasswordBridge.generate(options);
            currentPassword = result.password;
            passwordText.setText(result.password);
            strengthText.setText(getString(
                    R.string.strength_line, result.strength, Math.round(result.entropyBits)));
            latencyText.setText(getString(R.string.latency_line, result.roundTripMs));
        } catch (RuntimeException e) {
            showMessage(getString(R.string.error_generate, e.getMessage()));
        }
    }

    private void copy() {
        if (currentPassword.isEmpty()) {
            return;
        }
        ClipboardHelper.copy(requireContext(), getString(R.string.app_name), currentPassword);
        showMessage(getString(R.string.copied_clear));
    }

    private void save() {
        String site = textOf(siteInput);
        String username = textOf(usernameInput);
        if (site.isEmpty()) {
            siteLayout.setError(getString(R.string.error_site_required));
            return;
        }
        siteLayout.setError(null);
        if (currentPassword.isEmpty()) {
            return;
        }

        final String password = currentPassword;
        VaultAccess.withUnlock(requireActivity(), () -> {
            Vault.get(requireContext()).add(site, username, password);
            siteInput.setText("");
            usernameInput.setText("");
            showMessage(getString(R.string.saved_ok));
        }, this::showMessage);
    }

    private static String textOf(TextInputEditText input) {
        return input.getText() == null ? "" : input.getText().toString().trim();
    }

    private void showMessage(String message) {
        View view = getView();
        if (view != null) {
            Snackbar.make(view, message, Snackbar.LENGTH_LONG).show();
        }
    }
}
