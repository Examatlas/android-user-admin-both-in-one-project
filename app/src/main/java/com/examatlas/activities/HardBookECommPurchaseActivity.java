package com.examatlas.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
import com.examatlas.R;
import com.examatlas.adapter.HardBookECommPurchaseAdapter;
import com.examatlas.models.HardBookECommPurchaseModel;
import com.examatlas.models.extraModels.BookImageModels;
import com.examatlas.utils.Constant;
import com.examatlas.utils.MySingleton;
import com.examatlas.utils.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HardBookECommPurchaseActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private ImageSlider slider;
    private RecyclerView booksRecyclerView;
    private HardBookECommPurchaseAdapter hardBookECommPurchaseAdapter;
    private ArrayList<HardBookECommPurchaseModel> hardBookECommPurchaseModelArrayList;
    private ProgressBar progressBar;
    private SessionManager sessionManager;
    private String token;
    private RelativeLayout noDataLayout;
    private SearchView searchView;
    private final String bookURL = Constant.BASE_URL + "book/getAllBooks";
    ImageView cartIcon,wishlistIcon;
    private int currentPage = 1;
    private int totalPages = 1;
    private final int itemsPerPage = 10;
    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hard_book_ecomm_purchase);

        initializeViews();
        setupToolbar();
        setupImageSlider();
        setupSearchView();
        getAllBooks();
        setClickingListeners();
    }



    private void initializeViews() {
        toolbar = findViewById(R.id.hardbook_ecomm_purchase_toolbar);
        slider = findViewById(R.id.slider);
        progressBar = findViewById(R.id.progressBar);
        booksRecyclerView = findViewById(R.id.booksRecycler);
        noDataLayout = findViewById(R.id.noDataLayout);
        searchView = findViewById(R.id.search_icon);
        cartIcon = findViewById(R.id.cartBtn);
        wishlistIcon = findViewById(R.id.wishListBtn);
        hardBookECommPurchaseModelArrayList = new ArrayList<>();
        sessionManager = new SessionManager(this);
        token = sessionManager.getUserData().get("authToken");

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2); // 2 is the number of columns
        booksRecyclerView.setLayoutManager(gridLayoutManager);

        booksRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                // Get the GridLayoutManager and find the last visible item position
                int lastVisibleItemPosition = gridLayoutManager.findLastVisibleItemPosition();
                int totalItemCount = gridLayoutManager.getItemCount();

                Log.d("ScrollListener", "Last visible item position: " + lastVisibleItemPosition + " Total items: " + totalItemCount);

                // Check if we are at the bottom of the list
                if (lastVisibleItemPosition + 1 >= totalItemCount && !isLoading) {
                    // Check if there are more pages to load
                    if (currentPage < totalPages) {
                        currentPage++;  // Increment the current page
                        getAllBooks();   // Fetch the next set of books
                    }
                }
            }
        });

    }
    private void setClickingListeners() {
        wishlistIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HardBookECommPurchaseActivity.this, WishlistActivity.class);
                startActivity(intent);
            }
        });
        cartIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HardBookECommPurchaseActivity.this, CartViewActivity.class);
                startActivity(intent);
            }
        });
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
    }

    private void setupImageSlider() {
        ArrayList<SlideModel> sliderArrayList = new ArrayList<>();
        sliderArrayList.add(new SlideModel(R.drawable.image1, ScaleTypes.CENTER_CROP));
        sliderArrayList.add(new SlideModel(R.drawable.image2, ScaleTypes.CENTER_CROP));
        sliderArrayList.add(new SlideModel(R.drawable.image3, ScaleTypes.CENTER_CROP));
        slider.setImageList(sliderArrayList);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupSearchView() {
        searchView.setOnClickListener(view -> openKeyboard());
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (hardBookECommPurchaseAdapter != null) {
                    hardBookECommPurchaseAdapter.filter(newText);
                }
                return true;
            }
        });
    }

    private void openKeyboard() {
        searchView.setIconified(false);
        searchView.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(searchView, InputMethodManager.SHOW_IMPLICIT);
        }
    }
    private void getAllBooks() {
        String paginatedURL = bookURL + "?type=book&page=" + currentPage + "&per_page=" + itemsPerPage;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, paginatedURL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            booksRecyclerView.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.GONE);
                            boolean status = response.getBoolean("status");

                            if (status) {
                                JSONArray jsonArray = response.getJSONArray("books");
                                hardBookECommPurchaseModelArrayList.clear();

                                // Parse books directly here
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject2 = jsonArray.getJSONObject(i);
                                    ArrayList<BookImageModels> bookImageArrayList = new ArrayList<>();
                                    JSONArray jsonArray1 = jsonObject2.getJSONArray("images");

                                    JSONObject jsonObject = response.getJSONObject("pagination");

                                    int totalRows = Integer.parseInt(jsonObject.getString("totalRows"));
                                    totalPages = Integer.parseInt(jsonObject.getString("totalPages"));
                                    currentPage = Integer.parseInt(jsonObject.getString("currentPage"));
                                    int pageSize = Integer.parseInt(jsonObject.getString("pageSize"));

                                    for (int j = 0; j < jsonArray1.length(); j++) {
                                        JSONObject jsonObject3 = jsonArray1.getJSONObject(j);
                                        BookImageModels bookImageModels = new BookImageModels(
                                                jsonObject3.getString("url"),
                                                jsonObject3.getString("filename")
                                        );
                                        bookImageArrayList.add(bookImageModels);
                                    }
                                    HardBookECommPurchaseModel model = new HardBookECommPurchaseModel(
                                            jsonObject2.getString("_id"),
                                            jsonObject2.getString("type"),
                                            jsonObject2.getString("title"),
                                            jsonObject2.getString("keyword"),
                                            jsonObject2.getString("stock"),
                                            jsonObject2.getString("price"),
                                            jsonObject2.getString("sellPrice"),
                                            jsonObject2.getString("content"),
                                            jsonObject2.getString("author"),
                                            jsonObject2.getString("categoryId"),
                                            jsonObject2.getString("subCategoryId"),
                                            jsonObject2.getString("subjectId"),
                                            parseTags(jsonObject2.getJSONArray("tags")), // Ensure this method is implemented correctly
                                            jsonObject2.getString("bookUrl"),
                                            bookImageArrayList,
                                            jsonObject2.getString("createdAt"),
                                            jsonObject2.getString("updatedAt"),
                                            jsonObject2.getString("isInCart"),
                                            jsonObject2.getString("isInWishList"),
                                            totalRows,totalPages,currentPage,pageSize
                                    );
                                    hardBookECommPurchaseModelArrayList.add(model);
                                }
                                updateUI();
                                if (hardBookECommPurchaseModelArrayList.isEmpty()) {
                                    noDataLayout.setVisibility(View.VISIBLE);
                                    booksRecyclerView.setVisibility(View.GONE);
                                    progressBar.setVisibility(View.GONE);
                                } else {
                                    if (hardBookECommPurchaseAdapter == null) {
                                        hardBookECommPurchaseAdapter = new HardBookECommPurchaseAdapter(HardBookECommPurchaseActivity.this, hardBookECommPurchaseModelArrayList);
                                        booksRecyclerView.setAdapter(hardBookECommPurchaseAdapter);
                                    } else {
                                        hardBookECommPurchaseAdapter.notifyDataSetChanged();
                                    }
                                }
                            } else {
                                Toast.makeText(HardBookECommPurchaseActivity.this, response.getString("message"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Log.e("JSON_ERROR", "Error parsing JSON: " + e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String errorMessage = "Error: " + error.toString();
                        if (error.networkResponse != null) {
                            try {
                                // Parse the error response
                                String jsonError = new String(error.networkResponse.data);
                                JSONObject jsonObject = new JSONObject(jsonError);
                                String message = jsonObject.optString("message", "Unknown error");
                                // Now you can use the message
                                Toast.makeText(HardBookECommPurchaseActivity.this, message, Toast.LENGTH_LONG).show();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        Log.e("BlogFetchError", errorMessage);
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        MySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }
    private void updateUI() {
        if (hardBookECommPurchaseModelArrayList.isEmpty()) {
            noDataLayout.setVisibility(View.VISIBLE);
            booksRecyclerView.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
        } else {
            noDataLayout.setVisibility(View.GONE);
            booksRecyclerView.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            if (hardBookECommPurchaseAdapter == null) {
                hardBookECommPurchaseAdapter = new HardBookECommPurchaseAdapter(this, hardBookECommPurchaseModelArrayList);
                booksRecyclerView.setAdapter(hardBookECommPurchaseAdapter);
            } else {
                hardBookECommPurchaseAdapter.notifyDataSetChanged();
            }
        }
    }
    private String parseTags(JSONArray tagsArray) throws JSONException {
        StringBuilder tags = new StringBuilder();
        for (int j = 0; j < tagsArray.length(); j++) {
            tags.append(tagsArray.getString(j)).append(", ");
        }
        if (tags.length() > 0) {
            tags.setLength(tags.length() - 2); // Remove trailing comma and space
        }
        return tags.toString();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

