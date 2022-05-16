package ua.nprblm.bookexchange.UI.home;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import ua.nprblm.bookexchange.Models.Products;
import ua.nprblm.bookexchange.R;

class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.ViewHolder> {
    private final ArrayList<Products> products;

    private final OnProductClickListener onProductClickListener;

    public HomeAdapter(ArrayList<Products> products, OnProductClickListener onProductClickListener) {
        this.products = products;
        this.onProductClickListener = onProductClickListener;

    }


    @NonNull
    @Override
    public HomeAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_item_layout, parent, false);
        return new ViewHolder(view, onProductClickListener);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.nameText.setText(products.get(position).getName());
        holder.cityText.setText(products.get(position).getCity());
        holder.dateText.setText(products.get(position).getDate()+" "+products.get(position).getTime().substring(0,5));
        Picasso.get().load(products.get(position).getImage()).into(holder.productImage);
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public static class ViewHolder extends  RecyclerView.ViewHolder implements View.OnClickListener {

        public ImageView productImage;

        public TextView nameText;
        public TextView descriptionText;
        public TextView cityText;
        public TextView dateText;

        OnProductClickListener onProductClickListener;

        public ViewHolder(@NonNull View itemView, OnProductClickListener onProductClickListener) {
            super(itemView);
            productImage = itemView.findViewById(R.id.product_image);
            nameText = itemView.findViewById(R.id.name_text);
            descriptionText = itemView.findViewById(R.id.description_text);
            cityText = itemView.findViewById(R.id.city_text);
            dateText = itemView.findViewById(R.id.date_text);
            this.onProductClickListener = onProductClickListener;

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onProductClickListener.onProductClick(getAdapterPosition());
        }
    }

    public interface OnProductClickListener {
        void onProductClick(int position);
    }
}
