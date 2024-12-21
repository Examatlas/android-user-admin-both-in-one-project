package com.examatlas.fragment;

import static android.app.Activity.RESULT_OK;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.examatlas.R;
import com.examatlas.adapter.AdminShowAllBlogAdapter;
import com.examatlas.adapter.AdminTagsForDataALLAdapter;
import com.examatlas.models.AdminShowAllCategoryModel;
import com.examatlas.models.extraModels.AdminCategoryModel;
import com.examatlas.utils.Constant;
import com.examatlas.utils.MultipartRequest;
import com.examatlas.utils.MySingletonFragment;
import com.examatlas.models.AdminShowAllBlogModel;
import com.examatlas.models.AdminTagsForDataALLModel;
import com.examatlas.utils.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AdminBlogCreateDeleteFragment extends Fragment {

    private SearchView searchView;
    private Button createBlogBtn;
    private Dialog createBlogDialogBox;
    private EditText titleEditTxt, keywordEditTxt, contentEditTxt, tagsEditTxt;
    private Spinner categorySpinner;
    ArrayList<AdminShowAllCategoryModel> categoryModelArrayList;
    String categoryId,categoryName;
    private RecyclerView tagsRecyclerView;
    private AdminTagsForDataALLAdapter adminTagsForDataALLAdapter;
    private AdminTagsForDataALLModel adminTagsForDataALLModel;
    private ArrayList<AdminTagsForDataALLModel> adminTagsForDataALLModelArrayList;
    private ImageView uploadImage,btnCross;
    private TextView uploadImageName;
    Button uploadBlogDetailsBtn;
    private Uri image_uri;
    private final String createBlogURL = Constant.BASE_URL + "blog/createBlog";
    RecyclerView showAllBlogRecyclerView;
    AdminShowAllBlogAdapter adminShowAllBlogAdapter;
    AdminShowAllBlogModel adminShowAllBlogModel;
    ArrayList<AdminShowAllBlogModel> adminShowAllBlogModelArrayList;
    private final String blogURL = Constant.BASE_URL + "blog/getAllBlogs";
    ProgressBar showAllBlogProgressBar;
    RelativeLayout noDataLayout;
    SessionManager sessionManager;
    String authToken;
    private File imageFile;
    // ActivityResultLaunchers
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.admin_fragment_create_blog, container, false);

        searchView = view.findViewById(R.id.searchView);
        createBlogBtn = view.findViewById(R.id.btnCreate);

        showAllBlogRecyclerView = view.findViewById(R.id.showBlogRecycler);
        showAllBlogProgressBar = view.findViewById(R.id.showAllBlogProgressBar);

        noDataLayout = view.findViewById(R.id.noDataLayout);

        adminShowAllBlogModelArrayList = new ArrayList<>();
        showAllBlogRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL,false));

        sessionManager = new SessionManager(getContext());
        authToken = sessionManager.getUserData().get("authToken");

        showAllBlogFunction();
        getCategory();
        // Setup ActivityResultLaunchers
        setupActivityResultLaunchers();

        searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openKeyboard();
            }
        });

        // Set up touch listener for the parent layout
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (searchView.isShown() && !isPointInsideView(event.getRawX(), event.getRawY(), searchView)) {
                        searchView.setIconified(true); // Collapse the search view
                        InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0); // Hide the keyboard
                    }
                }
                return false; // Allow other events to be handled
            }
        });

        createBlogBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openCreateBlogDialog();
            }
        });


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Not needed for this implementation
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Call the filter method when the search text changes
                if (adminShowAllBlogAdapter != null) {
                    adminShowAllBlogAdapter.filter(newText);
                }
                return true;
            }
        });

        return view;
    }

    public void showAllBlogFunction() {
        noDataLayout.setVisibility(View.GONE);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, blogURL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            showAllBlogRecyclerView.setVisibility(View.VISIBLE);
                            showAllBlogProgressBar.setVisibility(View.GONE);
                            boolean status = response.getBoolean("status");

                            if (status) {
                                adminShowAllBlogModelArrayList.clear(); // Clear the list before adding new items

                                JSONObject jsonObject = response.getJSONObject("pagination");
                                String totalRows = jsonObject.getString("totalRows");
                                String totalPages = jsonObject.getString("totalPages");
                                String currentPage = jsonObject.getString("currentPage");
                                JSONArray jsonArray = response.getJSONArray("data");

                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject2 = jsonArray.getJSONObject(i);
                                    String blogID = jsonObject2.getString("_id");
                                    String title = jsonObject2.getString("title");
                                    String keyword = jsonObject2.getString("keyword");
                                    String content = jsonObject2.getString("content");
                                    Log.e("Blog content",content);
                                    JSONObject image = jsonObject2.getJSONObject("image");
                                    String url = image.getString("url");
                                    String categoryName;
                                    if (jsonObject2.has("category")) {
                                        categoryName = jsonObject2.getString("category");
                                    }else {
                                        categoryName =  "null";
                                    }
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

                                    adminShowAllBlogModel = new AdminShowAllBlogModel(blogID,null,categoryName,url, title, keyword, content, tags.toString(), totalRows,totalPages,currentPage);
                                    adminShowAllBlogModelArrayList.add(adminShowAllBlogModel);
                                }
                                // Update the original list in the adapter
                                if (adminShowAllBlogAdapter != null) {
                                    adminShowAllBlogAdapter.updateOriginalList(adminShowAllBlogModelArrayList);
                                }
                                // If you have already created the adapter, just notify the change
                                if (adminShowAllBlogModelArrayList.isEmpty()) {
                                    showAllBlogProgressBar.setVisibility(View.GONE);
                                    noDataLayout.setVisibility(View.VISIBLE);
                                } else {
                                    if (adminShowAllBlogAdapter == null) {
                                        adminShowAllBlogAdapter = new AdminShowAllBlogAdapter(adminShowAllBlogModelArrayList, AdminBlogCreateDeleteFragment.this);
                                        showAllBlogRecyclerView.setAdapter(adminShowAllBlogAdapter);
                                    } else {
                                        adminShowAllBlogAdapter.notifyDataSetChanged();
                                    }
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
                Log.e("BlogFetchError", errorMessage);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "Bearer " + authToken);
                return headers;
            }
        };
        MySingletonFragment.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }

    private void openCreateBlogDialog() {
        createBlogDialogBox = new Dialog(requireContext());
        createBlogDialogBox.setContentView(R.layout.admin_create_blog_dialog_box);

        categorySpinner = createBlogDialogBox.findViewById(R.id.categorySpinner);
        titleEditTxt = createBlogDialogBox.findViewById(R.id.titleEditTxt);
        keywordEditTxt = createBlogDialogBox.findViewById(R.id.keywordEditText);
        contentEditTxt = createBlogDialogBox.findViewById(R.id.contentEditText);
        tagsEditTxt = createBlogDialogBox.findViewById(R.id.tagsEditText);

        tagsRecyclerView = createBlogDialogBox.findViewById(R.id.tagsRecycler);

        uploadBlogDetailsBtn = createBlogDialogBox.findViewById(R.id.btnSubmit);

        btnCross = createBlogDialogBox.findViewById(R.id.btnCross);
        uploadImage = createBlogDialogBox.findViewById(R.id.uploadImage);
        uploadImageName = createBlogDialogBox.findViewById(R.id.txtNoFileChosen);

        adminTagsForDataALLModelArrayList = new ArrayList<>();
        tagsRecyclerView = createBlogDialogBox.findViewById(R.id.tagsRecycler);
        tagsRecyclerView.setLayoutManager(new GridLayoutManager(getContext(),2));
        adminTagsForDataALLAdapter = new AdminTagsForDataALLAdapter(adminTagsForDataALLModelArrayList);
        tagsRecyclerView.setAdapter(adminTagsForDataALLAdapter);

        setupCategorySpinner(categorySpinner, titleEditTxt,keywordEditTxt,contentEditTxt,tagsEditTxt,null);

        tagsEditTxt = createBlogDialogBox.findViewById(R.id.tagsEditText);

        uploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGallery();
            }
        });
        btnCross.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createBlogDialogBox.dismiss();
            }
        });
        tagsEditTxt.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND ||
                    (event != null && event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                String tagText = tagsEditTxt.getText().toString().trim();
                if (!tagText.isEmpty()) {
                    adminTagsForDataALLModelArrayList.add(new AdminTagsForDataALLModel(tagText));
                    adminTagsForDataALLAdapter.notifyItemInserted(adminTagsForDataALLModelArrayList.size() - 1);
                    tagsEditTxt.setText(""); // Clear the EditText
                    tagsRecyclerView.setVisibility(View.VISIBLE); // Show RecyclerView
                }
                return true; // Indicate that we've handled the event
            }
            return false; // Pass the event on
        });
        uploadBlogDetailsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createBlogDetails();
            }
        });

        createBlogDialogBox.show();
        WindowManager.LayoutParams params = createBlogDialogBox.getWindow().getAttributes();
        params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.CENTER;

