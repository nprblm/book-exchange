package ua.nprblm.bookexchange.UI.profile;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import ua.nprblm.bookexchange.Avtorizate.LoginActivity;
import ua.nprblm.bookexchange.Models.Users;
import ua.nprblm.bookexchange.R;
import ua.nprblm.bookexchange.UI.profile.settings.ProfileSettingsFragment;
import ua.nprblm.bookexchange.databinding.FragmentProfileBinding;


public class ProfileFragment extends Fragment {

    private final String parentDBName = "Users";

    private FragmentProfileBinding binding;

    private TextView nameTextView;
    private TextView phoneNumberTextView;
    private TextView tgTextView;
    private TextView fbTextView;

    private LinearLayout tgLayout;
    private LinearLayout fbLayout;

    private ShapeableImageView profileImage;

    private Button settingsButton;
    private Button signOutButton;

    @SuppressLint("ResourceType")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        new ViewModelProvider(this).get(ProfileViewModel.class);

        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        init(root);
        getData(savedInstanceState);

        settingsButton.setOnClickListener(v -> {

            Fragment newFragment = new ProfileSettingsFragment();
            assert getFragmentManager() != null;
            FragmentTransaction transaction = getFragmentManager().beginTransaction();

            transaction.replace(R.id.fragment_profile, newFragment);
            transaction.addToBackStack(null);

            transaction.commit();
        });

        signOutButton.setOnClickListener(v -> {
            Intent mainIntent = new Intent(getContext(), LoginActivity.class);
            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(mainIntent);
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void getData(Bundle savedInstanceState)
    {
        final String[] number = new String[1];
        final DatabaseReference RootRef;
        RootRef = FirebaseDatabase.getInstance("https://book-exchange-4777a-default-rtdb.europe-west1.firebasedatabase.app").getReference();

        RootRef.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if (savedInstanceState == null)
                {
                    Bundle extras = requireActivity().getIntent().getExtras();
                    if (extras == null)
                    {
                        number[0] = null;
                    } else
                        {
                        number[0] = extras.getString("number");
                    }
                }
                else
                    {
                    number[0] = (String) savedInstanceState.getSerializable("number");
                }
                Users usersData = snapshot.child(parentDBName).child(number[0]).getValue(Users.class);
                assert usersData != null;
                if(usersData.getTg()==null)
                {
                    tgLayout.setVisibility(View.GONE);
                }
                if(usersData.getFb()==null)
                {
                    fbLayout.setVisibility(View.GONE);
                }

                setData(usersData.getUsername(), usersData.getNumber(), usersData.getImage(), usersData.getTg(), usersData.getFb());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void setData(String name, String number, String profileAvatar, String telegram, String facebook)
    {
        nameTextView.setText(name);
        phoneNumberTextView.setText("+38"+number);
        tgTextView.setText(telegram==null?"":telegram);
        fbTextView.setText(facebook==null?"":facebook);

        Picasso.get().load(profileAvatar).into(profileImage);

    }

    private void init(View root)
    {
        nameTextView = root.findViewById(R.id.name_text);
        phoneNumberTextView = root.findViewById(R.id.phone_number_text);
        settingsButton = root.findViewById(R.id.settings_button);
        profileImage = root.findViewById(R.id.profile_image);
        signOutButton = root.findViewById(R.id.exit_button);

        tgTextView = root.findViewById(R.id.tg_text);
        fbTextView = root.findViewById(R.id.fb_text);

        tgLayout = root.findViewById(R.id.tg_layout);
        fbLayout = root.findViewById(R.id.fb_layout);

    }
}