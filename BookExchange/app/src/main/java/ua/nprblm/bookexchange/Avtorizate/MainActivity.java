package ua.nprblm.bookexchange.Avtorizate;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import ua.nprblm.bookexchange.R;
import ua.nprblm.bookexchange.UI.HomeActivity;
import ua.nprblm.bookexchange.Users;

public class MainActivity extends AppCompatActivity {
    private Button loginButton;
    private Button registerButton;

    private EditText numberEditText;
    private EditText passwordEditText;

    private TextView errorMessage;

    private CheckBox saveLoginCheckBox;

    private SharedPreferences.Editor loginPrefsEditor;

    private boolean isNumberValid = false;
    private boolean isPasswordValid = false;

    private ProgressDialog loadingBar;

    private final String parentDBName = "Users";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();

        numberEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                numberEditText.setTextColor(s.toString().length()<10? Color.RED:Color.rgb(116, 116, 116));
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        SharedPreferences loginPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
        loginPrefsEditor = loginPreferences.edit();

        boolean saveLogin = loginPreferences.getBoolean("saveLogin", false);
        if (saveLogin) {
            numberEditText.setText(loginPreferences.getString("number", ""));
            passwordEditText.setText(loginPreferences.getString("password", ""));
            saveLoginCheckBox.setChecked(true);
        }

        loadingBar = new ProgressDialog(this);

        loginButton.setOnClickListener(v -> loginUser());

        registerButton.setOnClickListener(v -> {
            Intent registerIntent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(registerIntent);
        });
    }

    private void loginUser()
    {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(numberEditText.getWindowToken(), 0);

            String phone_number = numberEditText.getText().toString();
            String password_save = passwordEditText.getText().toString();

            if (saveLoginCheckBox.isChecked()) {
                loginPrefsEditor.putBoolean("saveLogin", true);
                loginPrefsEditor.putString("number", phone_number);
                loginPrefsEditor.putString("password", password_save);
            } else {
                loginPrefsEditor.clear();
            }
        loginPrefsEditor.commit();

        if(isDataValid()) {
            String number = numberEditText.getText().toString();
            String password = passwordEditText.getText().toString();

            loadingBar.setTitle("Loging in...");
            loadingBar.setMessage("Please, wait a few seconds");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            validateUser(number, password);

            errorMessage.setText("");
        }
        else
        {
            String messageError = "";
            if(!isNumberValid) messageError+="Number is not valid\n";
            if(!isPasswordValid) messageError+="Password is not valid\n";

            setErrorMessage(messageError);
        }
    }

    private void validateUser(String number, String password) {
        final DatabaseReference RootRef;
        RootRef = FirebaseDatabase.getInstance("https://book-exchange-4777a-default-rtdb.europe-west1.firebasedatabase.app").getReference();

        RootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child(parentDBName).child(number).exists())
                {
                    Users usersData = snapshot.child(parentDBName).child(number).getValue(Users.class);

                    assert usersData != null;
                    if((usersData.getNumber().equals(number))&&(usersData.getPassword().equals(password)))
                    {
                        loadingBar.dismiss();

                        Intent homeIntent = new Intent(MainActivity.this, HomeActivity.class);
                        homeIntent.putExtra("number", number);
                        homeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        homeIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity(homeIntent);
                    }
                    else
                    {
                        loadingBar.dismiss();
                        Toast.makeText(MainActivity.this, "You enter wrong login or password", Toast.LENGTH_SHORT).show();
                        setErrorMessage("You enter wrong login or password\n");
                    }
                }
                else
                {
                    loadingBar.dismiss();
                    Toast.makeText(MainActivity.this, " Error You enter wrong login or password\n", Toast.LENGTH_SHORT).show();
                    setErrorMessage("You enter wrong login or password");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private boolean isDataValid()
    {
        isNumberValid = (numberEditText.getText().toString().length() >= 10);
        isPasswordValid = (!passwordEditText.getText().toString().equals(""));
        return((isNumberValid) && (isPasswordValid));
    }

    private void setErrorMessage(String message)
    {
        errorMessage.setText(message);
    }

    private void init() {
        loginButton = findViewById(R.id.login_button);
        registerButton = findViewById(R.id.register_button);

        numberEditText = findViewById(R.id.number_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);
        errorMessage = findViewById(R.id.error_mesage);

        saveLoginCheckBox = findViewById(R.id.save_login_check_box);
    }
}