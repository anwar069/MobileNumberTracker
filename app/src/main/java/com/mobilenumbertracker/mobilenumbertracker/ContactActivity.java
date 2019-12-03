package com.mobilenumbertracker.mobilenumbertracker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mobilenumbertracker.mobilenumbertracker.utils.ImageUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static android.Manifest.permission.CALL_PHONE;

public class ContactActivity extends AppCompatActivity {
    ImageButton ibUserPic;
    private static final int PERMISSIONS_REQUEST_CALL_PHONE = 102;

    String BlockPrefFileName="blockPref";
    SharedPreferences pref;

    String name, number, address, imageUrl,email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        name = getIntent().getStringExtra("Name");
        number = getIntent().getStringExtra("Number");
        address = getIntent().getStringExtra("Location");
        email= getIntent().getStringExtra("Email");
        imageUrl=getIntent().getStringExtra("ImageURL");


        pref= getApplicationContext().getSharedPreferences(BlockPrefFileName, MODE_PRIVATE);


        //Get Circular Drawable
        Drawable myDrawable = ContextCompat.getDrawable(this, R.drawable.face);
        Bitmap bitImage = ((BitmapDrawable) myDrawable).getBitmap();
        Bitmap circularImage = ImageUtil.getCroppedBitmap(bitImage);
        ibUserPic = ((ImageButton) findViewById(R.id.ib_user_profile_photo));
        ibUserPic.setImageDrawable(new BitmapDrawable(getResources(), circularImage));
        ibUserPic.bringToFront();

        ((TextView) findViewById(R.id.tv_user_profile_name)).setText(name);
        ((TextView) findViewById(R.id.tv_user_profile_location)).setText(address);
        ((TextView) findViewById(R.id.tv_user_profile_email)).setText(email);

        TextView tvNumber = (TextView) findViewById(R.id.tv_user_profile_number);
        tvNumber.setText(number);
        tvNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CallMobile();
            }
        });

//        if(!"".equals(address.trim()))
            ((TextView)findViewById(R.id.tv_user_profile_location)).setText(address);
//        else
//            ((TextView)findViewById(R.id.tv_user_profile_location)).setText("Location not available.");

        ((ImageView)findViewById(R.id.iv_sms)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sendIntent = new Intent(Intent.ACTION_VIEW);
                sendIntent.setData(Uri.parse("sms:"+number));
                startActivity(sendIntent);
            }
        });

        ((ImageView)findViewById(R.id.iv_call_contact)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CallMobile();
            }
        });

        ((ImageView)findViewById(R.id.iv_add_contact)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_INSERT);
                intent.setType(ContactsContract.Contacts.CONTENT_TYPE);

                intent.putExtra(ContactsContract.Intents.Insert.NAME, name);
                intent.putExtra(ContactsContract.Intents.Insert.PHONE,number);
//              intent.putExtra(ContactsContract.Intents.Insert.EMAIL, );

                startActivity(intent);
            }
        });

        ((ImageView)findViewById(R.id.iv_block_contact)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addNumberToBlockList(number);
            }
        });
    }

    public static Drawable drawableFromUrl(String url) throws IOException {
        Bitmap x;

        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.connect();
        InputStream input = connection.getInputStream();

        x = BitmapFactory.decodeStream(input);
        return new BitmapDrawable(x);
    }

    private void addNumberToBlockList(String number) {
        String JSONString=pref.getString("BLOCK_LIST","");
        JSONObject blockJson=null;


        if("".equals(JSONString)){
            blockJson= new JSONObject();
            try {
                blockJson.put(number,name);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else {
            try {
                blockJson=new JSONObject(JSONString);
                blockJson.put(number,name);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        SharedPreferences.Editor editor = pref.edit();
        editor.putString("BLOCK_LIST",blockJson.toString());
        editor.commit();

        Toast.makeText(getBaseContext(),name+" added to Block List",Toast.LENGTH_SHORT).show();

    }


    public void CallMobile() {
        int permissionCheck = ContextCompat.checkSelfPermission(this, CALL_PHONE);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{CALL_PHONE},
                    PERMISSIONS_REQUEST_CALL_PHONE);
        } else {
            startActivity(new Intent(Intent.ACTION_CALL).setData(Uri.parse("tel:" + number)));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {

            case PERMISSIONS_REQUEST_CALL_PHONE:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    CallMobile();
                } else {
                    Log.d("TAG", "Call Permission Not Granted");
                }
                break;

            default:
                break;
        }
    }
}
