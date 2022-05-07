package ua.nprblm.bookexchange.UI.home;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
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

    private Button filterButton;
    private Button openCloseButton;

    private CheckBox myCheckBox;

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

        filterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(cityChange.getText()!=null)
                {
                    loadItems(cityChange.getText().toString(),myCheckBox.isChecked());
                }
            }
        });

        openCloseButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onClick(View v) {
                if(!isOpen) {
                    cityChange.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.trans_in));
                    myCheckBox.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.trans_in));
                    filterButton.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.trans_in));
                    recyclerView.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.trans_in));
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    cityChange.setVisibility(View.VISIBLE);
                    myCheckBox.setVisibility(View.VISIBLE);
                    filterButton.setVisibility(View.VISIBLE);
                    openCloseButton.setText("▲ Close filters ▲");
                    openCloseButton.setBackgroundColor(Color.rgb(255, 101, 47));
                    isOpen=true;
                }
                else
                {
                    cityChange.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.trans_out));
                    myCheckBox.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.trans_out));
                    filterButton.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.trans_out));
                    recyclerView.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.trans_out));

                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    cityChange.setVisibility(View.GONE);
                    myCheckBox.setVisibility(View.GONE);
                    filterButton.setVisibility(View.GONE);
                    openCloseButton.setText("▼ Open filters ▼");
                    openCloseButton.setBackgroundColor(Color.rgb(20, 167, 108));
                    isOpen=false;
                }
            }
        });

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        loadItems("",false);

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

    private void loadItems(String cityName, boolean onlyMyBooks)
    {
        DatabaseReference rootRef = FirebaseDatabase.getInstance("https://book-exchange-4777a-default-rtdb.europe-west1.firebasedatabase.app").getReference();
        DatabaseReference productsRef = rootRef.child("Products");
        Query myTopPostsQuery = productsRef.orderByChild("id");

        myTopPostsQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<Products> list = new ArrayList<>();
                ArrayList<Products> onlyMyList = new ArrayList<>();
                for(DataSnapshot ds : dataSnapshot.getChildren()) {
                    Products product = ds.getValue(Products.class);
                    if(cityName.equals("")||cityName.equals(product.getCity())) {
                        list.add(product);
                        if (product.getNumber().equals(phoneNumber)) {
                            onlyMyList.add(product);
                        }
                    }
                }
                if(onlyMyBooks) {
                    setProducts(onlyMyList);
                }
                else {
                    setProducts(list);
                }
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
        myCheckBox = root.findViewById(R.id.my_check_box);
        filterButton = root.findViewById(R.id.filter_button);
        cityChange = root.findViewById(R.id.city_edit_text);
        productsRef = FirebaseDatabase.getInstance("https://book-exchange-4777a-default-rtdb.europe-west1.firebasedatabase.app").getReference().child("Products");
    }

}