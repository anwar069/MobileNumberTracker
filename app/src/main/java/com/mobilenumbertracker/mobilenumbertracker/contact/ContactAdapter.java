package com.mobilenumbertracker.mobilenumbertracker.contact;


import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mobilenumbertracker.mobilenumbertracker.R;
import com.mobilenumbertracker.mobilenumbertracker.utils.RoundImage;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.Manifest.permission.CALL_PHONE;

/**
 * Created by Ahmed on 12/01/2017.
 */

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.MyViewHolder> {
    public List<ContactModel> _data;
    private ArrayList<ContactModel> dataSet;
    Context _c;
    RoundImage roundedImage;
    private static final int PERMISSIONS_REQUEST_CALL_PHONE = 102;

    public ContactAdapter(ArrayList<ContactModel> arraylist,Context context) {
        this.dataSet = arraylist;
        this._c=context;
    }

    public void CallMobile(String number) {
        int permissionCheck = ContextCompat.checkSelfPermission(_c, CALL_PHONE);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    (Activity)_c,
                    new String[]{CALL_PHONE},
                    PERMISSIONS_REQUEST_CALL_PHONE);
        } else {
            _c.startActivity(new Intent(Intent.ACTION_CALL).setData(Uri.parse("tel:" + number)));
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_contact, parent, false);

//        view.setOnClickListener(MainActivity.myOnClickListener);

        MyViewHolder myViewHolder = new MyViewHolder(view);
        return myViewHolder;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        final ContactModel data = dataSet.get(position);
        holder.title.setText(data.getName());
        holder.check.setChecked(data.getCheckedBox());
        holder.phone.setText(data.getPhone());
        // Set image if exists
        ImageView ivCall = holder.imageView;
        try {
            if (data.getThumb() != null) {
                ivCall.setImageBitmap(data.getThumb());
            } else {
                ivCall.setImageResource(R.drawable.ic_tab_contact);
            }
            // Seting round image
//            Bitmap bm = BitmapFactory.decodeResource(_c.getResources(), R.drawable.ic_tab_contact); // Load default image
//            roundedImage = new RoundImage(bm);
//            holder.imageView.setImageDrawable(roundedImage);
        } catch (OutOfMemoryError e) {
            // Add default picture
            ivCall.setImageDrawable(this._c.getDrawable(R.drawable.ic_tab_contact));
            e.printStackTrace();
        }
        holder.ivCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(_c,"Calling "+data.getName(),Toast.LENGTH_SHORT).show();
                CallMobile(data.getPhone());
            }
        });
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }


    // Filter Class
  /*  public void filter(String charText) {
        charText = charText.toLowerCase(Locale.getDefault());
        _data.clear();
        if (charText.length() == 0) {
            _data.addAll(dataSet);
        } else {
            for (ContactModel wp : dataSet) {
                if (wp.getName().toLowerCase(Locale.getDefault())
                        .contains(charText)) {
                    _data.add(wp);
                }
            }
        }
        notifyDataSetChanged();
    }
*/
    public static class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView,ivCall;
        TextView title, phone;
        CheckBox check;

        public MyViewHolder(View itemView) {
            super(itemView);
            this.title = (TextView) itemView.findViewById(R.id.tvName);
            this.check = (CheckBox) itemView.findViewById(R.id.check);
            this.phone = (TextView) itemView.findViewById(R.id.tvNumber);
            this.imageView = (ImageView) itemView.findViewById(R.id.ivUserImage);
            this.ivCall=(ImageView)itemView.findViewById(R.id.ivCallType);
        }
    }
}
