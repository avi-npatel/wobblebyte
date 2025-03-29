package com.wobblebyte.app.ui;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.wobblebyte.app.R;

/** Learn tab: links to outside guides and tools, opened in the browser. */
public class LearnFragment extends Fragment {

    private static final class Resource {
        final int title;
        final int description;
        final String url;

        Resource(@StringRes int title, @StringRes int description, String url) {
            this.title = title;
            this.description = description;
            this.url = url;
        }
    }

    private static final Resource[] RESOURCES = {
            new Resource(R.string.learn_bitwarden_title, R.string.learn_bitwarden_desc,
                    "https://bitwarden.com/password-strength/"),
            new Resource(R.string.learn_hibp_title, R.string.learn_hibp_desc,
                    "https://haveibeenpwned.com/"),
            new Resource(R.string.learn_cisa_title, R.string.learn_cisa_desc,
                    "https://www.cisa.gov/secure-our-world"),
            new Resource(R.string.learn_ftc_title, R.string.learn_ftc_desc,
                    "https://consumer.ftc.gov/articles/how-recognize-and-avoid-phishing-scams"),
            new Resource(R.string.learn_eff_title, R.string.learn_eff_desc,
                    "https://ssd.eff.org/"),
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_learn, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LinearLayout list = view.findViewById(R.id.learn_list);
        LayoutInflater inflater = LayoutInflater.from(requireContext());

        for (Resource resource : RESOURCES) {
            View row = inflater.inflate(R.layout.item_resource, list, false);
            ((TextView) row.findViewById(R.id.resource_title)).setText(resource.title);
            ((TextView) row.findViewById(R.id.resource_description)).setText(resource.description);
            row.setOnClickListener(v -> open(resource.url));
            list.addView(row);
        }
    }

    private void open(String url) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (ActivityNotFoundException e) {
            View view = getView();
            if (view != null) {
                Snackbar.make(view, R.string.learn_open_error, Snackbar.LENGTH_LONG).show();
            }
        }
    }
}
