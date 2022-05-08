package ua.nprblm.bookexchange.UI.home;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
import java.util.Locale;

import ua.nprblm.bookexchange.Cities;
import ua.nprblm.bookexchange.Products;
import ua.nprblm.bookexchange.R;

public class ItemSettingsActivity extends AppCompatActivity {

    private ImageView addImage;
    private static final int GALLERY_PICK = 1;
    private static final int RESULT_OK = -1;
    private Uri imageUri;
    private StorageReference imageRef;
    private String downloadImageUrl = "";

    private EditText nameEditText;
    private EditText descriptionEditText;

    private AutoCompleteTextView cityChange;

    private TextView errorMessage;

    private Button addButton;
    private Button addPictureButton;

    private boolean isNameValid = false;
    private boolean isDescriptionValid = false;
    private boolean isCityValid = false;
    private final boolean isImageValid = false;

    private String name;
    private String description;
    private String city;
    String id;
    String phoneNumber;

    private Products thisProduct = new Products();

    private DatabaseReference productsRef;

    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_settings);

        init();
        if (savedInstanceState == null) {
            Bundle extras = this.getIntent().getExtras();
            if(extras == null) {
                id= null;
                phoneNumber = null;
            } else {
                id= extras.getString("id");
                phoneNumber= extras.getString("number");
            }
        } else {
            id= (String) savedInstanceState.getSerializable("id");
            phoneNumber= (String) savedInstanceState.getSerializable("number");
        }

        Products[] product = new Products[1];
        DatabaseReference RootRef = FirebaseDatabase.getInstance("https://book-exchange-4777a-default-rtdb.europe-west1.firebasedatabase.app").getReference();

        RootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                product[0] = snapshot.child("Products").child(id).getValue(Products.class);
                setData(product[0].getName(), product[0].getDescription(), product[0].getCity(), product[0].getImage());
                thisProduct.setTime(product[0].getTime());
                thisProduct.setDate(product[0].getDate());
                thisProduct.setDescription(product[0].getDescription());
                thisProduct.setNumber(product[0].getNumber());
                thisProduct.setImage(product[0].getImage());
                thisProduct.setId(product[0].getId());
                thisProduct.setCity(product[0].getCity());
                thisProduct.setName(product[0].getName());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        loadingBar = new ProgressDialog(this);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, Cities.getList());
        cityChange.setThreshold(1);
        cityChange.setAdapter(adapter);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isDataValid()) {
                    loadingBar.setTitle("Uploading product...");
                    loadingBar.setMessage("Please, wait a few seconds");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();

                    thisProduct.setName(nameEditText.getText().toString());
                    thisProduct.setDescription(descriptionEditText.getText().toString());
                    thisProduct.setCity(cityChange.getText().toString());

                    errorMessage.setText("");
                    if (imageUri != null) {
                        downloadImage(thisProduct.getId());
                    } else {
                        uploadData(thisProduct.getId(), thisProduct.getDate(), thisProduct.getTime(), thisProduct.getName(), thisProduct.getDescription(), thisProduct.getCity(), thisProduct.getImage(), thisProduct.getNumber());
                    }
                }
                else
                {
                    String messageError = "";
                    if(!isNameValid) messageError+="Name must have 4 or more characters\n";
                    if(!isDescriptionValid) messageError+="Description must have 10 or more characters\n";
                    if(!isCityValid) messageError+="Dont have this city in database\n";

                    errorMessage.setText(messageError);
                }
            }
        });

        addPictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });
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
            imageUri = data.getData();
            addImage.setImageURI(imageUri);
            addPictureButton.setText("Edit picture");
            addPictureButton.setBackgroundColor(Color.rgb(255, 101, 47));
        }
    }

    private boolean isDataValid()
    {
        name = nameEditText.getText().toString();
        description = descriptionEditText.getText().toString();

        isNameValid = (name.length()>3?true:false);
        isDescriptionValid = (description.length()>9?true:false);
        isCityValid = searchInArray(cityChange.getText().toString());
        return((isNameValid)&&(isDescriptionValid)&&(isCityValid)?true:false);
    }

    private boolean searchInArray(String city)
    {
        String[] array = Cities.getList();
        for(int i = 0 ;i< array.length;i++)
        {
            if(city.toLowerCase(Locale.ROOT).equals(array[i].toLowerCase(Locale.ROOT)))
            {
                city = cityChange.getText().toString().substring(0,1).toUpperCase(Locale.ROOT)+cityChange.getText().toString().substring(1).toLowerCase(Locale.ROOT);
                setCity(city);
                return true;
            }
        }
        return false;
    }

    private void setCity(String city) {
        this.city = city;
    }

    private void downloadImage(String uID)
    {
        loadingBar.setTitle("Uploading product...");
        loadingBar.setMessage("Please, wait a few seconds");
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();

        StorageReference filePath = imageRef.child(uID + ".jpeg");
        final UploadTask uploadTask = filePath.putFile(imageUri);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                String message = e.toString();
                Toast.makeText(ItemSettingsActivity.this, message, Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> uriTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        } else {
                            downloadImageUrl =filePath.getDownloadUrl().toString();
                            return filePath.getDownloadUrl();
                        }
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri downloadUrl = task.getResult();
                            downloadImageUrl = String.valueOf(downloadUrl);
                            System.out.println(downloadImageUrl);
                            thisProduct.setImage(downloadImageUrl);
                            loadingBar.dismiss();
                            uploadData(thisProduct.getId(), thisProduct.getDate(), thisProduct.getTime(), thisProduct.getName(), thisProduct.getDescription(), thisProduct.getCity(), thisProduct.getImage(), thisProduct.getNumber());
                        }
                    }
                });
            }
        });
    }

    private void uploadData(String productId, String date, String time, String username, String desc, String citySet, String imageLink, String number)
    {
        HashMap<String,Object> productMap = new HashMap<>();

        productMap.put("id",productId);
        productMap.put("date", date);
        productMap.put("time", time);
        productMap.put("name", username);
        productMap.put("description", desc);
        productMap.put("city", citySet);
        productMap.put("image", imageLink);
        productMap.put("number", number);

        productsRef.child(id).updateChildren(productMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    Toast.makeText(ItemSettingsActivity.this, "Product uploading is successful", Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                    hideKeyboard(ItemSettingsActivity.this);
                }
                else
                {
                    Toast.makeText(ItemSettingsActivity.this, "Product uploading is fail", Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                }

            }
        });
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void setData(String name, String description, String city, String imageLink)
    {
        nameEditText.setText(name);
        descriptionEditText.setText(description);
        cityChange.setText(city);
        Picasso.get().load(imageLink).into(addImage);
    }

    private void init()
    {
        addImage = findViewById(R.id.add_image);
        loadingBar = new ProgressDialog(this);
        nameEditText = findViewById(R.id.name_edit_text);
        descriptionEditText = findViewById(R.id.description_edit_text);
        cityChange = findViewById(R.id.city_edit_text);
        errorMessage = findViewById(R.id.error_mesage);
        addButton = findViewById(R.id.add_button);
        addPictureButton = findViewById(R.id.add_picture_button);
        imageRef = FirebaseStorage.getInstance().getReference().child("Product Images");
        productsRef = FirebaseDatabase.getInstance("https://book-exchange-4777a-default-rtdb.europe-west1.firebasedatabase.app").getReference().child("Products");
    }
}