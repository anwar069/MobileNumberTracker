package com.mobilenumbertracker.mobilenumbertracker;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.mobilenumbertracker.mobilenumbertracker.call.CallAdapter;
import com.mobilenumbertracker.mobilenumbertracker.call.CallModel;
import com.mobilenumbertracker.mobilenumbertracker.contact.ContactAdapter;
import com.mobilenumbertracker.mobilenumbertracker.contact.ContactModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

import static android.Manifest.permission.CALL_PHONE;
import static android.Manifest.permission.READ_CALL_LOG;
import static android.Manifest.permission.READ_CONTACTS;
import static android.support.v4.content.PermissionChecker.checkSelfPermission;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CallFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CallFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CallFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final int PERMISSIONS_REQUEST_CALL_PHONE = 102;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private static RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private static RecyclerView recyclerView;
    private static ArrayList<CallModel> data;
    static View.OnClickListener myOnClickListener;
    private static ArrayList<Integer> removedItems;

    // Request code for READ_CALLS. It can be any number > 0.
    private static final int PERMISSIONS_REQUEST_READ_CALLS = 101;
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;

    Context acContext;
    ArrayList<CallModel> callModels = new ArrayList<CallModel>();

    public CallFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CallFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CallFragment newInstance(String param1, String param2) {
        CallFragment fragment = new CallFragment();
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

    private void showCalls() {
        // Check the SDK version and whether the permission is already granted or not.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(acContext,READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{READ_CALL_LOG}, PERMISSIONS_REQUEST_READ_CALLS);
            //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
        }
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(acContext,READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
                //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
            }
        else {
            // Android version is lesser than 6.0 or the permission is already granted.
            LoadCalls loadCalls = new LoadCalls();
            loadCalls.execute();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View callFragmentView = inflater.inflate(R.layout.fragment_call, container, false);
        recyclerView = (RecyclerView) callFragmentView.findViewById(R.id.my_recycler_view);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(acContext);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        showCalls();

        return callFragmentView;
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
        acContext = context;
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_READ_CALLS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted
                showCalls();
            } else {
                Toast.makeText(acContext, "Until you grant the permission, we cannot display the Call Logs", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted
                showCalls();
            } else {
                Toast.makeText(acContext, "Until you grant the permission, we cannot display the names", Toast.LENGTH_SHORT).show();
            }
        }
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

    private ArrayList<CallModel> getCallDetails() {
        ArrayList<CallModel> callModels = new ArrayList<CallModel>();
        if (ContextCompat.checkSelfPermission(acContext, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) acContext, new String[]{Manifest.permission.READ_CALL_LOG}, PERMISSIONS_REQUEST_READ_CALLS);
        }

        Cursor managedCursor = acContext.getContentResolver().query(CallLog.Calls.CONTENT_URI, null,
                null, null, null);
        int number = managedCursor.getColumnIndex(CallLog.Calls.NUMBER);
        int type = managedCursor.getColumnIndex(CallLog.Calls.TYPE);
        int date = managedCursor.getColumnIndex(CallLog.Calls.DATE);
        int duration = managedCursor.getColumnIndex(CallLog.Calls.DURATION);
        while (managedCursor.moveToNext()) {
            String phNumber = managedCursor.getString(number);
            String callType = managedCursor.getString(type);
            String callDate = managedCursor.getString(date);
            Date callDayTime = new Date(Long.valueOf(callDate));
            int callDuration = Integer.parseInt(managedCursor.getString(duration));
            String dir = null;
            int dircode = Integer.parseInt(callType);
            switch (dircode) {
                case CallLog.Calls.OUTGOING_TYPE:
                    dir = "OUTGOING";
                    break;

                case CallLog.Calls.INCOMING_TYPE:
                    dir = "INCOMING";
                    break;

                case CallLog.Calls.MISSED_TYPE:
                    dir = "MISSED";
                    break;
            }

         //   String name = getContactName(acContext, phNumber);
            callModels.add(new CallModel(phNumber, dir, callDayTime,getTimeStringBySecond(callDuration)));
        }
        managedCursor.close();
        return callModels;


    }

    // Load data on background
    class LoadCalls extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected Void doInBackground(Void... voids) {
            // Get Contact list from Phone


            if (ContextCompat.checkSelfPermission(acContext, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions((Activity) acContext, new String[]{Manifest.permission.READ_CALL_LOG}, PERMISSIONS_REQUEST_READ_CALLS);
            }

            Cursor managedCursor = acContext.getContentResolver().query(CallLog.Calls.CONTENT_URI, null,
                    null, null,CallLog.Calls.DATE);
            int number = managedCursor.getColumnIndex(CallLog.Calls.NUMBER);
            int type = managedCursor.getColumnIndex(CallLog.Calls.TYPE);
            int date = managedCursor.getColumnIndex(CallLog.Calls.DATE);
            int duration = managedCursor.getColumnIndex(CallLog.Calls.DURATION);
            while (managedCursor.moveToNext()) {
                String phNumber = managedCursor.getString(number);
                String callType = managedCursor.getString(type);
                String callDate = managedCursor.getString(date);
                Date callDayTime = new Date(Long.valueOf(callDate));
                int callDuration = Integer.parseInt(managedCursor.getString(duration));
                String dir = null;
                int dircode = Integer.parseInt(callType);
                switch (dircode) {
                    case CallLog.Calls.OUTGOING_TYPE:
                        dir = "OUTGOING";
                        break;

                    case CallLog.Calls.INCOMING_TYPE:
                        dir = "INCOMING";
                        break;

                    case CallLog.Calls.MISSED_TYPE:
                        dir = "MISSED";
                        break;
                }


                callModels.add(new CallModel(phNumber, dir, callDayTime,getTimeStringBySecond(callDuration)));
            }
            managedCursor.close();
            //phones.close();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Collections.reverse(callModels);
            adapter = new CallAdapter(acContext,callModels );
            recyclerView.setAdapter(adapter);

            /*// Select item on listclick
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    Log.e("search", "here---------------- listener");

                    SelectUser data = selectUsers.get(i);
                }
            });

            listView.setFastScrollEnabled(true);*/
        }
    }

    public String getTimeStringBySecond(int secs){
        if(secs<=60)
            return secs+" Sec";
        else if (secs <= 3600) {
            return secs % 3600 / 60 + " Min " + secs % 60 + " Sec";
        }else {
            return secs % 86400 / 3600 + " Hours " +secs % 3600 / 60 + " Min " + secs % 60 + " Sec";
        }
    }


    private static class MyOnClickListener implements View.OnClickListener {

        private final Context context;

        private MyOnClickListener(Context context) {
            this.context = context;
        }

        @Override
        public void onClick(View v) {
//            removeItem(v);
        }

//        private void removeItem(View v) {
//            int selectedItemPosition = recyclerView.getChildPosition(v);
//            RecyclerView.ViewHolder viewHolder
//                    = recyclerView.findViewHolderForPosition(selectedItemPosition);
//            TextView textViewName
//                    = (TextView) viewHolder.itemView.findViewById(R.id.textViewName);
//            String selectedName = (String) textViewName.getText();
//            int selectedItemId = -1;
//            for (int i = 0; i < MyData.nameArray.length; i++) {
//                if (selectedName.equals(MyData.nameArray[i])) {
//                    selectedItemId = MyData.id_[i];
//                }
//            }
//            removedItems.add(selectedItemId);
//            data.remove(selectedItemPosition);
//            adapter.notifyItemRemoved(selectedItemPosition);
//        }
    }
}