// Set the window attributes
        createBlogDialogBox.getWindow().setAttributes(params);

// Now, to set margins, you'll need to set it in the root view of the dialog
        FrameLayout layout = (FrameLayout) createBlogDialogBox.findViewById(android.R.id.content);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) layout.getLayoutParams();

// Set top and bottom margins (adjust values as needed)
        layoutParams.setMargins(0, 50, 0, 50);
        layout.setLayoutParams(layoutParams);

// Background and animation settings
        createBlogDialogBox.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        createBlogDialogBox.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        createBlogDialogBox.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

    }

    public void setupCategorySpinner(Spinner categorySpinners,EditText titleEditTxt,EditText keywordEditTxt,EditText contentEditTxt,EditText tagsEditTxt ,AdminShowAllBlogModel currentCategory) {
        // Assuming `subCategoryModelArrayList` contains the categories data
        ArrayList<String> categoryNameList = new ArrayList<>();
        categoryNameList.add("Select Category"); // First item is "Select Category"

        for (int i = 0; i < categoryModelArrayList.size(); i++) {
            // Populate category names from your subCategoryModelArrayList
            categoryNameList.add(categoryModelArrayList.get(i).getCategoryName());
        }

        // Set the adapter for the Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, categoryNameList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinners.setAdapter(adapter);
        if (currentCategory != null) {
            for (int i = 0; i < categoryNameList.size(); i++) {
                if (categoryNameList.get(i).equals(currentCategory.getCategoryName())) {
                    categorySpinners.setSelection(i);
                    categoryName = categoryNameList.get(i);
                }
            }
        }
        // Set the OnItemSelectedListener to handle category selection
        categorySpinners.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                if (position > 0) { // Ensure that a category is selected (not "Select Category")
                    titleEditTxt.setEnabled(true);
                    keywordEditTxt.setEnabled(true);
                    contentEditTxt.setEnabled(true);
                    tagsEditTxt.setEnabled(true);
                    categoryId = categoryModelArrayList.get(position - 1).getId();
                    categoryName = categoryModelArrayList.get(position - 1).getCategoryName();
                } else {
                    categoryId = null;
                    titleEditTxt.setEnabled(false);
                    keywordEditTxt.setEnabled(false);
                    contentEditTxt.setEnabled(false);
                    tagsEditTxt.setEnabled(false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                categoryId = null;
                categoryName = null;
                titleEditTxt.setEnabled(false);
                keywordEditTxt.setEnabled(false);
                contentEditTxt.setEnabled(false);
                tagsEditTxt.setEnabled(false);
            }
        });
    }

    private void openKeyboard() {
        searchView.setIconified(false); // Expands the search view
        searchView.requestFocus(); // Requests focus
        InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(searchView, InputMethodManager.SHOW_IMPLICIT); // Show the keyboard
    }

    private boolean isPointInsideView(float x, float y, View view) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        return (x >= location[0] && x <= (location[0] + view.getWidth()) &&
                y >= location[1] && y <= (location[1] + view.getHeight()));
    }
