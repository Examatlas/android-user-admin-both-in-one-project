package com.examatlas.adapter;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.examatlas.R;
import com.examatlas.fragment.AdminCreateCategoryFragment;
import com.examatlas.models.AdminShowAllCategoryModel;
import com.examatlas.utils.Constant;
import com.examatlas.utils.MySingletonFragment;
import com.examatlas.utils.SessionManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AdminShowAllCategoryAdapter extends RecyclerView.Adapter<AdminShowAllCategoryAdapter.ViewHolder> {
    private ArrayList<AdminShowAllCategoryModel> categoryModelArrayList;
    private ArrayList<AdminShowAllCategoryModel> originalCategoryModelArrayList;
    private Fragment context;
    private String currentQuery = "";
    SessionManager sessionManager;
    String authToken;
    public AdminShowAllCategoryAdapter(ArrayList<AdminShowAllCategoryModel> categoryModelArrayList, Fragment context) {
        this.originalCategoryModelArrayList = new ArrayList<>(categoryModelArrayList);
        this.categoryModelArrayList = new ArrayList<>(originalCategoryModelArrayList);
        this.context = context;
        sessionManager = new SessionManager(context.getActivity());
        authToken = sessionManager.getUserData().get("authToken");
    }
    @NonNull
    @Override
    public AdminShowAllCategoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.admin_create_category_item_layout, parent, false);
        return new AdminShowAllCategoryAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminShowAllCategoryAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        AdminShowAllCategoryModel currentCategory = categoryModelArrayList.get(position);
        holder.itemView.setTag(currentCategory);

        holder.setHighlightedText(holder.title, currentCategory.getCategoryName(), currentQuery);

        String imageUrl = categoryModelArrayList.get(position).getImageUrl();
        Glide.with(context.getContext())
                .load(imageUrl)
                .error(R.drawable.image2)
                .into(holder.description);

        holder.editSubjectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openEditCategoryDialogBox(currentCategory,position);
            }
        });
        holder.deleteSubjectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MaterialAlertDialogBuilder(context.getContext())
                        .setTitle("Delete Category")
                        .setMessage("Are you sure you want to delete this category?")
                        .setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                deleteCategory(categoryModelArrayList.get(position));
                            }
                        }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Toast.makeText(context.getContext(), "Cancelled", Toast.LENGTH_SHORT).show();
                            }
                        }).show();
            }
        });

    }

    private void deleteCategory(AdminShowAllCategoryModel adminShowAllCategoryModel) {
        String id = adminShowAllCategoryModel.getId();
        String deleteURL = Constant.BASE_URL + "category/deleteCategory/" + id;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.DELETE, deleteURL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            boolean status = response.getBoolean("status");
                            if (status) {
                                String message = response.getString("message");
                                Toast.makeText(context.getContext(), message, Toast.LENGTH_SHORT).show();
                                categoryModelArrayList.remove(adminShowAllCategoryModel);
                                notifyDataSetChanged();
                                ((AdminCreateCategoryFragment) context).getAllCategory();
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
                Toast.makeText(context.getContext(), errorMessage, Toast.LENGTH_LONG).show();
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
        MySingletonFragment.getInstance(context).addToRequestQueue(jsonObjectRequest);
    }
    private void openEditCategoryDialogBox(AdminShowAllCategoryModel currentCategory, int position) {
        Dialog dialog = new Dialog(context.getContext());
        dialog.setContentView(R.layout.admin_create_category_dialog_box);

        EditText titleEditTxt = dialog.findViewById(R.id.titleEditTxt);
        titleEditTxt.setText(currentCategory.getCategoryName());
        EditText slugEditText = dialog.findViewById(R.id.slugEditText);
        slugEditText.setText(currentCategory.getSlug());
        ImageView imageView = dialog.findViewById(R.id.uploadImage);

        Glide.with(context)
                .load(categoryModelArrayList.get(position).getImageUrl())
                .error(R.drawable.image1)
                .into(imageView);

        Button submitBtn = dialog.findViewById(R.id.btnSubmit);
        ImageView crossBtn = dialog.findViewById(R.id.btnCross);

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendUpdateSubjectDetails(position,currentCategory,titleEditTxt.getText().toString().trim(),slugEditText.getText().toString().trim(),dialog);
            }
        });
        crossBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.show();
        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.CENTER;

