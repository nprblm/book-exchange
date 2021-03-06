package ua.nprblm.bookexchange.ui.add;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

import ua.nprblm.bookexchange.models.Cities;
import ua.nprblm.bookexchange.R;
import ua.nprblm.bookexchange.databinding.FragmentAddBinding;


public class AddFragment extends Fragment {

    private FragmentAddBinding binding;

    private ImageView addImage;
    private static final int GALLERY_PICK = 1;
    private static final int RESULT_OK = -1;
    private Uri imageUri;
    private StorageReference imageRef;
    private String downloadImageUrl;

    private EditText nameEditText;
    private EditText descriptionEditText;

    private AutoCompleteTextView cityChange;

    private TextView errorMessage;

    private Button addButton;
    private Button addPictureButton;

    private boolean isNameValid = false;
    private boolean isDescriptionValid = false;
    private boolean isCityValid = false;
    private boolean isImageValid = false;

    private String date;
    private String time;
    private String name;
    private String description;
    private String uniqueId;
    private String city;

    private DatabaseReference productsRef;

    private ProgressDialog loadingBar;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentAddBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        init(root);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_dropdown_item_1line, Cities.getList());
        cityChange.setThreshold(1);
        cityChange.setAdapter(adapter);

        addButton.setOnClickListener(v -> {
            if(isDataValid())
            {
                loadingBar.setTitle("Uploading product...");
                loadingBar.setMessage("Please, wait a few seconds");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                errorMessage.setText("");
                uniqueId = setUniqueId();
                downloadImage(uniqueId, savedInstanceState);

                clearAll();

            }
            else
            {
                String messageError = "";
                if(!isNameValid) messageError+="Name must have 4 or more characters\n";
                if(!isDescriptionValid) messageError+="Description must have 10 or more characters\n";
                if(!isCityValid) messageError+="Don`t have this city in database\n";
                if(!isImageValid) messageError+="You must upload a picture\n";

                errorMessage.setText(messageError);
            }
        });

        addPictureButton.setOnClickListener(v -> openGallery());

        return root;
    }

    private void openGallery() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,GALLERY_PICK);
    }

    @SuppressLint("SetTextI18n")
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private boolean isDataValid()
    {
        name = nameEditText.getText().toString();
        description = descriptionEditText.getText().toString();

        isNameValid = (name.length() > 3);
        isDescriptionValid = (description.length() > 9);
        isCityValid = searchInArray(cityChange.getText().toString());
        isImageValid = (imageUri != null);
        return((isNameValid) && (isDescriptionValid) && (isCityValid) && (isImageValid));
    }

    private boolean searchInArray(String city)
    {
        String[] array = Cities.getList();
        for (String s : array) {
            if (city.toLowerCase(Locale.ROOT).equals(s.toLowerCase(Locale.ROOT))) {
                city = cityChange.getText().toString().substring(0, 1).toUpperCase(Locale.ROOT) + cityChange.getText().toString().substring(1).toLowerCase(Locale.ROOT);
                setCity(city);
                return true;
            }
        }
        return false;
    }

    private void setCity(String city) {
        this.city = city;
    }

    private String setUniqueId()
    {
        Calendar calendar = Calendar.getInstance();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        @SuppressLint("SimpleDateFormat") SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        date = dateFormat.format(calendar.getTime());
        time = timeFormat.format(calendar.getTime());

        String result = "id" + date + time;
        result = result.replace(".","").replace(":","");
        return result;

    }

    private void downloadImage(String uID, Bundle savedInstanceState)
    {
        StorageReference filePath = imageRef.child(uID + ".jpeg");
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

                uploadData(savedInstanceState);
            }
        }));
    }

    private void uploadData(Bundle savedInstanceState)
    {
        String number;
        if (savedInstanceState == null) {
            Bundle extras = requireActivity().getIntent().getExtras();
            if(extras == null) {
                number= null;
            } else {
                number= extras.getString("number");
            }
        } else {
            number= (String) savedInstanceState.getSerializable("number");
        }

        HashMap<String,Object> productMap = new HashMap<>();

        productMap.put("id", uniqueId);
        productMap.put("date", date);
        productMap.put("time", time);
        productMap.put("name", name);
        productMap.put("description", description);
        productMap.put("city", city);
        productMap.put("image", downloadImageUrl);
        productMap.put("number", number);

        productsRef.child(uniqueId).updateChildren(productMap).addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                Toast.makeText(getActivity(), "Product uploading is successful", Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
                hideKeyboard(requireActivity());
            }
            else {
                Toast.makeText(getActivity(), "Product uploading is fail", Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
            }
        });
    }

    private void clearAll()
    {
        addImage.setImageResource(R.drawable.empty);
        nameEditText.setText("");
        descriptionEditText.setText("");
        cityChange.setText("");
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void init(View root)
    {
        addImage = root.findViewById(R.id.add_image);
        nameEditText = root.findViewById(R.id.name_edit_text);
        descriptionEditText = root.findViewById(R.id.description_edit_text);
        cityChange = root.findViewById(R.id.city_edit_text);
        errorMessage = root.findViewById(R.id.error_mesage);
        addButton = root.findViewById(R.id.add_button);
        addPictureButton = root.findViewById(R.id.add_picture_button);
        loadingBar = new ProgressDialog(getActivity());
        imageRef = FirebaseStorage.getInstance().getReference().child("Product Images");
        productsRef = FirebaseDatabase.getInstance("https://book-exchange-4777a-default-rtdb.europe-west1.firebasedatabase.app").getReference().child("Products");
    }
}