//    private void setupActivityResultLaunchers() {
//        galleryLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
//            if (result.getResultCode() == RESULT_OK) {
//                Intent data = result.getData();
//                if (data != null) {
//                    image_uri = data.getData();
//                    try {
//                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), image_uri);
//                        bitmap = getResizedBitmap(bitmap, 400);
//                        uploadImage.setImageBitmap(bitmap);
//                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
//                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
//                        byte[] byteArray = stream.toByteArray();
//                        Encoded_images = android.util.Base64.encodeToString(byteArray, android.util.Base64.DEFAULT);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        });
//
//        cameraLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
//            if (result.getResultCode() == RESULT_OK) {
//                Bitmap bitmap = (Bitmap) result.getData().getExtras().get("data");
//                if (bitmap != null) {
//                    bitmap = getResizedBitmap(bitmap, 400);
//                    uploadImage.setImageBitmap(bitmap);
//                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
//                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
//                    byte[] byteArray = stream.toByteArray();
//                    Encoded_images = android.util.Base64.encodeToString(byteArray, android.util.Base64.DEFAULT);
//                    Toast.makeText(getContext(), "Image Uploaded Successfully", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
//    }

    private void setupActivityResultLaunchers() {
        galleryLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                Intent data = result.getData();
                if (data != null) {
                    image_uri = data.getData();
                    handleImageUri(image_uri);
                }
            }
        });

        cameraLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                Bitmap bitmap = (Bitmap) result.getData().getExtras().get("data");
                if (bitmap != null) {
                    handleBitmap(bitmap);
                }
            }
        });
    }
    private void handleImageUri(Uri uri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), uri);
            handleBitmap(bitmap);// Process the Bitmap as you did in handleBitmap()
            uploadImage.setImageBitmap(bitmap);
            // Extract the image name from the URI
            String imageName = getFileName(uri);
            // Set the image name to the TextView (uploadImageName)
            uploadImageName.setText(imageName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private String getFileName(Uri uri) {
        String fileName = null;

        // If the URI is a content URI (which is usually the case for gallery images)
        if (uri.getScheme().equals("content")) {
            Cursor cursor = requireActivity().getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                // Get the column index for the display name (filename)
                int columnIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (columnIndex != -1) {
                    fileName = cursor.getString(columnIndex);
                }
                cursor.close();
            }
        }
        // If the URI is a file URI
        else if (uri.getScheme().equals("file")) {
            fileName = uri.getLastPathSegment(); // Get the last part of the path (filename)
        }

        return fileName != null ? fileName : "Unknown";
    }
    private void handleBitmap(Bitmap bitmap) {
        // Resize image
        bitmap = getResizedBitmap(bitmap, 400);
        uploadImage.setImageBitmap(bitmap);

        // Convert Bitmap to File
        imageFile = bitmapToFile(bitmap);  // Store the imageFile globally

        // Encode the image to Base64 for further use
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();

        // Extract the file name from the imageFile (created from Bitmap)
        String imageName = getFileNameFromFile(imageFile);

        // Set the image name to the TextView (uploadImageName)
        uploadImageName.setText(imageName);

    }
    private String getFileNameFromFile(File file) {
        // Extract the file name from the imageFile (file name is the last segment of the path)
        return file != null ? file.getName() : "Unknown";
    }
    private File bitmapToFile(Bitmap bitmap) {
        File file = new File(getContext().getCacheDir(), "uploaded_image.jpg");
        try (FileOutputStream out = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }
    private void openGallery() {
        final CharSequence[] options = {"Open Camera", "Choose from Gallery", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Add Image");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Open Camera")) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    cameraLauncher.launch(intent); // Use cameraLauncher
                } else if (options[item].equals("Choose from Gallery")) {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    galleryLauncher.launch(Intent.createChooser(intent, "Select Image")); // Use galleryLauncher
                } else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                    Toast.makeText(getContext(), "Image Uploading Cancelled", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.show();
    }

    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();
        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }
    public void createBlogDetails() {
        String title = titleEditTxt.getText().toString().trim();
        String keyword = keywordEditTxt.getText().toString().trim();
        String content = contentEditTxt.getText().toString().trim();

        // Prepare form data
        Map<String, String> params = new HashMap<>();
        params.put("title", title);
        params.put("keyword", keyword);
        params.put("content", content);
        params.put("category",categoryName);

        // Add the tags as a comma-separated string
        StringBuilder tagsBuilder = new StringBuilder();
        for (AdminTagsForDataALLModel tag : adminTagsForDataALLModelArrayList) {
            if (tagsBuilder.length() > 0) {
                tagsBuilder.append(",");
            }
            tagsBuilder.append(tag.getTagName());
        }
        params.put("tags", tagsBuilder.toString());

        // Create a Map for files
        Map<String, File> files = new HashMap<>();

        // If an image is selected, add the image file
        if (imageFile != null && imageFile.exists()) {
            files.put("image", imageFile);
        }

        // Create and send the multipart request
        MultipartRequest multipartRequest = new MultipartRequest(createBlogURL, params, files,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject responseObject = new JSONObject(response);
                            boolean status = responseObject.getBoolean("status");
                            if (status) {
                                String message = responseObject.getString("message");
                                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                                showAllBlogFunction();
                                createBlogDialogBox.dismiss();
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
                                String responseData = new String(error.networkResponse.data, "UTF-8");
                                errorMessage += "\nStatus Code: " + error.networkResponse.statusCode;
                                errorMessage += "\nResponse Data: " + responseData;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                        Log.e("BlogFetchError", errorMessage);
                    }
                }, authToken);

        // Add the request to the queue
        MySingletonFragment.getInstance(this).addToRequestQueue(multipartRequest);
    }
    public  void getCategory(){
        String categoryURL = Constant.BASE_URL + "category/getCategory";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, categoryURL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            boolean status = response.getBoolean("status");
                            if (status) {
                                categoryModelArrayList = new ArrayList<>();
                                JSONArray jsonArray = response.getJSONArray("data");
                                // Parse books directly here
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject2 = jsonArray.getJSONObject(i);
                                    String id = jsonObject2.getString("_id");
                                    String categoryName = jsonObject2.getString("categoryName");
                                    String description = jsonObject2.getString("slug");
                                    String is_active = jsonObject2.getString("is_active");
                                    JSONObject imageObj = jsonObject2.getJSONObject("image");
                                    String imageUrl = imageObj.getString("url");

                                    AdminShowAllCategoryModel categoryModel = new AdminShowAllCategoryModel(id,categoryName,description,is_active,imageUrl);
                                    categoryModelArrayList.add(categoryModel);
                                }
                            }
                        } catch (JSONException e) {
                            Toast.makeText(getContext(), e.toString(), Toast.LENGTH_SHORT).show();
                            Log.e("catch",e.toString());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getContext(), error.toString(), Toast.LENGTH_SHORT).show();
                Log.e("onErrorResponse",error.toString());
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "Bearer " + authToken);
                return headers;
            }
        };
        MySingletonFragment.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }
    public String getCategoryName(){
        return categoryName;
    }
}