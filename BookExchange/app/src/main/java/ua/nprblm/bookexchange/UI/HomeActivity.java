package ua.nprblm.bookexchange.UI;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import ua.nprblm.bookexchange.Models.Users;
import ua.nprblm.bookexchange.R;
import ua.nprblm.bookexchange.databinding.ActivityHomeBinding;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ua.nprblm.bookexchange.databinding.ActivityHomeBinding binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String number = getNumber(savedInstanceState);

        new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_add, R.id.navigation_profile).build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_home);
        NavigationUI.setupWithNavController(binding.navView, navController);

        final DatabaseReference RootRef;
        RootRef = FirebaseDatabase.getInstance("https://book-exchange-4777a-default-rtdb.europe-west1.firebasedatabase.app").getReference();

        assert number != null;
        RootRef.child("Users").child(number).get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Toast.makeText(HomeActivity.this, "DataBase Error", Toast.LENGTH_SHORT).show();
            }
            else {
                task.getResult().getValue(Users.class);
            }
        });
    }

    private String getNumber(Bundle savedInstanceState)
    {
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                return null;
            } else {
                return extras.getString("number");
            }
        } else {
            return (String) savedInstanceState.getSerializable("number");
        }
    }

}