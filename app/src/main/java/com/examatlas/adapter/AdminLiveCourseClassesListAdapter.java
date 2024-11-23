package com.examatlas.adapter;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.examatlas.R;
import com.examatlas.activities.AdminJoinLiveClassActivity;
import com.examatlas.activities.AdminLiveCoursesClassesActivity;
import com.examatlas.models.AdminLiveCoursesClassesListModel;
import com.examatlas.utils.Constant;
import com.examatlas.utils.MySingleton;
import com.examatlas.utils.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AdminLiveCourseClassesListAdapter extends RecyclerView.Adapter<AdminLiveCourseClassesListAdapter.ViewHolder> {
    Context context;
    ArrayList<AdminLiveCoursesClassesListModel> liveCoursesClassesListModelArrayList;
    String joinClassURL = Constant.BASE_URL + "liveclass/joinNow";
    SessionManager sessionManager;
    String authToken;

    public AdminLiveCourseClassesListAdapter(Context context, ArrayList<AdminLiveCoursesClassesListModel> liveCoursesClassesListModelArrayList) {
        this.context = context;
        this.liveCoursesClassesListModelArrayList = liveCoursesClassesListModelArrayList;
        sessionManager = new SessionManager(context);
        authToken = sessionManager.getUserData().get("authToken");
    }

    @NonNull
    @Override
    public AdminLiveCourseClassesListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.admin_live_courses_classes_list_item_list, parent, false);
        return new AdminLiveCourseClassesListAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminLiveCourseClassesListAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {

        holder.itemView.setTag(position);

        holder.classTitleTxt.setText(liveCoursesClassesListModelArrayList.get(position).getTitle());
        holder.liveTxt.setText(liveCoursesClassesListModelArrayList.get(position).getStatus());

        String formatedDate = formatDate(liveCoursesClassesListModelArrayList.get(position).getDate());
        String formatedTimeAndDate = formatedDate + " (" + liveCoursesClassesListModelArrayList.get(position).getTime() + ")";

        holder.timeAndDateTxt.setText(formatedTimeAndDate);
        holder.scheduleTxtDisplay.setText(liveCoursesClassesListModelArrayList.get(position).getScheduledTime());

        holder.deleteClassBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteClass(position);
            }
        });

        holder.joinNowBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ProgressDialog progressDialog = new ProgressDialog(context);
                progressDialog.setMessage("Joining in...");
                progressDialog.setCancelable(false);
                progressDialog.show();
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("scheduledClassId", liveCoursesClassesListModelArrayList.get(position).getClassID());
                    jsonObject.put("role", "admin");
                    jsonObject.put("meetingId", liveCoursesClassesListModelArrayList.get(position).getMeetingID());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, joinClassURL, jsonObject,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                progressDialog.dismiss();
                                try {
                                    String status = response.getString("status");
                                    String token = response.getString("token");
                                    Intent intent = new Intent(context, AdminJoinLiveClassActivity.class);
                                    intent.putExtra("course_id" ,((AdminLiveCoursesClassesActivity) context).getCourseId());
                                    intent.putExtra("token" ,token);
                                    intent.putExtra("meetingID",liveCoursesClassesListModelArrayList.get(position).getMeetingID());
                                    progressDialog.dismiss();
                                    context.startActivity(intent);

                                } catch (JSONException e) {
                                    Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
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
                        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show();
                        Log.e("LoginActivity", errorMessage);
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
                MySingleton.getInstance(context).addToRequestQueue(jsonObjectRequest);
            }
        });
    }

    private void deleteClass(int position) {
        Toast.makeText(context, "Still in development", Toast.LENGTH_SHORT).show();
    }
//        String deleteClassURL = Constant.BASE_URL + "liveclass/deleteClass/" + liveCoursesClassesListModelArrayList.get(position).getClassID();
//        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.DELETE, deleteClassURL, null,
//                new Response.Listener<JSONObject>() {
//                    @Override
//                    public void onResponse(JSONObject response) {
//                        Log.d("API Response", response.toString());
//                        try {
//                            boolean status = response.getBoolean("status");
//                            if (status) {
//                                String message = response.getString("message");
//                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
//                                ((AdminLiveCoursesClassesActivity) context).getLiveClassesList();
//                            }
//                        } catch (JSONException e) {
//                            Log.e("catch",e.toString());
//                        }
//                    }
//                }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                String errorMessage = "Error: " + error.toString();
//                if (error.networkResponse != null) {
//                    try {
//                        // Parse the error response
//                        String jsonError = new String(error.networkResponse.data);
//                        JSONObject jsonObject = new JSONObject(jsonError);
//                        String message = jsonObject.optString("message", "Unknown error");
//                        // Now you can use the message
//                        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//                Log.e("BlogFetchError", errorMessage);
//            }
//        }) {
//            @Override
//            public Map<String, String> getHeaders() throws AuthFailureError {
//                Map<String, String> headers = new HashMap<>();
//                headers.put("Content-Type", "application/json");
//                headers.put("Authorization", "Bearer " + authToken);
//                return headers;
//            }
//        };
//
//        MySingleton.getInstance(context).addToRequestQueue(jsonObjectRequest);
//
//    }

    @Override
    public int getItemCount() {
        return liveCoursesClassesListModelArrayList.size();
    }
    private String formatDate(String dateStr) {
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        Date date;
        try {
            date = inputFormat.parse(dateStr);
            return outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return dateStr; // Return original if parsing fails
        }
    }
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView timeAndDateTxt, classTitleTxt, liveTxt, scheduleTxtDisplay,joinNowBtn;
        ImageView deleteClassBtn;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            timeAndDateTxt = itemView.findViewById(R.id.timeAndDateTxt);
            classTitleTxt = itemView.findViewById(R.id.classTitleTxt);
            liveTxt = itemView.findViewById(R.id.liveTxt);
            scheduleTxtDisplay = itemView.findViewById(R.id.scheduleTxtDisplay);
            joinNowBtn = itemView.findViewById(R.id.goLiveTxt);
            deleteClassBtn = itemView.findViewById(R.id.deleteClassBtn);

        }
    }
}