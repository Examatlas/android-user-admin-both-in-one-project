package com.examatlas.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
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
import com.examatlas.activities.CurrentAffairsActivity;
import com.examatlas.activities.DashboardActivity;
import com.examatlas.activities.HardBookECommPurchaseActivity;
import com.examatlas.adapter.BlogAdapter;
import com.examatlas.adapter.CurrentAffairsAdapter;
import com.examatlas.adapter.LiveCoursesAdapter;
import com.examatlas.adapter.books.BookForUserAdapter;
import com.examatlas.adapter.books.HomeFragmentBookAdapter;
import com.examatlas.models.BlogModel;
import com.examatlas.models.Books.AllBooksModel;
import com.examatlas.models.CurrentAffairsModel;
import com.examatlas.models.LiveCoursesModel;
import com.examatlas.models.Books.BookImageModels;
import com.examatlas.utils.Constant;
import com.examatlas.utils.MySingleton;
import com.examatlas.utils.SessionManager;
import com.examatlas.utils.MySingletonFragment;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class HomeFragment extends Fragment {
    RecyclerView liveClassesRecycler, booksRecycler, blogsRecycler, currentAffairRecycler;
    ProgressBar homeProgress;
    SessionManager sessionManager;
    ImageSlider slider;
    ArrayList<BlogModel> blogModelArrayList;
    ArrayList<CurrentAffairsModel> currentAffairsModelArrayList;
    ArrayList<LiveCoursesModel> liveCoursesModelArrayList;
    private ArrayList<AllBooksModel> allBooksModelArrayList;
    BlogAdapter blogAdapter;
    LiveCoursesAdapter liveCoursesAdapter;
    CurrentAffairsAdapter currentAffairAdapter;
    private final String bookURL = Constant.BASE_URL + "v1/books";
    private final String blogURL = Constant.BASE_URL + "blog/getAllBlogs";
    private final String liveClassURL = Constant.BASE_URL + "liveclass/getAllLiveClass";
    private final String currentAffairsURL = Constant.BASE_URL + "currentAffair/getAllCA";
    String token;
    ShimmerFrameLayout booksShimmeringLayout,blogShimmeringLayout,currentAffairShimmeringLayout;
    TextView viewAllBlog,viewAllCA;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);
        liveClassesRecycler = view.findViewById(R.id.examRecycler);
        currentAffairRecycler = view.findViewById(R.id.currentAffairRecycler);
        blogsRecycler = view.findViewById(R.id.blogsRecycler);
        booksRecycler = view.findViewById(R.id.booksRecycler);
        homeProgress = view.findViewById(R.id.homeProgress);

        booksShimmeringLayout = view.findViewById(R.id.shimmer_Book_container);
        blogShimmeringLayout = view.findViewById(R.id.shimmer_blog_container);
        currentAffairShimmeringLayout = view.findViewById(R.id.shimmer_CA_container);

        viewAllBlog = view.findViewById(R.id.viewAllBlogTxt);
        viewAllCA = view.findViewById(R.id.viewAllCATxt);

        slider = view.findViewById(R.id.slider);
        ArrayList<SlideModel> sliderArrayList = new ArrayList<>();
        allBooksModelArrayList = new ArrayList<>();
        blogModelArrayList = new ArrayList<>();
        liveCoursesModelArrayList = new ArrayList<>();
        currentAffairsModelArrayList = new ArrayList<>();
        sessionManager = new SessionManager(getContext());

        sliderArrayList.add(new SlideModel(R.drawable.image1, ScaleTypes.CENTER_CROP));
        sliderArrayList.add(new SlideModel(R.drawable.image2, ScaleTypes.CENTER_CROP));
        sliderArrayList.add(new SlideModel(R.drawable.image3, ScaleTypes.CENTER_CROP));

        slider.setImageList(sliderArrayList);

        liveClassesRecycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        booksRecycler.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false));
        blogsRecycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        currentAffairRecycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));liveClassesRecycler.setVisibility(View.GONE);
        blogsRecycler.setVisibility(View.GONE);
        token = sessionManager.getUserData().get("authToken");

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //        getLiveClasses();
                getAllBooks();
                getBlogList();
                getCurrentAffairs();
            }
        },1000);

        viewAllBlog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Check if the activity hosting the fragment is an instance of DashboardActivity
                if (getActivity() instanceof DashboardActivity) {
                    DashboardActivity activity = (DashboardActivity) getActivity();

                    // Check if the current fragment is an instance of HomeFragment
                    Fragment currentFragment = activity.getSupportFragmentManager().findFragmentById(R.id.container);

                    if (currentFragment instanceof HomeFragment) {
                        // Begin fragment transaction to replace HomeFragment with BlogFragment
                        FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.container, new BlogFragment());
                        transaction.addToBackStack(null); // Optional, adds fragment to back stack
                        transaction.commit();

                        // Update the bottom navigation to select the 'Blogs' item
                        activity.bottom_navigation.setSelectedItemId(R.id.blogs);
                    }
                }
            }
        });


        viewAllCA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), CurrentAffairsActivity.class));
            }
        });
        return view;
    }

    private void getLiveClasses() {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, liveClassURL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            liveClassesRecycler.setVisibility(View.VISIBLE);
                            boolean status = response.getBoolean("status");

                            if (status) {
                                JSONArray jsonArray = response.getJSONArray("classes");
                                liveCoursesModelArrayList.clear();

                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject2 = jsonArray.getJSONObject(i);
                                    String classID = jsonObject2.getString("_id");
                                    String title = jsonObject2.getString("title");
                                    String subTitle = jsonObject2.getString("subTitle");
                                    String description = jsonObject2.getString("description");
                                    String teacher = jsonObject2.getString("teacher");
                                    String language = jsonObject2.getString("language");
                                    String price = jsonObject2.getString("price");
                                    String categoryId = jsonObject2.getString("categoryId");
                                    String subCategoryId = jsonObject2.getString("subCategoryId");
                                    String subjectId = jsonObject2.getString("subjectId");
                                    String courseContent = jsonObject2.getString("courseContent");
                                    String isActive = jsonObject2.getString("is_active");
                                    String finalPrice = jsonObject2.getString("finalPrice");

                                    // Use StringBuilder for tags
                                    StringBuilder tags = new StringBuilder();
                                    JSONArray jsonArray1 = jsonObject2.getJSONArray("tags");
                                    for (int j = 0; j < jsonArray1.length(); j++) {
                                        String singleTag = jsonArray1.getString(j);
                                        tags.append(singleTag).append(", ");
                                    }
                                    if (tags.length() > 0) {
                                        tags.setLength(tags.length() - 2);
                                    }
                                    JSONArray jsonImageArray = jsonObject2.getJSONArray("images");

                                    ArrayList<BookImageModels> bookImageArrayList = new ArrayList<>();

                                    for (int j = 0; j < jsonImageArray.length(); j++) {
                                        JSONObject jsonImageObject = jsonImageArray.getJSONObject(j);
                                        BookImageModels bookImageModels = new BookImageModels(
                                                jsonImageObject.getString("url"),
                                                jsonImageObject.getString("filename"));
                                        bookImageArrayList.add(bookImageModels);
                                    }

                                    String startDate = "", endDate = "";
                                    if (jsonObject2.has("startDate")) {
                                        startDate = jsonObject2.getString("startDate");
                                        endDate = jsonObject2.getString("endDate");
                                    }

                                    JSONArray jsonStudentArray = jsonObject2.getJSONArray("students");
                                    ArrayList<BookImageModels> studentsArrayList = new ArrayList<>();
//                                    for (int j = 0; j<jsonImageArray.length();j++){
//                                    }
                                    JSONArray jsonLiveClasses = jsonObject2.getJSONArray("liveClasses");
                                    ArrayList<BookImageModels> liveClassesArrayList = new ArrayList<>();
//                                    for (int j = 0; j<jsonImageArray.length();j++){
//                                    }
                                    LiveCoursesModel liveCoursesModel = new LiveCoursesModel(classID, title,subTitle, description,language,price,teacher,tags.toString(), categoryId, subCategoryId, subjectId,courseContent,isActive,finalPrice, startDate, endDate,bookImageArrayList,null, null, null);

                                    liveCoursesModelArrayList.add(liveCoursesModel);
                                }
                                if (liveCoursesAdapter == null) {
                                    liveCoursesAdapter = new LiveCoursesAdapter(liveCoursesModelArrayList, HomeFragment.this);
                                    liveClassesRecycler.setAdapter(liveCoursesAdapter);
                                } else {
                                    liveCoursesAdapter.notifyDataSetChanged();
                                }
                            } else {
                                String message = response.getString("message");
                                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Toast.makeText(getContext(), e.toString(), Toast.LENGTH_SHORT).show();
                            Log.e("Live Classes Error", e.getMessage());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getContext(), error.toString(), Toast.LENGTH_SHORT).show();
                Log.e("Live Classes Error", error.getMessage());
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
                            boolean status = response.getBoolean("success");
                            if (status) {
                                JSONArray jsonArray = response.getJSONArray("data");
                                allBooksModelArrayList.clear();

                                // Parse books directly here
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject2 = jsonArray.getJSONObject(i);

                                    // Convert the book object into a Map to make it dynamic
                                    Map<String, Object> bookData = new Gson().fromJson(jsonObject2.toString(), Map.class);
                                    AllBooksModel model = new AllBooksModel(bookData); // Pass the map to the model

                                    allBooksModelArrayList.add(model);
                                }

                                // Update UI and adapters
                                booksRecycler.setAdapter(new HomeFragmentBookAdapter(HomeFragment.this, allBooksModelArrayList));
                                booksShimmeringLayout.stopShimmer();
                                booksShimmeringLayout.setVisibility(View.GONE);
                                booksRecycler.setVisibility(View.VISIBLE);
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
    private void getBlogList() {
        // Create a JsonObjectRequest for the GET request
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, blogURL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            blogShimmeringLayout.stopShimmer();
                            blogShimmeringLayout.setVisibility(View.GONE);
                            blogsRecycler.setVisibility(View.VISIBLE);
                            boolean status = response.getBoolean("status");

                            if (status) {
                                JSONArray jsonArray = response.getJSONArray("data");
                                blogModelArrayList.clear(); // Clear the list before adding new items

                                JSONObject jsonObject = response.getJSONObject("pagination");
                                String totalRows = jsonObject.getString("totalRows");
                                String totalPages = jsonObject.getString("totalPages");
                                String currentPage = jsonObject.getString("currentPage");

                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject2 = jsonArray.getJSONObject(i);
                                    String blogID = jsonObject2.getString("_id");
                                    String title = jsonObject2.getString("title");
                                    String keyword = jsonObject2.getString("keyword");
                                    String content = jsonObject2.getString("content");
                                    Log.e("Blog content",content);
                                    JSONObject image = jsonObject2.getJSONObject("image");
                                    String url = image.getString("url");

                                    // Use StringBuilder for tags
                                    StringBuilder tags = new StringBuilder();
                                    JSONArray jsonArray1 = jsonObject2.getJSONArray("tags");
                                    for (int j = 0; j < jsonArray1.length(); j++) {
                                        String singleTag = jsonArray1.getString(j);
                                        tags.append(singleTag).append(", ");
                                    }
                                    // Remove trailing comma and space if any
                                    if (tags.length() > 0) {
                                        tags.setLength(tags.length() - 2);
                                    }

                                    BlogModel blogModel = new BlogModel(blogID,url, title, keyword, content, tags.toString(),totalRows,totalPages,currentPage);
                                    blogModelArrayList.add(blogModel);
                                }

                                // If you have already created the adapter, just notify the change
                                if (blogAdapter == null) {
                                    blogAdapter = new BlogAdapter(blogModelArrayList, HomeFragment.this);
                                    blogsRecycler.setAdapter(blogAdapter);
                                } else {
                                    blogAdapter.notifyDataSetChanged();
                                }
                            } else {
                                // Handle the case where status is false
                                String message = response.getString("message");
                                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Log.e("JSON_ERROR", "Error parsing JSON: " + e.getMessage());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String errorMessage = "Error: " + error.toString();
                if (error.networkResponse != null) {
                    try {
                        String responseData = new String(error.networkResponse.data, "UTF-8");
                        errorMessage += "\nStatus Code: " + error.networkResponse.statusCode;
                        errorMessage += "\nResponse Data: " + responseData;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();}
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


    private void getCurrentAffairs() {
        // Create a JsonObjectRequest for the GET request
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, currentAffairsURL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            currentAffairShimmeringLayout.stopShimmer();
                            currentAffairShimmeringLayout.setVisibility(View.GONE);
                            currentAffairRecycler.setVisibility(View.VISIBLE);
                            boolean status = response.getBoolean("status");

                            if (status) {
                                JSONArray jsonArray = response.getJSONArray("data");
                                currentAffairsModelArrayList.clear(); // Clear the list before adding new items

                                JSONObject jsonObject2 = response.getJSONObject("pagination");
                                String totalRows = jsonObject2.getString("totalRows");
                                String totalPages = jsonObject2.getString("totalPages");
                                String currentPage = jsonObject2.getString("currentPage");

                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    String affairID = jsonObject.getString("_id");
                                    String title = jsonObject.getString("title");
                                    String keyword = jsonObject.getString("keyword");
                                    String content = jsonObject.getString("content");
                                    JSONObject image = jsonObject.getJSONObject("image");
                                    String url = image.getString("url");


                                    // Use StringBuilder for tags
                                    StringBuilder tags = new StringBuilder();
                                    JSONArray tagsArray = jsonObject.getJSONArray("tags");
                                    for (int j = 0; j < tagsArray.length(); j++) {
                                        String singleTag = tagsArray.getString(j);
                                        tags.append(singleTag).append(", ");
                                    }
                                    // Remove trailing comma and space if any
                                    if (tags.length() > 0) {
                                        tags.setLength(tags.length() - 2); // Adjust to remove the last comma and space
                                    }

                                    CurrentAffairsModel currentAffairModel = new CurrentAffairsModel(affairID,url, title, keyword, content, tags.toString(),totalRows, totalPages,currentPage);
                                    currentAffairsModelArrayList.add(currentAffairModel);
                                }

                                // If you have already created the adapter, just notify the change
                                if (currentAffairAdapter == null) {
                                    currentAffairAdapter = new CurrentAffairsAdapter(currentAffairsModelArrayList, HomeFragment.this,null);
                                    currentAffairRecycler.setAdapter(currentAffairAdapter);
                                } else {
                                    currentAffairAdapter.notifyDataSetChanged();
                                }
                            } else {
                                // Handle the case where status is false
                                String message = response.getString("message");
                                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Log.e("JSON_ERROR", "Error parsing JSON: " + e.getMessage());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String errorMessage = "Error: " + error.toString();
                if (error.networkResponse != null) {
                    try {
                        String responseData = new String(error.networkResponse.data, "UTF-8");
                        errorMessage += "\nStatus Code: " + error.networkResponse.statusCode;
                        errorMessage += "\nResponse Data: " + responseData;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
//                Log.e("CurrentAffairsFetchError", errorMessage);
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

        // Use the MySingleton instance to add the request to the queue
        MySingleton.getInstance(getContext()).addToRequestQueue(jsonObjectRequest);
    }
}
