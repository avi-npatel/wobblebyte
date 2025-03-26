package com.wobblebyte.app.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.wobblebyte.app.R;
import com.wobblebyte.app.auth.VaultAccess;
import com.wobblebyte.app.data.Credential;
import com.wobblebyte.app.data.Vault;

import java.util.List;

/** Vault screen: saved sites, with the biometric prompt guarding every reveal. */
public class VaultFragment extends Fragment {

    private CredentialAdapter adapter;
    private TextView countText;
    private TextView emptyText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_vault, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        countText = view.findViewById(R.id.vault_count);
        emptyText = view.findViewById(R.id.vault_empty);

        adapter = new CredentialAdapter(this::open);
        RecyclerView list = view.findViewById(R.id.credential_list);
        list.setLayoutManager(new LinearLayoutManager(requireContext()));
        list.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    private void refresh() {
        List<Credential> credentials = Vault.get(requireContext()).list();
        adapter.submit(credentials);
        emptyText.setVisibility(credentials.isEmpty() ? View.VISIBLE : View.GONE);
        countText.setText(getResources().getQuantityString(
                R.plurals.saved_count, credentials.size(), credentials.size()));
    }

    private void open(Credential credential) {
        VaultAccess.withUnlock(requireActivity(), () -> {
            Vault.Entry entry = Vault.get(requireContext()).reveal(credential);
            showEntry(credential, entry);
        }, this::showMessage);
    }

    private void showEntry(Credential credential, Vault.Entry entry) {
        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(credential.site)
                .setMessage(getString(R.string.entry_message, entry.username, entry.password))
                .setPositiveButton(R.string.copy_password, (d, which) -> {
                    ClipboardHelper.copy(requireContext(), credential.site, entry.password);
                    showMessage(getString(R.string.copied_clear));
                })
                .setNeutralButton(R.string.delete, (d, which) -> confirmDelete(credential))
                .setNegativeButton(R.string.close, null)
                .create();

        // Dialogs are separate windows, so the activity's FLAG_SECURE doesn't cover them.
        if (dialog.getWindow() != null) {
            dialog.getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_SECURE,
                    WindowManager.LayoutParams.FLAG_SECURE);
        }
        dialog.show();
    }

    private void confirmDelete(Credential credential) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.delete_title)
                .setMessage(getString(R.string.delete_message, credential.site))
                .setPositiveButton(R.string.delete, (d, which) -> {
                    Vault.get(requireContext()).delete(credential.id);
                    refresh();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showMessage(String message) {
        View view = getView();
        if (view != null) {
            Snackbar.make(view, message, Snackbar.LENGTH_LONG).show();
        }
    }
}
