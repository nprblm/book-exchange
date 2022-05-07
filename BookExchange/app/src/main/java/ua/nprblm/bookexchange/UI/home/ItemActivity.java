package ua.nprblm.bookexchange.UI.home;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import ua.nprblm.bookexchange.Products;
import ua.nprblm.bookexchange.R;
import ua.nprblm.bookexchange.Users;

public class ItemActivity extends AppCompatActivity {
    private final String parentDBName = "Products";
    private final Products item = new Products();
    private String id;
    private String phoneNumber;
    private boolean isOpen = false;

    private ImageView img;
    private ShapeableImageView profileIcon;

    private TextView nameText;
    private TextView descriptionText;
    private TextView cityText;
    private TextView timeText;
    private TextView contactNameText;
    private TextView contactPhoneText;
    private TextView tgText;
    private TextView fbText;
    private TextView message;

    private Button showContactButton;
    private Button deleteButton;
    private Button yesButton;
    private Button noButton;

    private LinearLayout contactLayout;
    private LinearLayout deleteLayout;
    private LinearLayout tgLayout;
    private LinearLayout fbLayout;

    private ProgressDialog loadingBar;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);

        loadingBar = new ProgressDialog(this);

        init();

        if (savedInstanceState == null)
        {
            Bundle extras = this.getIntent().getExtras();
            if (extras == null)
            {
                id = null;
                phoneNumber = null;
            } else
            {
                id = extras.getString("id");
                phoneNumber = extras.getString("number");
            }
        }
        else
        {
            id = (String) savedInstanceState.getSerializable("id");
            phoneNumber = (String) savedInstanceState.getSerializable("number");
        }

        getData(id);

        showContactButton.setOnClickListener(v -> {
            if(!isOpen) {
                contactLayout.startAnimation(AnimationUtils.loadAnimation(ItemActivity.this, R.anim.trans_in_contact));

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                contactLayout.setVisibility(View.VISIBLE);
                showContactButton.setText("▲ Hide contact info ▲");
                showContactButton.setBackgroundColor(Color.rgb(255, 101, 47));
                isOpen=true;
            }
            else
            {
                contactLayout.startAnimation(AnimationUtils.loadAnimation(ItemActivity.this, R.anim.trans_out_contact));

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                contactLayout.setVisibility(View.GONE);
                showContactButton.setText("▼ Show contact info ▼");
                showContactButton.setBackgroundColor(Color.rgb(20, 167, 108));
                isOpen=false;
            }
        });

        deleteButton.setOnClickListener(v -> {
            if(phoneNumber.equals(contactPhoneText.getText().toString().substring(3))) {
                deleteLayout.startAnimation(AnimationUtils.loadAnimation(ItemActivity.this, R.anim.trans_in_contact));
                message.setVisibility(View.VISIBLE);
                deleteButton.setVisibility(View.GONE);
                deleteLayout.setVisibility(View.VISIBLE);
            }
        });

        noButton.setOnClickListener(v -> {
            deleteLayout.startAnimation(AnimationUtils.loadAnimation(ItemActivity.this, R.anim.trans_out_contact));
            message.setVisibility(View.GONE);
            deleteButton.setVisibility(View.VISIBLE);
            deleteLayout.setVisibility(View.GONE);
        });

        yesButton.setOnClickListener(v -> {
            loadingBar.setTitle("Deleting product...");
            loadingBar.setMessage("Please, wait a few seconds");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();
            deleteThisItem(id);
            onBackPressed();
        });

    }

    private void getData(String id)
    {
        final String[] number = new String[1];
        final DatabaseReference RootRef;
        RootRef = FirebaseDatabase.getInstance("https://book-exchange-4777a-default-rtdb.europe-west1.firebasedatabase.app").getReference();

        RootRef.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                Products product = snapshot.child(parentDBName).child(id).getValue(Products.class);
                assert product != null;
                item.setId(product.getId());
                item.setName(product.getName());
                item.setCity(product.getCity());
                item.setDate(product.getDate());
                item.setDescription(product.getDescription());
                item.setImage(product.getImage());
                item.setNumber(product.getNumber());
                item.setTime(product.getTime());
                setData(item.getImage(), item.getName(), item.getDescription(), item.getCity(), item.getDate(), item.getTime());
                number[0] = item.getNumber();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        RootRef.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                Users user = snapshot.child("Users").child(number[0]).getValue(Users.class);
                assert user != null;
                setContactData(user.getUsername(), user.getNumber(), user.getImage(), user.getTg(), user.getFb());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void deleteThisItem(String id)
    {
        FirebaseDatabase.getInstance("https://book-exchange-4777a-default-rtdb.europe-west1.firebasedatabase.app").getReference().child("Products").child(id).removeValue();loadingBar.dismiss();
        Toast.makeText(ItemActivity.this, "Product delete is successful", Toast.LENGTH_SHORT).show();
    }

    private void setData(String image, String name, String desc, String city, String date, String time)
    {
        nameText.setText(name);
        descriptionText.setText(desc);
        cityText.setText("City: "+city);
        timeText.setText(date+" "+time.substring(0,5));
        Picasso.get().load(image).into(img);
    }

    private void setContactData(String name, String number, String image, String telegram, String facebook)
    {
        contactNameText.setText(name);
        contactPhoneText.setText("+38"+number);
        if(telegram!=null && !telegram.equals("")) {
            tgLayout.setVisibility(View.VISIBLE);
            tgText.setText("t.me/" + telegram);
        }
        if(facebook!=null && !facebook.equals("")) {
            fbLayout.setVisibility(View.VISIBLE);
            fbText.setText("fb.com/" + facebook);
        }
        Picasso.get().load(image).into(profileIcon);
        deleteButton.setVisibility(phoneNumber.equals(contactPhoneText.getText().toString().substring(3))?View.VISIBLE:View.GONE);
    }

    private void init()
    {
        img = findViewById(R.id.product_image);
        nameText = findViewById(R.id.name_text);
        descriptionText = findViewById(R.id.description_text);
        cityText = findViewById(R.id.city_text);
        timeText = findViewById(R.id.date_text);
        message = findViewById(R.id.message);
        message.setVisibility(View.GONE);

        showContactButton = findViewById(R.id.contact_button);
        deleteButton = findViewById(R.id.delete_button);
        deleteButton.setVisibility(View.GONE);

        noButton = findViewById(R.id.no_button);
        yesButton = findViewById(R.id.yes_button);

        contactLayout = findViewById(R.id.contact_layout);
        deleteLayout = findViewById(R.id.delete_layout);
        contactLayout.setVisibility(View.GONE);
        deleteLayout.setVisibility(View.GONE);

        tgLayout = findViewById(R.id.tg_layout);
        fbLayout = findViewById(R.id.fb_layout);
        tgLayout.setVisibility(View.GONE);
        fbLayout.setVisibility(View.GONE);

        contactNameText = findViewById(R.id.contact_name_text);
        contactPhoneText = findViewById(R.id.contact_phone_number_text);
        tgText = findViewById(R.id.tg_text);
        fbText = findViewById(R.id.fb_text);

        profileIcon = findViewById(R.id.profile_image);
    }

}