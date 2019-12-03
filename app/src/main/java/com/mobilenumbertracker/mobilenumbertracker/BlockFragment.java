package com.mobilenumbertracker.mobilenumbertracker;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.mobilenumbertracker.mobilenumbertracker.block.BlockAdapter;
import com.mobilenumbertracker.mobilenumbertracker.block.BlockModel;
import com.mobilenumbertracker.mobilenumbertracker.contact.ContactAdapter;
import com.mobilenumbertracker.mobilenumbertracker.contact.ContactModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static android.Manifest.permission.READ_CONTACTS;
import static android.content.Context.MODE_PRIVATE;
import static android.support.v4.content.PermissionChecker.checkSelfPermission;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link BlockFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link BlockFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BlockFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;

    // ArrayList
    ArrayList<BlockModel> blockModels;
    // Contact List
    RecyclerView recyclerView;
    TextView emptyView;
    // Cursor to load contacts list

    private RecyclerView.LayoutManager layoutManager;
    // Pop up
    ContentResolver resolver;
    SearchView search;
    BlockAdapter adapter;

    String BlockPrefFileName="blockPref";
    SharedPreferences pref;

    private Context  acContext;
    private JSONObject blockJSONObj;

    private OnFragmentInteractionListener mListener;

    public BlockFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment BlockFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static BlockFragment newInstance(String param1, String param2) {
        BlockFragment fragment = new BlockFragment();
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

    private void showContacts() {
        // Check the SDK version and whether the permission is already granted or not.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(acContext,READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
            //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
        } else {
            // Android version is lesser than 6.0 or the permission is already granted.
            String JSONString=pref.getString("BLOCK_LIST","");

            if (!"".equals(JSONString)){
               emptyView.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                try {
                    blockJSONObj=new JSONObject(JSONString);
                    LoadBlockContact loadContact =new LoadBlockContact();
                    loadContact.execute();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }else {
                emptyView.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            }

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View blockFragmentView = inflater.inflate(R.layout.fragment_block, container, false);

        pref= acContext.getSharedPreferences(BlockPrefFileName, MODE_PRIVATE);

        blockModels = new ArrayList<BlockModel>();
        resolver = acContext.getContentResolver();
        recyclerView = (RecyclerView) blockFragmentView.findViewById(R.id.block_recycler_view);
        emptyView=(TextView)blockFragmentView.findViewById(R.id.empty_view);

        layoutManager = new LinearLayoutManager(acContext);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        showContacts();

        return blockFragmentView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    // Load data on background
    class LoadBlockContact extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected Void doInBackground(Void... voids) {
            // Get Contact list from Phone

            Iterator<?> keys=blockJSONObj.keys();

            while (keys.hasNext()){
                String key=(String)keys.next();

                BlockModel model= new BlockModel();

                try {
                    model.setName(blockJSONObj.getString(key));
                    model.setNumber(key);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                blockModels.add(model);
            }
            //phones.close();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if(blockModels.size()>0) {
                adapter = new BlockAdapter(acContext,blockModels);
                recyclerView.setAdapter(adapter);
            }else {
                Toast.makeText(acContext, "No contacts in your block list.", Toast.LENGTH_LONG).show();
            }
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

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        acContext=context;
      /*  if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }*/
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
