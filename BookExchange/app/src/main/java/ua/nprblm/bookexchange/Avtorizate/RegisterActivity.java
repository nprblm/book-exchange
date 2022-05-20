package ua.nprblm.bookexchange.Avtorizate;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.AnyRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Objects;

import ua.nprblm.bookexchange.R;


public class RegisterActivity extends AppCompatActivity {
    private Button registerButton;

    private EditText loginEditText;
    private EditText numberEditText;
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;

    private TextView errorMessage;

    private boolean isNameValid = false;
    private boolean isNumberValid = false;
    private boolean isPasswordValid = false;

    private ProgressDialog loadingBar;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        init();
        loadingBar = new ProgressDialog(this);

        numberEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                numberEditText.setTextColor(s.toString().length()<10?Color.RED:Color.rgb(20,167,108));
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        confirmPasswordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                errorMessage.setText((confirmPasswordEditText.getText().toString().equals(passwordEditText.getText().toString())?"":"Passwords is not equals!"));
            }

            @Override
            public void afterTextChanged(Editable s) {
                errorMessage.setText((confirmPasswordEditText.getText().toString().equals(passwordEditText.getText().toString())?"":"Passwords is not equals!"));
            }
        });

        registerButton.setOnClickListener(v -> CreateAccount());

    }

    private void CreateAccount()
    {
        if(isDataValid()) {
            String username = loginEditText.getText().toString();
            String number = numberEditText.getText().toString();
            String password = passwordEditText.getText().toString();

            createImage(username, number, password);

            loadingBar.setTitle("Creating account...");
            loadingBar.setMessage("Please, wait a few seconds");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            errorMessage.setText("");
        }
        else
        {
            String messageError = "";
            if(!isNameValid) messageError+="Name is not valid\n";
            if(!isNumberValid) messageError+="Number is not valid\n";
            if(!isPasswordValid) messageError+="Password is not valid\n";

            errorMessage.setText(messageError);
        }
    }

    private boolean isDataValid()
    {
        isNameValid = (loginEditText.getText().toString().length() > 0);
        isNumberValid = (numberEditText.getText().toString().length() >= 10);
        isPasswordValid = ((passwordEditText.getText().toString().equals(confirmPasswordEditText.getText().toString())) && (!passwordEditText.getText().toString().equals("")) && passwordEditText.getText().toString().length() >=5);
        return((isNameValid) && (isNumberValid) && (isPasswordValid));
    }

    private void publishData(String name, String number, String password, String link)
    {
        final DatabaseReference RootRef;
        RootRef = FirebaseDatabase.getInstance("https://book-exchange-4777a-default-rtdb.europe-west1.firebasedatabase.app").getReference();

        RootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!(snapshot.child("Users").child(number).exists()))
                {
                    HashMap<String, Object> userDataMap = new HashMap<>();
                    userDataMap.put("number",number);
                    userDataMap.put("username",name);
                    userDataMap.put("password",password);
                    userDataMap.put("image", link);

                    RootRef.child("Users").child(number).updateChildren(userDataMap).addOnCompleteListener(task -> {
                        if(task.isSuccessful())
                        {
                            loadingBar.dismiss();

                            Toast.makeText(RegisterActivity.this, "Register is successful", Toast.LENGTH_SHORT).show();

                            Intent loginIntent = new Intent(RegisterActivity.this, LoginActivity.class);
                            loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            loginIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                            startActivity(loginIntent);
                        }
                        else
                        {
                            loadingBar.dismiss();
                            Toast.makeText(RegisterActivity.this, "Error", Toast.LENGTH_SHORT).show();
                        }

                    });
                }
                else
                {
                    Toast.makeText(RegisterActivity.this, "Number is already used", Toast.LENGTH_SHORT).show();
                    errorMessage.setText("Number is already used");
                    loadingBar.dismiss();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void createImage(String name, String phoneNumber, String password)
    {
        Resources res = this.getResources();
        int drawableResourceId = this.getResources().getIdentifier("ic_default_profile_240", "drawable", this.getPackageName());
        Uri imageUri = getUriToResource(res,drawableResourceId);

        StorageReference filePath = FirebaseStorage.getInstance().getReference().child("Profile Images").child(phoneNumber + ".jpeg");
        final UploadTask uploadTask = filePath.putFile(imageUri);

        uploadTask.addOnFailureListener(e -> loadingBar.dismiss()).addOnSuccessListener(taskSnapshot -> uploadTask.continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw Objects.requireNonNull(task.getException());
            } else {
                return filePath.getDownloadUrl();
            }
        }).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Uri downloadUrl = task.getResult();
                String downloadImageUrl = String.valueOf(downloadUrl);
                publishData(name, phoneNumber, password, downloadImageUrl);
            }
        }));
    }

    public static Uri getUriToResource(@NonNull Resources res,
                                       @AnyRes int resId)
            throws Resources.NotFoundException {
        return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                "://" + res.getResourcePackageName(resId)
                + '/' + res.getResourceTypeName(resId)
                + '/' + res.getResourceEntryName(resId));
    }

    private void init()
    {
        registerButton = findViewById(R.id.register_button);
        loginEditText = findViewById(R.id.login_edit_text);
        numberEditText = findViewById(R.id.number_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);
        confirmPasswordEditText= findViewById(R.id.confirm_password_edit_text);
        errorMessage = findViewById(R.id.error_mesage);
    }

}