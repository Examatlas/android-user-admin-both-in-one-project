package com.examatlas.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
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
import com.examatlas.activities.Books.CartViewActivity;
import com.examatlas.activities.Books.SearchingBooksActivity;
import com.examatlas.activities.Books.WishListActivity;
import com.examatlas.activities.LoginWithEmailActivity;
import com.examatlas.adapter.books.AllBookShowingAdapter;
import com.examatlas.adapter.books.CategoryAdapter;
import com.examatlas.models.Books.AllBooksModel;
import com.examatlas.models.Books.CategoryModel;
import com.examatlas.utils.Constant;
import com.examatlas.utils.MySingletonFragment;
import com.examatlas.utils.SessionManager;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BooksFragment extends Fragment {
    ImageView cartBtn,wishListBtn,logo;
    TextView cartItemCountTxt;
    private ImageSlider slider;
    CardView sliderCardView;
    ArrayList<SlideModel> sliderArrayList;
    private RecyclerView booksRecyclerView,categoryRecyclerView,bookForUserRecyclerView,bookBestSellerRecyclerView;
    private ArrayList<AllBooksModel> allBooksModelArrayList;
    private SessionManager sessionManager;
    private String token;
//    private RelativeLayout noDataLayout;
    private EditText searchView;
    private final String bookURL = Constant.BASE_URL + "v1/books?type=book";
    private final String categoryURL = Constant.BASE_URL + "v1/category";
    int totalPage,totalItems;
    ArrayList<CategoryModel> categoryArrayList = new ArrayList<>();
    NestedScrollView nestedScrollView;
    RelativeLayout toolbarRelativeLayout;
    FrameLayout searchViewFrameLayout;
    ShimmerFrameLayout sliderShimmerLayout,categoryShimmerLayout,bookForUsrShimmerLayout,bookBestSellerShimmerLayout;
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_books, container, false);

        getActivity().getWindow().setStatusBarColor(ContextCompat.getColor(getContext(), R.color.md_theme_dark_surfaceTint));

        logo = view.findViewById(R.id.logo);
        cartBtn = view.findViewById(R.id.cartBtn);
        wishListBtn = view.findViewById(R.id.wishListBtn);
        slider = view.findViewById(R.id.slider);
        nestedScrollView = view.findViewById(R.id.nestScrollView);
        searchViewFrameLayout = view.findViewById(R.id.searchViewFrameLayout);
        toolbarRelativeLayout = view.findViewById(R.id.hardbook_ecomm_purchase_toolbar);
        booksRecyclerView = view.findViewById(R.id.booksRecycler);

        categoryRecyclerView = view.findViewById(R.id.categoryRecycler);
        sliderCardView = view.findViewById(R.id.slider_cardView);
        sliderShimmerLayout = view.findViewById(R.id.shimmer_Slider_container);
        sliderCardView.setVisibility(View.GONE);
        categoryShimmerLayout = view.findViewById(R.id.shimmer_category_container);
        categoryRecyclerView.setVisibility(View.GONE);
        categoryShimmerLayout.setVisibility(View.VISIBLE);
        categoryShimmerLayout.startShimmer();

        bookForUserRecyclerView = view.findViewById(R.id.booksForUserRecycler);
        bookForUsrShimmerLayout = view.findViewById(R.id.shimmer_for_user_container);
        bookForUserRecyclerView.setVisibility(View.GONE);
        bookForUsrShimmerLayout.setVisibility(View.VISIBLE);
        bookForUsrShimmerLayout.startShimmer();

        bookBestSellerRecyclerView = view.findViewById(R.id.booksBestSellerRecycler);
        bookBestSellerShimmerLayout = view.findViewById(R.id.shimmer_Best_selling_container);
        bookBestSellerRecyclerView.setVisibility(View.GONE);
        bookBestSellerShimmerLayout.setVisibility(View.VISIBLE);
        bookBestSellerShimmerLayout.startShimmer();

//        noDataLayout = view.findViewById(R.id.noDataLayout);

        searchView = view.findViewById(R.id.search_icon);
        searchView.setFocusable(false);
        searchView.setClickable(true);
