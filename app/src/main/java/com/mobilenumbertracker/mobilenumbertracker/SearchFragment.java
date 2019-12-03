package com.mobilenumbertracker.mobilenumbertracker;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mobilenumbertracker.mobilenumbertracker.search.SearchAdapter;
import com.mobilenumbertracker.mobilenumbertracker.search.SearchModel;
import com.mobilenumbertracker.mobilenumbertracker.utils.Constants;
import com.mobilenumbertracker.mobilenumbertracker.utils.HTTPPost;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import pref.PrefManager.PrefManager;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SearchFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SearchFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SearchFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private PrefManager prefManager;
    // CONNECTION_TIMEOUT and READ_TIMEOUT are in milliseconds
    public static final int CONNECTION_TIMEOUT = 10000;
    public static final int READ_TIMEOUT = 15000;
    private RecyclerView mRVSearch;
    private SearchAdapter mAdapter;
    private LinearLayout llEmptyView;
    ContentResolver resolver;
    SearchView searchView = null;
    Context acContext;


    Button BtnContactSync;
    public static String prefFilename = "ContactPreferences";
    SharedPreferences preferences;
    private OnFragmentInteractionListener mListener;

    public SearchFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SearchFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SearchFragment newInstance(String param1, String param2) {
        SearchFragment fragment = new SearchFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        List<SearchModel> data=new ArrayList<>();
        // Inflate the layout for this fragment
        View searchFragmentView = inflater.inflate(R.layout.fragment_search, container, false);

        ((TextView)searchFragmentView.findViewById(R.id.tvOpenSearch)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(acContext, SearchActivity.class));
            }
        });

        BtnContactSync=(Button)searchFragmentView.findViewById(R.id.btnSync);
        BtnContactSync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AsyncContactsSync().execute();
            }
        });


        preferences =acContext.getSharedPreferences(prefFilename, 0);
        String jsonProfStr = preferences.getString("CONTACTJSON", "{}");
        prefManager = new PrefManager(acContext);

        JSONObject jsonProfObj=null;
        try {
            jsonProfObj = new JSONObject(jsonProfStr);

            data.clear();

            Iterator<String> keys = jsonProfObj.keys();
            while( keys.hasNext() ){
                String key = keys.next();
                try {
                    if( jsonProfObj.get(key) instanceof JSONObject ){
                        JSONObject contactObj=(JSONObject) jsonProfObj.get(key);
                        SearchModel searchResult = new SearchModel();

                        searchResult.setFirstName(contactObj.getString("FirstName"));
                        searchResult.setLastName(contactObj.getString("LastName"));
                        searchResult.setEmail(contactObj.getString("Email"));
                        searchResult.setMobile(contactObj.getString("Mobile"));
                        searchResult.setPicUrl(contactObj.getString("PicURL"));
                        searchResult.setAddress(contactObj.getString("Address"));

                        data.add(searchResult);
                    }
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        mRVSearch = (RecyclerView) searchFragmentView.findViewById(R.id.rv_recent_search);
        llEmptyView= (LinearLayout) searchFragmentView.findViewById(R.id.empty_view);
        if (data.size()>0){
            llEmptyView.setVisibility(View.GONE);
            mRVSearch.setVisibility(View.VISIBLE);

            Collections.reverse(data);
            mAdapter = new SearchAdapter(acContext, data);

            mRVSearch.setLayoutManager(new LinearLayoutManager(acContext));
            mRVSearch.setAdapter(mAdapter);
        }else {
            llEmptyView.setVisibility(View.VISIBLE);
            mRVSearch.setVisibility(View.GONE);
        }

        // Setup and Handover data to recyclerview

        setSyncButtonVisiblity();

        return searchFragmentView;
    }

    private void showSuccesDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                acContext);

        // set title
        alertDialogBuilder.setTitle("Mobile Number Tracker");

        // set dialog message
        alertDialogBuilder
                .setMessage("Contacts synced successfully.")
                .setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, close
                        // current activity
                        setSyncButtonVisiblity();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    private void setSyncButtonVisiblity() {

        if (prefManager.isFirstTimeSync()) {
            BtnContactSync.setVisibility(View.VISIBLE);
        }else {
            BtnContactSync.setVisibility(View.GONE);
        }

    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        acContext=context;
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
    @NonNull
    private JSONArray getContactJson() {
        resolver = acContext.getContentResolver();
        Cursor phones = acContext.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");

        JSONArray contactArray = new JSONArray();

        while (phones.moveToNext()) {
            Bitmap bit_thumb = null;
            String id = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
            String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
//            String EmailAddr = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));

            String Email = null;
            Cursor emails = resolver.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI,null, ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = " + id, null, null);
            while (emails.moveToNext()) {
                Email = emails.getString(emails.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                break;
            }
            emails.close();

            String image_thumb = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI));
            try {
                if (image_thumb != null) {
                    bit_thumb = MediaStore.Images.Media.getBitmap(resolver, Uri.parse(image_thumb));
                } else {
                    Log.e("No Image Thumb", "--------------");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            JSONObject contactJSON = new JSONObject();

            String [] fullName= name.split(" ");

            try {
                contactJSON.put("Id", 0);

                contactJSON.put("FirstName", fullName[0]);

                if(fullName.length>1)
                    contactJSON.put("LastName", fullName[fullName.length-1]);
                else
                    contactJSON.put("LastName", "");
                contactJSON.put("Email", Email);
                contactJSON.put("Mobile", phoneNumber);
                contactJSON.put("Dob", "");
                contactJSON.put("City", "");
                contactJSON.put("State", "");
                contactJSON.put("Address", "");
                contactJSON.put("PostalCode", "");
                contactJSON.put("PictureUrl", "");
                contactJSON.put("FbUrl", "");
                contactJSON.put("GplusUrl", "");
                contactJSON.put("LinkedUrl", "");
                contactJSON.put("TwitterUrl", "");

            } catch (JSONException e) {
                e.printStackTrace();
            }

            contactArray.put(contactJSON);
        }
        return contactArray;
    }

    private class AsyncContactsSync extends AsyncTask<String, String, String> {

        String response="";
        ProgressDialog progressDialog;

        @Override
        protected String doInBackground(String... params) {
            publishProgress("Syncing contacts..."); // Calls onProgressUpdate()
            HTTPPost postContact=new HTTPPost();

            try {
                response= postContact.post(Constants.postUrl,getContactJson().toString());

            } catch (IOException e) {
                e.printStackTrace();
            }
            return response;
        }


        @Override
        protected void onPostExecute(String result) {
            // execution of result of Long time consuming operation
//            progressDialog.dismiss();
//            if(!"".equals(result))
//                Toast.makeText(acContext,"Contacts synced successfully.",Toast.LENGTH_SHORT).show();


        }


        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(acContext,
                    "Mobile Number Tracker",
                    "Syncing contacts...");

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    if (prefManager.isFirstTimeSync()) {
                        prefManager.setFirstTimeSync(false);
                    }
                    progressDialog.dismiss();
                    showSuccesDialog();
                }
            }, 3000);
        }


//        @Override
//        protected void onProgressUpdate(String... text) {
//            finalResult.setText(text[0]);
//
//        }
    }
    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
