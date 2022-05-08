package ua.nprblm.bookexchange.UI.home;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

import ua.nprblm.bookexchange.Cities;
import ua.nprblm.bookexchange.Products;
import ua.nprblm.bookexchange.R;
import ua.nprblm.bookexchange.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment implements HomeAdapter.OnProductClickListener {

    private HomeViewModel homeViewModel;
    private FragmentHomeBinding binding;

    private AutoCompleteTextView cityChange;

    private RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    private DatabaseReference productsRef;

    private ArrayList<Products> products = new ArrayList<>();

    private String city;
    private String phoneNumber;

    private boolean isOpen = false;
    private boolean isSearchOpen = false;


    private ImageView openCloseButton;
    private ImageView searchButton;

    private CheckBox myCheckBox;

    private EditText searchEditText;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        init(root);
        phoneNumber = getPhoneNumber(savedInstanceState);
        recyclerView = root.findViewById(R.id.recycler_view);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_dropdown_item_1line, Cities.getList());
        cityChange.setThreshold(1);
        cityChange.setAdapter(adapter);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isSearchOpen) {
                    searchEditText.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.trans_in_left));
                    openCloseButton.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.trans_in_left));
                    searchEditText.setVisibility(View.VISIBLE);
                    openCloseButton.setVisibility(View.VISIBLE);
                    isSearchOpen = true;
                }
                else
                {
                    if(isOpen)
                    {
                        cityChange.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.trans_out));
                        myCheckBox.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.trans_out));
                        recyclerView.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.trans_out));

                        cityChange.setVisibility(View.GONE);
                        myCheckBox.setVisibility(View.GONE);
                        isOpen=false;
                    }

                    searchEditText.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.trans_out_left));
                    openCloseButton.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.trans_out_left));
                    searchEditText.setVisibility(View.GONE);
                    openCloseButton.setVisibility(View.GONE);
                    isSearchOpen = false;
                }
            }
        });

        openCloseButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onClick(View v) {
               openCloseButtonPressed();
            }
        });

        searchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    loadItems(searchEditText.getText().toString(), cityChange.getText().toString(), myCheckBox.isChecked());
                    return true;
                }
                return false;
            }
        });

        cityChange.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    loadItems(searchEditText.getText().toString(), cityChange.getText().toString(), myCheckBox.isChecked());
                    return true;
                }
                return false;
            }
        });

        myCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadItems(searchEditText.getText().toString(), cityChange.getText().toString(), myCheckBox.isChecked());
            }
        });

        return root;
    }

    private void openCloseButtonPressed()
    {
        if(!isOpen) {
            cityChange.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.trans_in));
            myCheckBox.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.trans_in));
            recyclerView.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.trans_in));
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            cityChange.setVisibility(View.VISIBLE);
            myCheckBox.setVisibility(View.VISIBLE);
            isOpen=true;
        }
        else
        {
            cityChange.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.trans_out));
            myCheckBox.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.trans_out));
            recyclerView.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.trans_out));

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            cityChange.setVisibility(View.GONE);
            myCheckBox.setVisibility(View.GONE);
            isOpen=false;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        loadItems(null,null,false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void setProducts(ArrayList<Products> products) {
        this.products = products;
    }

    public ArrayList<Products> getProducts() {
        return products;
    }

    @Override
    public void onProductClick(int position) {
        System.out.println(products.get(position).getName());
        Intent itemIntent = new Intent(getContext(), ItemActivity.class);
        itemIntent.putExtra("id", products.get(position).getId());
        itemIntent.putExtra("number", phoneNumber);
        startActivity(itemIntent);
    }

    private void loadItems(String searchText, String cityName, boolean onlyMyBooks)
    {
        DatabaseReference rootRef = FirebaseDatabase.getInstance("https://book-exchange-4777a-default-rtdb.europe-west1.firebasedatabase.app").getReference();
        DatabaseReference productsRef = rootRef.child("Products");
        Query myTopPostsQuery = productsRef.orderByChild("id");

        myTopPostsQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<Products> list = new ArrayList<>();
                for(DataSnapshot ds : dataSnapshot.getChildren()) {
                    Products product = ds.getValue(Products.class);
                    list.add(product);
                }

                if(searchText!=null && !searchText.equals(""))
                {
                    for(int i =0; i<list.size();i++)
                    if(!list.get(i).getName().toLowerCase(Locale.ROOT).contains(searchText.toLowerCase(Locale.ROOT)))
                    {
                        list.remove(i);
                        i--;
                    }
                }
                if(cityName!=null && !cityName.equals(""))
                {
                    for(int i = 0; i<list.size();i++)
                        if(!list.get(i).getCity().toLowerCase(Locale.ROOT).equals(cityName.toLowerCase(Locale.ROOT)))
                        {
                            list.remove(i);
                            i--;
                        }
                }

                if(onlyMyBooks)
                {
                    for(int i = 0; i<list.size();i++)
                        if(!list.get(i).getNumber().equals(phoneNumber))
                        {
                            list.remove(i);
                            i--;
                        }
                }

                setProducts(list);

                Collections.reverse(products);
                HomeAdapter adapter = new HomeAdapter(products, HomeFragment.this);

                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    private String getPhoneNumber(Bundle savedInstanceState)
    {
        String number;
        if (savedInstanceState == null)
        {
            Bundle extras = getActivity().getIntent().getExtras();
            if (extras == null)
            {
                number = null;
            } else
            {
                number = extras.getString("number");
            }
        }
        else
        {
            number = (String) savedInstanceState.getSerializable("number");
        }
        return number;
    }

    private void init(View root)
    {
        openCloseButton = root.findViewById(R.id.open_close_button);
        searchButton = root.findViewById(R.id.search_button);
        myCheckBox = root.findViewById(R.id.my_check_box);
        cityChange = root.findViewById(R.id.city_edit_text);
        searchEditText = root.findViewById(R.id.search_edit_text);

        searchEditText.setVisibility(View.GONE);
        openCloseButton.setVisibility(View.GONE);

        cityChange.setVisibility(View.GONE);
        myCheckBox.setVisibility(View.GONE);

        productsRef = FirebaseDatabase.getInstance("https://book-exchange-4777a-default-rtdb.europe-west1.firebasedatabase.app").getReference().child("Products");
    }

}