//        cartIcon = findViewById(R.id.cartBtn);
        allBooksModelArrayList = new ArrayList<>();
        sessionManager = new SessionManager(getContext());
        token = sessionManager.getUserData().get("authToken");

        cartItemCountTxt = view.findViewById(R.id.cartItemCountTxt);
        String quantity = sessionManager.getCartQuantity();
        if (!quantity.equals("0")) {
            cartItemCountTxt.setVisibility(View.VISIBLE);
            cartItemCountTxt.setText(quantity);
        }else {
            cartItemCountTxt.setVisibility(View.GONE);
        }
        booksRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false));
        bookForUserRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false));
        bookBestSellerRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false));

        searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), SearchingBooksActivity.class);
                startActivity(intent);
            }
        });
        cartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sessionManager.IsLoggedIn()) {
                    Intent intent = new Intent(getContext(), CartViewActivity.class);
                    startActivity(intent);
                }else {
                    new MaterialAlertDialogBuilder(getContext())
                            .setTitle("Login")
                            .setMessage("You need to login to view cart")
                            .setPositiveButton("Proceed to Login", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Intent intent = new Intent(getContext(), LoginWithEmailActivity.class);
                                    startActivity(intent);
                                }
                            }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Toast.makeText(getContext(), "Cancelled", Toast.LENGTH_SHORT).show();
                                }
                            }).show();
                }
            }
        });
        wishListBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), WishListActivity.class));
            }
        });

        getAlLCategory();
        getAllBooks();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        String quantity = sessionManager.getCartQuantity();
        if (!quantity.equals("0")) {
            cartItemCountTxt.setVisibility(View.VISIBLE);
            cartItemCountTxt.setText(quantity);
        }else {
            cartItemCountTxt.setVisibility(View.GONE);
        }
    }

    private void setupImageSlider() {
        sliderArrayList = new ArrayList<>();
        sliderArrayList.add(new SlideModel(R.drawable.image1, ScaleTypes.CENTER_CROP));
        sliderArrayList.add(new SlideModel(R.drawable.image2, ScaleTypes.CENTER_CROP));
        sliderArrayList.add(new SlideModel(R.drawable.image3, ScaleTypes.CENTER_CROP));
        slider.setImageList(sliderArrayList);
    }

    private void getAlLCategory() {
        String paginatedURL = categoryURL;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, paginatedURL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            categoryRecyclerView.setVisibility(View.VISIBLE);
                            boolean status = response.getBoolean("success");
                            if (status) {
                                JSONArray jsonArray = response.getJSONArray("data");
                                categoryArrayList.clear();
                                totalPage = Integer.parseInt(response.getString("totalPage"));
                                totalItems = Integer.parseInt(response.getString("totalItems"));

                                // Parse books directly here
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject2 = jsonArray.getJSONObject(i);

                                    // Convert the book object into a Map to make it dynamic
                                    Map<String, Object> categoryData = new Gson().fromJson(jsonObject2.toString(), Map.class);

                                    // Extract image URL from the "imageUrl" field, handle missing image gracefully
                                    JSONObject imgObj = jsonObject2.optJSONObject("imageUrl");
                                    String imageURL = null;
                                    if (imgObj != null) {
                                        imageURL = imgObj.optString("url", ""); // Default to empty string if URL is not present
                                    }
                                    // Handle missing image gracefully
                                    if (imageURL == null || imageURL.isEmpty()) {
                                        imageURL = "default_image_url"; // Placeholder for missing images
                                    }

                                    CategoryModel model = new CategoryModel(categoryData,imageURL); // Pass the map to the model
                                    // Add the model to the list
                                    categoryArrayList.add(model);
                                }
                                if (categoryArrayList.size()>10){
                                    categoryRecyclerView.setLayoutManager(new GridLayoutManager(getContext(),2,GridLayoutManager.HORIZONTAL,false));
                                }else {
                                    categoryRecyclerView.setLayoutManager(new GridLayoutManager(getContext(),5,GridLayoutManager.VERTICAL,false));
                                }
                                // Update UI and adapters
                                categoryRecyclerView.setAdapter(new CategoryAdapter(categoryArrayList, getContext()));
                                categoryShimmerLayout.stopShimmer();
                                categoryShimmerLayout.setVisibility(View.GONE);
                                categoryRecyclerView.setVisibility(View.VISIBLE);
                                sliderShimmerLayout.stopShimmer();
                                sliderShimmerLayout.setVisibility(View.GONE);
                                sliderCardView.setVisibility(View.VISIBLE);
                                setupImageSlider();
                            } else {
                                Toast.makeText(getContext(), response.getString("message"), Toast.LENGTH_SHORT).show();
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
                                String jsonError = new String(error.networkResponse.data);
                                JSONObject jsonObject = new JSONObject(jsonError);
                                String message = jsonObject.optString("message", "Unknown error");
                                Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
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

        MySingletonFragment.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }

    private void getAllBooks() {
        String paginatedURL = bookURL;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, paginatedURL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            booksRecyclerView.setVisibility(View.VISIBLE);
                            boolean status = response.getBoolean("success");
                            if (status) {
                                JSONArray jsonArray = response.getJSONArray("data");
                                allBooksModelArrayList.clear();
                                totalPage = Integer.parseInt(response.getString("totalPage"));
                                totalItems = Integer.parseInt(response.getString("totalItems"));

                                // Parse books directly here
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject2 = jsonArray.getJSONObject(i);

                                    // Convert the book object into a Map to make it dynamic
                                    Map<String, Object> bookData = new Gson().fromJson(jsonObject2.toString(), Map.class);

                                    // Extract category and subCategory names
                                    String categoryName = "";
                                    String subCategoryName = "";

                                    // Check if categoryData exists and extract categoryName
                                    if (bookData.containsKey("categoryData")) {
                                        Map<String, Object> categoryData = (Map<String, Object>) bookData.get("categoryData");
                                        categoryName = categoryData.containsKey("categoryName") ? categoryData.get("categoryName").toString() : "";
                                    }

                                    // Check if subCategoryData exists and extract subCategoryName
                                    if (bookData.containsKey("subCategoryData")) {
                                        Map<String, Object> subCategoryData = (Map<String, Object>) bookData.get("subCategoryData");
                                        subCategoryName = subCategoryData.containsKey("name") ? subCategoryData.get("name").toString() : "";
                                    }

                                    // Extract dimensions (assuming they are present in the 'dimensions' field of the book data)
                                    String length = "";
                                    String width = "";
                                    String height = "";
                                    String weight = "";


                                    if (bookData.containsKey("dimensions")) {
                                        Object dimensionsObj = bookData.get("dimensions");

                                        if (dimensionsObj instanceof Map) {
                                            // If it's already a Map, we can safely cast
                                            Map<String, Object> dimensions = (Map<String, Object>) dimensionsObj;
                                            length = dimensions.containsKey("length") ? dimensions.get("length").toString() : "";
                                            width = dimensions.containsKey("width") ? dimensions.get("width").toString() : "";
                                            height = dimensions.containsKey("height") ? dimensions.get("height").toString() : "";
                                            weight = dimensions.containsKey("weight") ? dimensions.get("weight").toString() : "";
                                        } else if (dimensionsObj instanceof String) {
                                            // If it's a String (likely JSON), parse it into a Map
                                            String dimensionsJson = dimensionsObj.toString();
                                            try {
                                                Map<String, Object> dimensions = new Gson().fromJson(dimensionsJson, Map.class);
                                                length = dimensions.containsKey("length") ? dimensions.get("length").toString() : "";
                                                width = dimensions.containsKey("width") ? dimensions.get("width").toString() : "";
                                                height = dimensions.containsKey("height") ? dimensions.get("height").toString() : "";
                                                weight = dimensions.containsKey("weight") ? dimensions.get("weight").toString() : "";
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                                // Handle parsing errors here if necessary
                                            }
                                        }
                                    }

                                    // Pass the data and dimensions to the model constructor
                                    AllBooksModel model = new AllBooksModel(bookData, length, width, height, weight,categoryName,subCategoryName); // Pass map and dimensions

                                    // Add the model to the list
                                    allBooksModelArrayList.add(model);
                                }

                                // Update UI and adapters
                                bookForUserRecyclerView.setAdapter(new AllBookShowingAdapter(getContext(), allBooksModelArrayList));
                                bookForUsrShimmerLayout.stopShimmer();
                                bookForUsrShimmerLayout.setVisibility(View.GONE);
                                bookForUserRecyclerView.setVisibility(View.VISIBLE);
                                bookBestSellerRecyclerView.setAdapter(new AllBookShowingAdapter(getContext(), allBooksModelArrayList));
                                bookBestSellerShimmerLayout.stopShimmer();
                                bookBestSellerShimmerLayout.setVisibility(View.GONE);
                                bookBestSellerRecyclerView.setVisibility(View.VISIBLE);

                                if (allBooksModelArrayList.isEmpty()) {
//                                    noDataLayout.setVisibility(View.VISIBLE);
                                    booksRecyclerView.setVisibility(View.GONE);
                                } else {
                                    booksRecyclerView.setAdapter(new AllBookShowingAdapter(getContext(), allBooksModelArrayList));
                                }
                            } else {
                                Toast.makeText(getContext(), response.getString("message"), Toast.LENGTH_SHORT).show();
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
                                String jsonError = new String(error.networkResponse.data);
                                JSONObject jsonObject = new JSONObject(jsonError);
                                String message = jsonObject.optString("message", "Unknown error");
                                Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
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

        MySingletonFragment.getInstance(this).addToRequestQueue(jsonObjectRequest);
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
}

