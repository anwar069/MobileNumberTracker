package com.mobilenumbertracker.mobilenumbertracker.search;

/**
 * Created by Ahmed on 12/01/2017.
 */


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.mobilenumbertracker.mobilenumbertracker.ContactActivity;
import com.mobilenumbertracker.mobilenumbertracker.GettingStartedActivity;
import com.mobilenumbertracker.mobilenumbertracker.R;
import com.mobilenumbertracker.mobilenumbertracker.WelcomeActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private LayoutInflater inflater;
    List<SearchModel> data = Collections.emptyList();
    public static String prefFilename = "ContactPreferences";
    SharedPreferences preferences;
    SearchModel current;
    int currentPos = 0;

    // create constructor to initialize context and data sent from MainActivity
    public SearchAdapter(Context context, List<SearchModel> data) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.data = data;
    }

    // Inflate the layout when ViewHolder created
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_search_result, parent, false);
        MyHolder holder = new MyHolder(view);
        return holder;
    }

    // Bind data
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        // Get current position of item in RecyclerView to bind data and assign values from list
        MyHolder myHolder = (MyHolder) holder;
        SearchModel current = data.get(position);
        myHolder.textName.setText(current.getFirstName() + " " + current.getLastName());
        myHolder.textNumber.setText(current.getMobile());
        if(!"".equals(current.getAddress().trim()))
            myHolder.textLocation.setText(current.getAddress());
        else
            myHolder.textLocation.setText("Location not available.");

//        myHolder.textOther.setText("Rs. " + current.getOther()+ "\\Kg");
//        myHolder.textName.setTextColor(ContextCompat.getColor(context, R.color.colorAccent));
    }

    private void saveContactToPref(SearchModel sModel) {
        preferences = context.getSharedPreferences(prefFilename, 0);

        String jsonContactStr = preferences.getString("CONTACTJSON", "{}");

        JSONObject jsonContactObj = null;
        try {
            jsonContactObj = new JSONObject(jsonContactStr);
            JSONObject newProfObj = new JSONObject();
            newProfObj.put("FirstName", sModel.getFirstName());
            newProfObj.put("LastName", sModel.getLastName());
            newProfObj.put("Address", sModel.getAddress());
            newProfObj.put("Email", sModel.getEmail());
            newProfObj.put("Mobile", sModel.getMobile());
            newProfObj.put("PicURL", sModel.getPicUrl());
            jsonContactObj.put(sModel.getMobile(), newProfObj);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("CONTACTJSON", jsonContactObj.toString());
        editor.commit();
    }

    // return total item from List
    @Override
    public int getItemCount() {
        return data.size();
    }

    class MyHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView textName;
        TextView textNumber;
        TextView textLocation;
        TextView textOther;

        // create constructor to get widget reference
        public MyHolder(View itemView) {
            super(itemView);
            textName = (TextView) itemView.findViewById(R.id.tvSearchName);
            textNumber = (TextView) itemView.findViewById(R.id.tvSearchNumber);
            textLocation = (TextView) itemView.findViewById(R.id.tvSearchAddress);
//            textOther = (TextView) itemView.findViewById(R.id.tvSearchPrice);
            itemView.setOnClickListener(this);
        }

        // Click event for all items
        @Override
        public void onClick(View v) {
            Intent contactIntent = new Intent(context, ContactActivity.class);
            SearchModel current = data.get(this.getLayoutPosition());
//            Toast.makeText(context, current.getMobile(), Toast.LENGTH_SHORT).show();
            saveContactToPref(current);
            contactIntent.putExtra("Name", textName.getText());
            contactIntent.putExtra("Number", textNumber.getText());
            contactIntent.putExtra("Location", textLocation.getText());
            contactIntent.putExtra("Email",("".equals(current.getEmail().trim()))?"Email not available":current.getEmail());
            contactIntent.putExtra("ImageURL",current.getPicUrl());
            context.startActivity(contactIntent);
        }
    }
}