// Set the window attributes
        dialog.getWindow().setAttributes(params);

// Now, to set margins, you'll need to set it in the root view of the dialog
        FrameLayout layout = (FrameLayout) dialog.findViewById(android.R.id.content);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) layout.getLayoutParams();

// Set top and bottom margins (adjust values as needed)
        layoutParams.setMargins(0, 50, 0, 50);
        layout.setLayoutParams(layoutParams);

// Background and animation settings
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

    }

    private void sendUpdateSubjectDetails(int position, AdminShowAllCategoryModel currentCategory, String name, String slug,  Dialog dialog) {
        String updateURL = Constant.BASE_URL + "category/createCategory";
        // Create JSON object to send in the request
        JSONObject categoryObject = new JSONObject();
        try {
            categoryObject.put("id", currentCategory.getId());
            categoryObject.put("categoryName", name);
            categoryObject.put("slug", slug);

        } catch (JSONException e) {
            Log.e("JSON_ERROR", "Error creating JSON object: " + e.getMessage());
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, updateURL, categoryObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            boolean status = response.getBoolean("status");
                            if (status) {
                                Toast.makeText(context.getContext(), "Blog Updated Successfully", Toast.LENGTH_SHORT).show();
                                updateSubjectInList(currentCategory, name,slug);
                                ((AdminCreateCategoryFragment) context).getAllCategory();
                                notifyItemChanged(position);
                                dialog.dismiss();
                            } else {
                                Toast.makeText(context.getContext(), "Failed to update blog", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Log.e("JSON_ERROR", "Error parsing JSON response: " + e.getMessage());
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
                Toast.makeText(context.getContext(), errorMessage, Toast.LENGTH_LONG).show();
                Log.e("BlogUpdateError", errorMessage);
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "Bearer " + authToken);
                return headers;
            }
        };
        MySingletonFragment.getInstance(context).addToRequestQueue(jsonObjectRequest);
    }

    private void updateSubjectInList(AdminShowAllCategoryModel currentCategory, String name, String description) {
        // Find the subject in the list
        for (int i = 0; i < categoryModelArrayList.size(); i++) {
            if (categoryModelArrayList.get(i).getId().equals(currentCategory.getId())) {
                // Update the title of the subject in the list
                categoryModelArrayList.get(i).setCategoryName(name);
                categoryModelArrayList.get(i).setSlug(description);
                notifyItemChanged(i);
                break;
            }
        }
    }

    @Override
    public int getItemCount() {
        return categoryModelArrayList.size();
    }

    public void filter(String query) {
        currentQuery = query; // Store current query
        categoryModelArrayList.clear();
        if (query.isEmpty()) {
            categoryModelArrayList.addAll(originalCategoryModelArrayList); // Restore the original list if no query
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (AdminShowAllCategoryModel categoryModel : originalCategoryModelArrayList) {
                if (categoryModel.getCategoryName().toLowerCase().contains(lowerCaseQuery) || categoryModel.getSlug().toLowerCase().contains(lowerCaseQuery)) {
                    categoryModelArrayList.add(categoryModel); // Add matching eBook to the filtered list
                }
            }
        }
        notifyDataSetChanged(); // Notify adapter of data change
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        ImageView editSubjectBtn, deleteSubjectBtn;
        ImageView description;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.titleTxt);
            description = itemView.findViewById(R.id.categoryImageView);
            editSubjectBtn = itemView.findViewById(R.id.editTitleBtn);
            deleteSubjectBtn = itemView.findViewById(R.id.deleteSubjectBtn);
        }

        public void setHighlightedText(TextView textView, String text, String query) {
            if (query == null || query.isEmpty()) {
                textView.setText(text);
                return;
            }
            SpannableString spannableString = new SpannableString(text);
            int startIndex = text.toLowerCase().indexOf(query.toLowerCase());
            while (startIndex >= 0) {
                int endIndex = startIndex + query.length();
                spannableString.setSpan(new BackgroundColorSpan(Color.YELLOW), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                startIndex = text.toLowerCase().indexOf(query.toLowerCase(), endIndex);
            }
            textView.setText(spannableString);
        }
    }

    public void updateOriginalList(ArrayList<AdminShowAllCategoryModel> newList) {
        originalCategoryModelArrayList.clear();
        originalCategoryModelArrayList.addAll(newList);
    }
}
