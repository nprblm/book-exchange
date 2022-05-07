package ua.nprblm.bookexchange.UI.like;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import ua.nprblm.bookexchange.R;
import ua.nprblm.bookexchange.databinding.FragmentLikeBinding;

public class LikeFragment extends Fragment {

    private LikeViewModel likeViewModel;
    private FragmentLikeBinding binding;

    private AutoCompleteTextView cityChange;

    private final String[] mCats = new String[]{ "Kyiv", "Lviv", "Harkiv", "Ushhorod"};

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        likeViewModel =
                new ViewModelProvider(this).get(LikeViewModel.class);

        binding = FragmentLikeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        cityChange = root.findViewById(R.id.city_edit_text);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_dropdown_item_1line, mCats);
        cityChange.setThreshold(1);
        cityChange.setAdapter(adapter);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}