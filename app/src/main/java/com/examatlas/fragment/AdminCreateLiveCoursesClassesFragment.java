package com.examatlas.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.examatlas.R;
import com.examatlas.adapter.LiveCoursesAdapter;
import com.examatlas.models.LiveCoursesModel;
import com.examatlas.models.extraModels.BookImageModels;
import com.examatlas.utils.Constant;
import com.examatlas.utils.MySingletonFragment;
import com.examatlas.utils.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AdminCreateLiveCoursesClassesFragment extends Fragment {

    ProgressBar liveClassesProgress;
    LiveCoursesAdapter liveCoursesAdapter;
    RecyclerView liveClassesRecycler;
    ArrayList<LiveCoursesModel> liveCoursesModelArrayList = new ArrayList<>();
    private final String liveClassURL = Constant.BASE_URL + "liveclass/getAllLiveClass";
    SessionManager sessionManager;
    String authToken;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.admin_fragment_create_live_courses_classes, container, false);

        liveClassesProgress = view.findViewById(R.id.showAllLiveCoursesProgressBar);
        liveClassesRecycler = view.findViewById(R.id.showLiveCoursesRecycler);
        liveClassesRecycler.setLayoutManager(new GridLayoutManager(getContext(), 2));

        liveClassesProgress.setVisibility(View.VISIBLE);
        liveClassesRecycler.setVisibility(View.GONE);

        sessionManager = new SessionManager(getContext());
        authToken = sessionManager.getUserData().get("authToken");
        getLiveClasses();

        return view;
    }

    private void getLiveClasses() {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, liveClassURL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            liveClassesRecycler.setVisibility(View.VISIBLE);
                            liveClassesProgress.setVisibility(View.GONE);
                            boolean status = response.getBoolean("status");

                            if (status) {
                                JSONArray jsonArray = response.getJSONArray("classes");
                                liveCoursesModelArrayList.clear();

                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject2 = jsonArray.getJSONObject(i);
                                    String classID = jsonObject2.getString("_id");
                                    String title = jsonObject2.getString("title");
                                    String description = jsonObject2.getString("description");
                                    String teacherName = jsonObject2.getString("teacher");

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

                                    String categoryId = jsonObject2.getString("categoryId");
                                    String subCategoryId = jsonObject2.getString("subCategoryId");
                                    String subjectId = jsonObject2.getString("subjectId");

                                    ArrayList<BookImageModels> bookImageArrayList = new ArrayList<>();
                                    JSONArray jsonImageArray = jsonObject2.getJSONArray("images");
                                    for (int j = 0; j<jsonImageArray.length();j++){
                                        JSONObject jsonImageObject = jsonImageArray.getJSONObject(j);
                                        BookImageModels bookImageModels = new BookImageModels(
                                                jsonImageObject.getString("url"),
                                                jsonImageObject.getString("filename")
                                        );
                                        bookImageArrayList.add(bookImageModels);
                                    }

                                    String startDate = jsonObject2.getString("startDate");
                                    String endDate = jsonObject2.getString("endDate");

                                    JSONArray jsonStudentArray = jsonObject2.getJSONArray("students");
                                    ArrayList<BookImageModels> studentsArrayList = new ArrayList<>();
                                    for (int j = 0; j<jsonImageArray.length();j++){
                                    }
                                    JSONArray jsonLiveClasses = jsonObject2.getJSONArray("liveClasses");
                                    ArrayList<BookImageModels> liveClassesArrayList = new ArrayList<>();
                                    for (int j = 0; j<jsonImageArray.length();j++){
                                    }
                                    LiveCoursesModel liveCoursesModel = new LiveCoursesModel(classID, title, description, teacherName, tags.toString(),categoryId,subCategoryId,subjectId,startDate,endDate,bookImageArrayList,studentsArrayList,liveClassesArrayList);
                                    liveCoursesModelArrayList.add(liveCoursesModel);
                                }
                                if (liveCoursesAdapter == null) {
                                    liveCoursesAdapter = new LiveCoursesAdapter(liveCoursesModelArrayList, AdminCreateLiveCoursesClassesFragment.this);
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
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getContext(), error.toString(), Toast.LENGTH_SHORT).show();
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
}
