package ua.nprblm.bookexchange.UI.profile.settings;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Objects;
import java.util.regex.Pattern;

import ua.nprblm.bookexchange.Models.Users;
import ua.nprblm.bookexchange.R;
import ua.nprblm.bookexchange.UI.profile.ProfileFragment;
import ua.nprblm.bookexchange.databinding.FragmentProfileSettingsBinding;

public class ProfileSettingsFragment extends Fragment {

    private final String parentDBName = "Users";
    private static final int GALLERY_PICK = 1;
    private static final int RESULT_OK = -1;

    private ShapeableImageView profileImage;
    private Uri imageUri;
    private StorageReference imageRef;
    private String downloadImageUrl;

    private TextView errorMessage;
    private TextView passwordErrorMessage;
    private TextView numberTextView;

    private EditText nameEditText;
    private EditText passwordEditText;
    private EditText newPasswordEditText;
    private EditText confirmNewPasswordEditText;

    private EditText telegramEditText;
    private EditText facebookEditText;

    private Button changeNameButton;
    private Button changePasswordButton;
    private Button changePhotoButton;

    private boolean isNameValid;
    private boolean isTgValid;
    private boolean isFbValid;

    private final Users user = new Users();

    private FragmentProfileSettingsBinding binding;

    private ProgressDialog loadingBar;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        new ViewModelProvider(this).get(ProfileSettingsViewModel.class);

        binding = FragmentProfileSettingsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        getUser(savedInstanceState);

        init(root);

        confirmNewPasswordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!confirmNewPasswordEditText.getText().toString().equals("")&&!newPasswordEditText.getText().toString().equals(""))
                    passwordErrorMessage.setText(confirmNewPasswordEditText.getText().toString().equals(newPasswordEditText.getText().toString())?"":"Passwords is not equals!");
                else passwordErrorMessage.setText("");
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(!confirmNewPasswordEditText.getText().toString().equals("")&&!newPasswordEditText.getText().toString().equals(""))
                    passwordErrorMessage.setText(confirmNewPasswordEditText.getText().toString().equals(newPasswordEditText.getText().toString())?"":"Passwords is not equals!");
                else passwordErrorMessage.setText("");
            }
        });

        changeNameButton.setOnClickListener(v -> {
            if(isInfoValid())
            {
                startLoadingBar();
                user.setUsername(nameEditText.getText().toString());
                user.setTg(telegramEditText.getText().toString());
                user.setFb(facebookEditText.getText().toString());
                loadDataIntoDB(user.getNumber(), user.getUsername(), user.getPassword(), user.getImage(), user.getTg(), user.getFb(), true);
            }
            else
            {
                String messageError = "";
                if(!isNameValid) messageError+="Name is not valid\n";
                if(!isTgValid) messageError+="Telegram link is not valid\n";
                if(!isFbValid) messageError+="Facebook link is not valid\n";
                errorMessage.setText(messageError);
            }
        });

        changePasswordButton.setOnClickListener(v -> {
            if(isPasswordInfoValid()&&passwordEditText.getText().toString().equals(user.getPassword()))
            {
                startLoadingBar();
                user.setPassword(newPasswordEditText.getText().toString());
                loadDataIntoDB(user.getNumber(), user.getUsername(), user.getPassword(), user.getImage(), user.getTg(), user.getFb(), true);
            }
            else
            {
                String messageError = "";
                if(!isPasswordInfoValid()) messageError += "New password is not valid\n";
                if(!passwordEditText.getText().toString().equals(user.getPassword())) messageError += "Password is wrong\n";
                passwordErrorMessage.setText(messageError);
            }
        });

        changePhotoButton.setOnClickListener(v -> openGallery());

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void openGallery() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,GALLERY_PICK);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_PICK && resultCode == RESULT_OK && data!=null)
        {
            startLoadingBar();
            imageUri = data.getData();
            profileImage.setImageURI(imageUri);
            downloadImage(user.getNumber());
        }
    }

    private void downloadImage(String phoneNumber)
    {
        StorageReference filePath = imageRef.child(phoneNumber + ".jpeg");
        final UploadTask uploadTask = filePath.putFile(imageUri);

        uploadTask.addOnFailureListener(e -> {
            String message = e.toString();
            Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
            loadingBar.dismiss();
        }).addOnSuccessListener(taskSnapshot -> uploadTask.continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw Objects.requireNonNull(task.getException());
            } else {
                downloadImageUrl = filePath.getDownloadUrl().toString();
                return filePath.getDownloadUrl();
            }
        }).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Uri downloadUrl = task.getResult();
                downloadImageUrl = String.valueOf(downloadUrl);
                user.setImage(downloadImageUrl);
                loadDataIntoDB(user.getNumber(), user.getUsername(), user.getPassword(), user.getImage(), user.getTg(), user.getFb(), false);
            }
        }));
    }

    private void getUser(Bundle savedInstanceState)
    {
        final Users[] usersData = new Users[1];
        final String[] numberId = new String[1];
        final DatabaseReference RootRef;
        RootRef = FirebaseDatabase.getInstance("https://book-exchange-4777a-default-rtdb.europe-west1.firebasedatabase.app").getReference();

        RootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (savedInstanceState == null) {
                    @SuppressLint("UseRequireInsteadOfGet") Bundle extras = Objects.requireNonNull(getActivity()).getIntent().getExtras();
                    if (extras == null) {
                        numberId[0] = null;
                    } else {
                        numberId[0] = extras.getString("number");
                    }
                } else {
                    numberId[0] = (String) savedInstanceState.getSerializable("number");
                }
                usersData[0] = snapshot.child(parentDBName).child(numberId[0]).getValue(Users.class);
                assert usersData[0] != null;
                setData(usersData[0].getUsername(), usersData[0].getNumber(), usersData[0].getImage(), usersData[0].getTg(), usersData[0].getFb());
                user.setUsername(usersData[0].getUsername());
                user.setNumber(usersData[0].getNumber());
                user.setImage(usersData[0].getImage());
                user.setPassword(usersData[0].getPassword());
                user.setTg(usersData[0].getTg());
                user.setFb(usersData[0].getFb());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void setData(String name, String number, String imageLink, String telegram, String facebook)
    {
        nameEditText.setText(name);
        numberTextView.setText("+38"+number);
        telegramEditText.setText(telegram==null?"":telegram);
        facebookEditText.setText(facebook==null?"":facebook);
        Picasso.get().load(imageLink).into(profileImage);
    }

    private boolean isInfoValid()
    {
        Pattern tgPattern = Pattern.compile("^([a-zA-Z]|[0-9]|_){5}([a-zA-Z]|[0-9]|_)*");
        Pattern fbPattern = Pattern.compile("^([a-zA-Z]|[0-9]|\\.){5}([a-zA-Z]|[0-9]|\\.)*");
        isNameValid = (nameEditText.getText().toString().length() > 0);
        if(telegramEditText.getText().toString().equals(""))
        {
            isTgValid = true;
        }
        else {
            isTgValid = (tgPattern.matcher(telegramEditText.getText().toString()).matches()||telegramEditText.getText().toString().equals(""));
        }
        if(facebookEditText.getText().toString().equals(""))
        {
            isFbValid = true;
        }
        else {
            isFbValid = (fbPattern.matcher(facebookEditText.getText().toString()).matches()||facebookEditText.getText().toString().equals(""));
        }
        return(isNameValid&&isTgValid&&isFbValid);
    }

    private boolean isPasswordInfoValid()
    {
        return(((newPasswordEditText.getText().toString().equals(confirmNewPasswordEditText.getText().toString())) && (!newPasswordEditText.getText().toString().equals("")) && newPasswordEditText.getText().toString().length() >= 5));
    }

    private void loadDataIntoDB(String phoneNumber, String username, String pass, String profileImg, String telegram, String facebook, boolean changeFragment)
    {
        final DatabaseReference RootRef;
        RootRef = FirebaseDatabase.getInstance("https://book-exchange-4777a-default-rtdb.europe-west1.firebasedatabase.app").getReference();

        RootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                HashMap<String, Object> userDataMap = new HashMap<>();
                userDataMap.put("number", phoneNumber);
                userDataMap.put("username", username);
                userDataMap.put("password", pass);
                userDataMap.put("image", profileImg);
                if(telegram != null && !telegram.equals(""))
                    userDataMap.put("tg", telegram);
                if(facebook != null && !facebook.equals(""))
                    userDataMap.put("fb", facebook);

                RootRef.child("Users").child(phoneNumber).updateChildren(userDataMap).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        loadingBar.dismiss();
                        Toast.makeText(getActivity(), "Info changes is successful...", Toast.LENGTH_SHORT).show();
                        if(changeFragment)
                        {
                            try {
                                Fragment newFragment = new ProfileFragment();
                                assert getFragmentManager() != null;
                                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                                Thread.sleep(100);

                                transaction.replace(R.id.fragment_profile_settings, newFragment);
                                transaction.addToBackStack(null);
                                transaction.commit();
                            }
                            catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    else {
                        loadingBar.dismiss();
                        Toast.makeText(getActivity(), "Error", Toast.LENGTH_SHORT).show();
                    }

                });

                if(telegram!=null && telegram.equals(""))
                {
                    RootRef.child("Users").child(phoneNumber).child("tg").removeValue();
                }
                if(facebook!=null && facebook.equals(""))
                {
                    RootRef.child("Users").child(phoneNumber).child("fb").removeValue();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        errorMessage.setText("");
    }

    private void startLoadingBar()
    {
        loadingBar.setTitle("Changing info...");
        loadingBar.setMessage("Please, wait a few seconds");
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();
    }

    private void init(View root)
    {
        nameEditText = root.findViewById(R.id.name_edit_text);
        numberTextView = root.findViewById(R.id.number_text_view);
        changeNameButton = root.findViewById(R.id.save_info_button);
        passwordEditText = root.findViewById(R.id.password_edit_text);
        newPasswordEditText = root.findViewById(R.id.new_password_edit_text);
        confirmNewPasswordEditText = root.findViewById(R.id.confirm_new_password_edit_text);
        changePasswordButton = root.findViewById(R.id.edit_password_button);
        changePhotoButton = root.findViewById(R.id.change_photo_button);
        errorMessage = root.findViewById(R.id.error_mesage);
        passwordErrorMessage = root.findViewById(R.id.password_error_mesage);
        profileImage = root.findViewById(R.id.profile_image);
        imageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        telegramEditText = root.findViewById(R.id.telegram_edit_text);
        facebookEditText = root.findViewById(R.id.facebook_edit_text);

        loadingBar = new ProgressDialog(this.getContext());
    }
}