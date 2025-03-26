package com.wobblebyte.app.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.wobblebyte.app.R;
import com.wobblebyte.app.data.Credential;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/** Lists saved sites. Usernames and passwords stay sealed until a row is opened. */
final class CredentialAdapter extends RecyclerView.Adapter<CredentialAdapter.Holder> {

    interface OnClick {
        void onClick(Credential credential);
    }

    private final List<Credential> items = new ArrayList<>();
    private final OnClick onClick;

    CredentialAdapter(OnClick onClick) {
        this.onClick = onClick;
    }

    void submit(List<Credential> credentials) {
        items.clear();
        items.addAll(credentials);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View row = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_credential, parent, false);
        return new Holder(row);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Credential credential = items.get(position);
        String date = DateFormat.getDateInstance(DateFormat.MEDIUM)
                .format(new Date(credential.createdAt));

        holder.site.setText(credential.site);
        holder.saved.setText(holder.itemView.getContext().getString(R.string.entry_saved_on, date));
        holder.itemView.setOnClickListener(v -> onClick.onClick(credential));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static final class Holder extends RecyclerView.ViewHolder {
        final TextView site;
        final TextView saved;

        Holder(@NonNull View itemView) {
            super(itemView);
            site = itemView.findViewById(R.id.credential_site);
            saved = itemView.findViewById(R.id.credential_saved);
        }
    }
}
