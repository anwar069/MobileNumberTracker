package com.mobilenumbertracker.mobilenumbertracker.block;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mobilenumbertracker.mobilenumbertracker.ContactActivity;
import com.mobilenumbertracker.mobilenumbertracker.R;
import com.mobilenumbertracker.mobilenumbertracker.search.SearchAdapter;
import com.mobilenumbertracker.mobilenumbertracker.search.SearchModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.List;

/**
 * Created by Ahmed on 15/02/2017.
 */

public class BlockAdapter  extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private LayoutInflater inflater;
    List<BlockModel> data = Collections.emptyList();
    public static String prefFilename = "blockPref";
    SharedPreferences preferences;
    SearchModel current;
    int currentPos = 0;

    // create constructor to initialize context and data sent from MainActivity
    public BlockAdapter(Context context, List<BlockModel> data) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.data = data;
    }

    // Inflate the layout when ViewHolder created
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_block_contact, parent, false);
        BlockAdapter.MyHolder holder = new BlockAdapter.MyHolder(view);
        return holder;
    }

    // Bind data
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        // Get current position of item in RecyclerView to bind data and assign values from list
        BlockAdapter.MyHolder myHolder = (BlockAdapter.MyHolder) holder;
        BlockModel current = data.get(position);
        myHolder.textName.setText(current.getName());
        myHolder.textNumber.setText(current.getNumber());
//        myHolder.textOther.setText("Rs. " + current.getOther()+ "\\Kg");
//        myHolder.textName.setTextColor(ContextCompat.getColor(context, R.color.colorAccent));

    }

    private void RemoveContactFromPref(BlockModel bModel) {
        preferences = context.getSharedPreferences(prefFilename, 0);

        String JSONString=preferences.getString("BLOCK_LIST","");
        JSONObject blockObject=null;
        try {
            blockObject= new JSONObject(JSONString);
            blockObject.remove(bModel.getNumber());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("BLOCK_LIST",blockObject.toString());
        editor.commit();

        data.remove(bModel);

        notifyDataSetChanged();

        Toast.makeText(context,bModel.getName()+ " removed from block list.",Toast.LENGTH_SHORT).show();
    }

    // return total item from List
    @Override
    public int getItemCount() {
        return data.size();
    }

    class MyHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView textName;
        TextView textNumber;
        ImageView removeImage;

        // create constructor to get widget reference
        public MyHolder(View itemView) {
            super(itemView);
            textName = (TextView) itemView.findViewById(R.id.tvBlockName);
            textNumber = (TextView) itemView.findViewById(R.id.tvBlockNumber);
            removeImage=(ImageView)itemView.findViewById(R.id.tvRemoveImage);
//            textOther = (TextView) itemView.findViewById(R.id.tvSearchPrice);
            removeImage.setOnClickListener(this);
        }

        // Click event for all items
        @Override
        public void onClick(View v) {
            Intent contactIntent = new Intent(context, ContactActivity.class);
            BlockModel current = data.get(this.getLayoutPosition());
//            Toast.makeText(context, current.getMobile(), Toast.LENGTH_SHORT).show();
            RemoveContactFromPref(current);
        }
    }
}
