package ua.nprblm.bookexchange.UI.home;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import ua.nprblm.bookexchange.Avtorizate.MainActivity;
import ua.nprblm.bookexchange.Products;
import ua.nprblm.bookexchange.R;
import ua.nprblm.bookexchange.UI.HomeActivity;

class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.ViewHolder> {
    private ArrayList<Products> products;

    private OnProductClickListener onProductClickListener;

    public HomeAdapter(ArrayList<Products> products, OnProductClickListener onProductClickListener) {
        this.products = products;
        this.onProductClickListener = onProductClickListener;

    }


    @NonNull
    @Override
    public HomeAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_item_layout, parent, false);
        ViewHolder holder = new ViewHolder(view, onProductClickListener);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.nameText.setText(products.get(position).getName());
        holder.cityText.setText("City: " + products.get(position).getCity());
        holder.dateText.setText(products.get(position).getDate()+" "+products.get(position).getTime().substring(0,5));
        Picasso.get().load(products.get(position).getImage()).into(holder.productImage);
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public class ViewHolder extends  RecyclerView.ViewHolder implements View.OnClickListener {

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
