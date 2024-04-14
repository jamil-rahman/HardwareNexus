package com.example.hardwarenexus;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ProductAdapter.ProductClickListener {
    private RecyclerView recyclerView;
    private ProductAdapter adapter;
    private final List<Product> products = new ArrayList<>();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EdgeToEdge.enable(this);

        recyclerView = findViewById(R.id.products_recycler_view);
        adapter = new ProductAdapter(this, products, this::showProductDetailsDialog);
        recyclerView.setAdapter(adapter);

        // Setting the layout manager depending on orientation
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        } else {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String username = user.getDisplayName();
            if (username != null && !username.isEmpty()) {
                TextView greetingText = findViewById(R.id.greeting_text);
                greetingText.setText("Welcome");
            } else {
                TextView greetingText = findViewById(R.id.greeting_text);
                greetingText.setText("Welcome"); // Fallback to email if username is not available
            }
        } else {
            TextView greetingText = findViewById(R.id.greeting_text);
            greetingText.setText("Hi, Guest");
        }

        loadProducts();
    }

    private void loadProducts() {
        db.collection("products")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    products.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Product product = document.toObject(Product.class);
                        products.add(product);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this,"Error fetching data", Toast.LENGTH_SHORT);
                });
    }

    @Override
    public void onProductClick(Product product) {
        showProductDetailsDialog(product);
    }

    @SuppressLint("DefaultLocale")
    private void showProductDetailsDialog(Product product) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_product_details);

        ImageView imageView = dialog.findViewById(R.id.dialog_image);
        TextView titleView = dialog.findViewById(R.id.dialog_title);
        TextView descriptionView = dialog.findViewById(R.id.dialog_description);
        TextView priceView = dialog.findViewById(R.id.dialog_price);
        Button closeButton = dialog.findViewById(R.id.close_button);

        titleView.setText(product.getTitle());
        descriptionView.setText(product.getDescription());
        priceView.setText(String.format("$%.2f", product.getPrice()));

        Glide.with(this)
                .load(product.getImage())
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.error)
                .into(imageView);

        closeButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}