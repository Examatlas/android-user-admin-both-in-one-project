package com.examatlas.activities;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.examatlas.R;
import com.examatlas.adapter.AdminLiveCourseClassesListAdapter;
import com.examatlas.adapter.LiveCourseClassesListAdapter;
import com.examatlas.models.AdminLiveCoursesClassesListModel;
import com.examatlas.models.LiveCoursesClassesListModel;
import com.examatlas.utils.Constant;
import com.examatlas.utils.MySingleton;
import com.examatlas.utils.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AdminLiveCoursesClassesActivity extends AppCompatActivity {
    Toolbar toolbar;
    ProgressBar progressBar;
    RelativeLayout noDataLayout;
    RecyclerView classesListRecyclerView;
    AdminLiveCourseClassesListAdapter liveCourseClassesListAdapter;
    ArrayList<AdminLiveCoursesClassesListModel> liveCoursesClassesListModelArrayList;
    String getLiveClassesListURL;
    SessionManager sessionManager;
    String authToken;
    String courseId;
    TextView scheduleClassTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_live_courses_classes);

        scheduleClassTxt = findViewById(R.id.scheduleClassTxt);

        sessionManager = new SessionManager(this);
        authToken = sessionManager.getUserData().get("authToken");
        Intent intent = getIntent();
        courseId = intent.getStringExtra("course_id");


        toolbar = findViewById(R.id.liveCoursesClassesListToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);

        progressBar = findViewById(R.id.liveCoursesClassesListProgressBar);
        noDataLayout = findViewById(R.id.noDataLayout);
        classesListRecyclerView = findViewById(R.id.liveCoursesClassesListRecyclerView);
        classesListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        liveCoursesClassesListModelArrayList = new ArrayList<>();

        getLiveClassesListURL = Constant.BASE_URL + "liveclass/getAllScheduledCourseByCourseId/" + courseId;

        scheduleClassTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                scheduleClass();
                openScheduleLiveCourseDialogBox();
            }
        });
    }
    Dialog dialog;
    EditText titleEditTxt,descriptionEditText,durationEditText;
    TextView dateDisplayText,timeDisplayText;
    ImageView crossBtn;
    String startDateIso,timeIso;
    Button submitBtn;

    private void openScheduleLiveCourseDialogBox() {
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.admin_schedule_live_course_dialog_box);

        titleEditTxt = dialog.findViewById(R.id.titleEditTxt);
        descriptionEditText = dialog.findViewById(R.id.descriptionEditText);
        durationEditText = dialog.findViewById(R.id.durationEditText);
        dateDisplayText = dialog.findViewById(R.id.dateDisplayText);
        timeDisplayText = dialog.findViewById(R.id.timeDisplayText);
        crossBtn = dialog.findViewById(R.id.btnCross);
        submitBtn = dialog.findViewById(R.id.btnSubmit);

        crossBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dateDisplayText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

                // Create DatePickerDialog
                DatePickerDialog datePickerDialog = new DatePickerDialog(AdminLiveCoursesClassesActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(android.widget.DatePicker view, int year, int month, int dayOfMonth) {
                                // Display the selected date in the EditText
                                Calendar selectedDate = Calendar.getInstance();
                                selectedDate.set(year, month, dayOfMonth);

                                // Format the date to a readable format
                                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                                String formattedDate = dateFormat.format(selectedDate.getTime());

                                // Set the formatted date in EditText
                                dateDisplayText.setText(formattedDate);
                                SimpleDateFormat isoDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                                startDateIso = isoDateFormat.format(selectedDate.getTime());

                            }
                        },
                        year, month, dayOfMonth);

                // Show the DatePickerDialog
                datePickerDialog.show();
            }
        });

        timeDisplayText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar calendar = Calendar.getInstance();
                int hour = calendar.get(Calendar.HOUR_OF_DAY); // 24-hour format
                int minute = calendar.get(Calendar.MINUTE);

                // Create TimePickerDialog
                TimePickerDialog timePickerDialog = new TimePickerDialog(AdminLiveCoursesClassesActivity.this, // Context
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                // Convert the time to LocalTime and format it as ISO 8601 (HH:mm:ss)
                                LocalTime selectedTime = null;
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    selectedTime = LocalTime.of(hourOfDay, minute);
                                }
                                timeIso = null;
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    timeIso = selectedTime.format(DateTimeFormatter.ISO_LOCAL_TIME);
                                }

                                // Display the time in the TextView
                                timeDisplayText.setText(timeIso); // Show the formatted time (HH:mm:ss)

                                // Optionally, you can use the timeIso variable to send the time to the backend
                                // Example: sendTimeToBackend(timeIso);
                            }
                        },
                        hour, minute, true // true for 24-hour format, false for 12-hour format
                );

                // Show the TimePickerDialog
                timePickerDialog.show();
            }
        });

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scheduleClass();
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

    private void scheduleClass() {
        String scheduleClassURL = Constant.BASE_URL + "liveclass/scheduleLiveCourse";
        String title = titleEditTxt.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();
        String duration = durationEditText.getText().toString().trim();

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("title",title);
            jsonObject.put("courseId",courseId);
            jsonObject.put("date",startDateIso);
            jsonObject.put("time",timeIso);
            jsonObject.put("description",description);
            jsonObject.put("duration",duration);
        }catch (JSONException e) {
            Log.e("catch",e.toString());
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, scheduleClassURL, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("API Response", response.toString());
                        try {
                            boolean status = response.getBoolean("status");
                            if (status) {
                                String message = response.getString("message");
                                Toast.makeText(AdminLiveCoursesClassesActivity.this, message, Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            } else {
                                showNoDataLayout();
                            }
                        } catch (JSONException e) {
                            showErrorToast(String.valueOf(e));
                        }
                    }
                }, new Response.ErrorListener() {
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
                        Toast.makeText(AdminLiveCoursesClassesActivity.this, message, Toast.LENGTH_LONG).show();
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
                headers.put("Authorization", "Bearer " + authToken);
                return headers;
            }
        };

        MySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getLiveClassesList(); // Always refresh data when the activity is resumed
    }

    public void getLiveClassesList() {
        progressBar.setVisibility(View.VISIBLE);
        noDataLayout.setVisibility(View.GONE);
        classesListRecyclerView.setVisibility(View.GONE);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, getLiveClassesListURL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("API Response", response.toString());
                        try {
                            boolean status = response.getBoolean("status");
                            if (status) {
                                liveCoursesClassesListModelArrayList.clear();
                                JSONArray jsonArray = response.getJSONArray("courses");
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    String classID = jsonObject.getString("_id");
                                    String courseID = jsonObject.getString("courseId");
                                    String title = jsonObject.getString("title");
                                    String meetingID = jsonObject.getString("meetingId");
                                    String time = jsonObject.getString("time");
                                    String date = jsonObject.getString("date");
                                    String addedBy = jsonObject.getString("addedBy");
                                    String scheduledTime = jsonObject.getString("scheduleTime");
                                    String classStatus = jsonObject.getString("status");
                                    String startedAt,endedAt;
                                    if (classStatus.equalsIgnoreCase("completed")) {
                                        startedAt = jsonObject.getString("startedAt");
                                        endedAt = jsonObject.getString("endedAt");
                                    }else {
                                        startedAt = null;
                                        endedAt = null;
                                    }
                                    AdminLiveCoursesClassesListModel liveCoursesClassesListModel = new AdminLiveCoursesClassesListModel(classID, courseID, title, meetingID, time, date, addedBy, scheduledTime, classStatus, startedAt,endedAt, null);
                                    liveCoursesClassesListModelArrayList.add(liveCoursesClassesListModel);
                                }

                                if (!liveCoursesClassesListModelArrayList.isEmpty()) {
                                    progressBar.setVisibility(View.GONE);
                                    classesListRecyclerView.setVisibility(View.VISIBLE);
                                    liveCourseClassesListAdapter = new AdminLiveCourseClassesListAdapter(AdminLiveCoursesClassesActivity.this, liveCoursesClassesListModelArrayList);
                                    classesListRecyclerView.setAdapter(liveCourseClassesListAdapter);
                                    liveCourseClassesListAdapter.notifyDataSetChanged();
                                } else {
                                    showNoDataLayout();
                                }
                            } else {
                                showNoDataLayout();
                            }
                        } catch (JSONException e) {
                            showErrorToast(String.valueOf(e));
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String errorMessage = "Error: " + error.toString();
                if (error.networkResponse != null) {
                    try {
                        // Parse the error response
                        String jsonError = new String(error.networkResponse.data);
                        JSONObject jsonObject = new JSONObject(jsonError);
                        String message = jsonObject.optString("message", "Unknown error");
                        showNoDataLayout();
                        // Now you can use the message
                        Toast.makeText(AdminLiveCoursesClassesActivity.this, message, Toast.LENGTH_LONG).show();
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
                headers.put("Authorization", "Bearer " + authToken);
                return headers;
            }
        };

        MySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }

    private void showNoDataLayout() {
        classesListRecyclerView.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        noDataLayout.setVisibility(View.VISIBLE);
    }

    private void showErrorToast(String message) {
        if (message != null && !message.isEmpty()) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "An error occurred", Toast.LENGTH_SHORT).show(); // Fallback message
        }
    }
    public String getCourseId() {
        return courseId;
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