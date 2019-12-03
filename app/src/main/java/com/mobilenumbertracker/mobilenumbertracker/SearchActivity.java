package com.mobilenumbertracker.mobilenumbertracker;


import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mobilenumbertracker.mobilenumbertracker.search.SearchAdapter;
import com.mobilenumbertracker.mobilenumbertracker.search.SearchModel;
import com.mobilenumbertracker.mobilenumbertracker.utils.Constants;
import com.mobilenumbertracker.mobilenumbertracker.utils.HTTPPost;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    // CONNECTION_TIMEOUT and READ_TIMEOUT are in milliseconds
    public static final int CONNECTION_TIMEOUT = 10000;
    public static final int READ_TIMEOUT = 15000;
    private RecyclerView mRVSearch;
    private SearchAdapter mAdapter;
    TextView TVTotal;
    SearchView searchView = null;
    ContentResolver resolver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        final Drawable upArrow = ContextCompat.getDrawable(this,R.drawable.ic_arrow_back);
        upArrow.setColorFilter(ContextCompat.getColor(this,R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(0xffffff));
        getSupportActionBar().setDisplayShowTitleEnabled(false);


        TVTotal= (TextView) findViewById(R.id.tvTotalCount);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // adds item to action bar
        getMenuInflater().inflate(R.menu.search_main, menu);

        // Get Search item from action bar and Get Search service
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchManager searchManager = (SearchManager) SearchActivity.this.getSystemService(Context.SEARCH_SERVICE);
        if (searchItem != null) {
            searchView = (SearchView) searchItem.getActionView();

            EditText searchEditText = (EditText) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
            searchEditText.setTextColor(getResources().getColor(R.color.colorPrimary));
            searchEditText.setHintTextColor(getResources().getColor(R.color.colorAccent));

            ImageView mCloseButton = (ImageView) searchView.findViewById(android.support.v7.appcompat.R.id.search_close_btn);
            mCloseButton.setImageResource(R.drawable.ic_cross);



            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                Handler handler=new Handler();
                Runnable runnable;
                @Override
                public boolean onQueryTextSubmit(String query) {

                    new AsyncFetch(query).execute();

                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
//                    final String searchText = newText;
//
//
//                    // Remove all previous callbacks.
//                    handler.removeCallbacks(runnable);
//
//                    if(!newText.trim().equals("")){
//                        runnable = new Runnable() {
//                            @Override
//                            public void run() {
//                                // Your code here.
//                                new AsyncFetch(searchText).execute();
//                            }
//                        };
//                        handler.postDelayed(runnable, 1500);
//                    }
                    return false;

                }
            });
        }
        if (searchView != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(SearchActivity.this.getComponentName()));
            searchView.setIconified(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }



    // Create class AsyncFetch
    private class AsyncFetch extends AsyncTask<String, String, String> {

        ProgressDialog pdLoading = new ProgressDialog(SearchActivity.this);
        HttpURLConnection conn;
        String BaseNameUrl ="http://api.mobilenumbertracker.com/v1/person/name/";
        String BaseNumberUrl ="http://api.mobilenumbertracker.com/v1/person/mobile/";
        String searchQuery;
        String AuthToken= "A525CKA30B760953CC8018C57C49FDA8";
        URL url = null;


        public AsyncFetch(String searchQuery){
            this.searchQuery=searchQuery;
        }

        private boolean isValidMobile(String phone) {
            return android.util.Patterns.PHONE.matcher(phone).matches();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //this method will be running on UI thread
            pdLoading.setMessage("\tSearching...");
            pdLoading.setCancelable(false);
            pdLoading.show();



        }

        @Override
        protected String doInBackground(String... params) {
            try {

                // Enter URL address where your php file resides
                String searchText = this.searchQuery;

                String encodedText=searchText.replaceAll(" ","%20").trim();
//                try {
//                    encodedText = URLEncoder.encode(searchText.trim(),"UTF-8");
//                } catch (UnsupportedEncodingException e) {
//                    e.printStackTrace();
//                }

                String UrlString=(isValidMobile(searchText)?BaseNumberUrl:BaseNameUrl)+encodedText +"/?auth_token="+AuthToken;

                url = new URL(UrlString);

            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return e.toString();
            }
            try {

                // Setup HttpURLConnection class to send and receive data from php and mysql
                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(READ_TIMEOUT);
                conn.setConnectTimeout(CONNECTION_TIMEOUT);
                conn.setRequestMethod("GET");

                // setDoInput and setDoOutput to true as we send and recieve data
                conn.setDoInput(true);
//                conn.setDoOutput(true);

                // add parameter to our above url
//                Uri.Builder builder = new Uri.Builder().appendQueryParameter("searchQuery", searchQuery);
//                String query = builder.build().getEncodedQuery();

//                OutputStream os = conn.getOutputStream();
//                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
//                writer.write(query);
//                writer.flush();
//                writer.close();
//                os.close();
                conn.connect();

            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
                return e1.toString();
            }

            try {

                int response_code = conn.getResponseCode();

                // Check if successful connection made
                if (response_code == HttpURLConnection.HTTP_OK) {

                    // Read data sent from server
                    InputStream input = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                    StringBuilder result = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }

                    // Pass data to onPostExecute method
                    return (result.toString());

                } else {
                    return("No Results found for entered text.");
                }

            } catch (IOException e) {
                e.printStackTrace();
                return e.toString();
            } finally {
                conn.disconnect();
            }


        }

        @Override
        protected void onPostExecute(String result) {

            //this method will be running on UI thread
            pdLoading.dismiss();
            List<SearchModel> data=new ArrayList<>();
//            Toast.makeText(SearchActivity.this,result, Toast.LENGTH_LONG).show();
            pdLoading.dismiss();
            if(result.equals("")) {
                Toast.makeText(SearchActivity.this, "No Results found for entered query", Toast.LENGTH_LONG).show();
                TVTotal.setText("No results found");
            }else{

                try {

                    if(isValidMobile(this.searchQuery)){
                        JSONObject jsonObject= new JSONObject(result);
                        SearchModel searchResult = new SearchModel();

                        searchResult.setFirstName(jsonObject.getString("FirstName"));
                        searchResult.setLastName(jsonObject.getString("LastName"));
                        searchResult.setEmail(jsonObject.getString("Email"));
                        searchResult.setMobile(jsonObject.getString("Mobile"));
                        searchResult.setPicUrl(jsonObject.getString("PictureUrl"));

                        String addr=(jsonObject.isNull("Address")?"": jsonObject.getString("Address"))+" "+(jsonObject.isNull("City")||"Not Available".equals(jsonObject.getString("City"))?"": jsonObject.getString("City"))+" "+(jsonObject.isNull("State")||"Not Available".equals(jsonObject.getString("State"))?"": jsonObject.getString("State"));
                        searchResult.setAddress(addr);
                        data.add(searchResult);
                    }else{

                        JSONArray jArray =new JSONArray(result);
                        // Extract data from json and store into ArrayList as class objects
                        for (int i = 0; i < jArray.length(); i++) {
                            JSONObject json_data = jArray.getJSONObject(i);
                            SearchModel searchResult = new SearchModel();
                            try {
                                searchResult.setFirstName(json_data.getString("FirstName"));
                                searchResult.setLastName(json_data.getString("LastName"));
                                searchResult.setEmail("".equals(json_data.getString("Email"))?"Email not available.":json_data.getString("Email"));
                                searchResult.setMobile(json_data.getString("Mobile"));
                                searchResult.setPicUrl(json_data.getString("PictureUrl"));
                                String addr=(json_data.isNull("Address")?"": json_data.getString("Address"))+" "+(json_data.isNull("City")||"Not Available".equals(json_data.getString("City"))?"": json_data.getString("City"))+" "+(json_data.isNull("State")||"Not Available".equals(json_data.getString("State"))?"": json_data.getString("State"));
                                searchResult.setAddress(addr);
                                data.add(searchResult);
                            }catch (Exception e){
                                continue;
                            }
                        }
                    }


                    // Setup and Handover data to recyclerview
                    mRVSearch = (RecyclerView) findViewById(R.id.rvSearch);
                    mAdapter = new SearchAdapter(SearchActivity.this, data);

                    mRVSearch.setLayoutManager(new LinearLayoutManager(SearchActivity.this));
                    mRVSearch.setAdapter(mAdapter);

                    TVTotal.setText("Total "+data.size()+" results");
                } catch (JSONException e) {
                    // You to understand what actually error is and handle it appropriately
//                    Toast.makeText(SearchActivity.this, e.toString(), Toast.LENGTH_LONG).show();
                    Toast.makeText(SearchActivity.this, result.toString(), Toast.LENGTH_LONG).show();
                }
            }
        }
    }



}