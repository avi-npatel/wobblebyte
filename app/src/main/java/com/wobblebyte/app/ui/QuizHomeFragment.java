package com.wobblebyte.app.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.wobblebyte.app.R;
import com.wobblebyte.app.quiz.QuizModule;
import com.wobblebyte.app.quiz.QuizRepository;
import com.wobblebyte.app.quiz.ScoreStats;
import com.wobblebyte.app.quiz.ScoreStore;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** Quiz tab: the list of modules with each one's score history. */
public class QuizHomeFragment extends Fragment {

    private final List<QuizModule> modules = new ArrayList<>();
    private ScoreStore scores;
    private ModuleAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_quiz_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        scores = new ScoreStore(requireContext());
        adapter = new ModuleAdapter();
        RecyclerView list = view.findViewById(R.id.module_list);
        list.setLayoutManager(new LinearLayoutManager(requireContext()));
        list.setAdapter(adapter);

        try {
            modules.clear();
            modules.addAll(QuizRepository.load(requireContext()));
        } catch (IOException | JSONException e) {
            TextView error = view.findViewById(R.id.quiz_error);
            error.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Scores change when a quiz finishes, so redraw the rows on return.
        adapter.notifyDataSetChanged();
    }

    private void start(QuizModule module) {
        Intent intent = new Intent(requireContext(), QuizActivity.class);
        intent.putExtra(QuizActivity.EXTRA_MODULE_ID, module.id);
        startActivity(intent);
    }

    private final class ModuleAdapter extends RecyclerView.Adapter<ModuleAdapter.Holder> {

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View row = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_quiz_module, parent, false);
            return new Holder(row);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            QuizModule module = modules.get(position);
            Context context = holder.itemView.getContext();
            ScoreStats.Summary summary = ScoreStats.summarize(scores.forModule(module.id));

            holder.title.setText(module.title);
            holder.summary.setText(module.summary);
            holder.count.setText(context.getString(R.string.quiz_question_count, module.questions.size()));
            holder.score.setText(ScoreText.history(context, summary));
            holder.itemView.setOnClickListener(v -> start(module));
        }

        @Override
        public int getItemCount() {
            return modules.size();
        }

        final class Holder extends RecyclerView.ViewHolder {
            final TextView title;
            final TextView summary;
            final TextView count;
            final TextView score;

            Holder(@NonNull View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.module_title);
                summary = itemView.findViewById(R.id.module_summary);
                count = itemView.findViewById(R.id.module_count);
                score = itemView.findViewById(R.id.module_score);
            }
        }
    }
}
