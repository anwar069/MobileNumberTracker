package com.mobilenumbertracker.mobilenumbertracker.call;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.mobilenumbertracker.mobilenumbertracker.R;
import java.util.ArrayList;

import static android.Manifest.permission.CALL_PHONE;

/**
 * Created by Ahmed on 11/01/2017.
 */

public class CallAdapter extends RecyclerView.Adapter<CallAdapter.CallViewHolder> {

    private ArrayList<CallModel> dataSet;
    private Context acContext;
    private static final int PERMISSIONS_REQUEST_CALL_PHONE = 102;

    class CallViewHolder extends RecyclerView.ViewHolder {

        TextView textViewUserName;
        TextView textViewDuration;
        ImageView imageUserIcon;
        ImageView imageCallTypeIcon;
        CardView cvCallHistory;

        public CallViewHolder(View itemView) {
            super(itemView);
            this.textViewUserName = (TextView) itemView.findViewById(R.id.tvName);
            this.textViewDuration = (TextView) itemView.findViewById(R.id.tvDuration);
            this.imageUserIcon = (ImageView) itemView.findViewById(R.id.ivUserImage);
            this.imageCallTypeIcon=(ImageView) itemView.findViewById(R.id.ivCallType);
            this.cvCallHistory=(CardView)itemView.findViewById(R.id.cv_call_history);
        }

    }

    public CallAdapter(Context context,ArrayList<CallModel> data) {
        this.dataSet = data;
        this.acContext=context;
    }
    public void CallMobile(String number) {
        int permissionCheck = ContextCompat.checkSelfPermission(acContext, CALL_PHONE);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    (Activity)acContext,
                    new String[]{CALL_PHONE},
                    PERMISSIONS_REQUEST_CALL_PHONE);
        } else {
           acContext.startActivity(new Intent(Intent.ACTION_CALL).setData(Uri.parse("tel:" + number)));
        }
    }
    @Override
    public CallViewHolder onCreateViewHolder(ViewGroup parent,
                                             int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.call_card_layout, parent, false);

//        view.setOnClickListener(MainActivity.myOnClickListener);

        CallViewHolder callViewHolder = new CallViewHolder(view);
        return callViewHolder;
    }

    public static String getContactName(Context context, String phoneNumber) {
        ContentResolver cr = context.getContentResolver();
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        Cursor cursor = cr.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
        if (cursor == null) {
            return null;
        }
        String contactName = null;
        if (cursor.moveToFirst()) {
            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
        }

        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        return contactName;
    }

    @Override
    public void onBindViewHolder(final CallViewHolder holder, final int listPosition) {

        TextView textViewName = holder.textViewUserName;
        TextView textViewDuration = holder.textViewDuration;
        ImageView imageViewUser = holder.imageUserIcon;
        ImageView imageViewCallType=holder.imageCallTypeIcon;
        CardView cardLayout=  holder.cvCallHistory;


        final String number = dataSet.get(listPosition).getNumber();
        String name =getContactName(acContext,number);
        final String contactName = name == null ? number : name;
        textViewName.setText(contactName);
        textViewDuration.setText(dataSet.get(listPosition).getCallDuration());
        imageViewCallType.setImageResource(dataSet.get(listPosition).getCallType()=="OUTGOING"? android.R.drawable.sym_call_outgoing:dataSet.get(listPosition).getCallType()=="INCOMING"?android.R.drawable.sym_call_incoming:android.R.drawable.sym_call_missed);
//        imageView.setImageResource(dataSet.get(listPosition).getImage());

        imageViewCallType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(acContext,"Calling "+contactName,Toast.LENGTH_SHORT).show();
                CallMobile(number);
            }
        });

    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }
}