package com.mobilenumbertracker.mobilenumbertracker;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.SyncHttpClient;
import com.mobilenumbertracker.mobilenumbertracker.contact.ContactModel;
import com.mobilenumbertracker.mobilenumbertracker.utils.HTTPPost;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;
import pref.PrefManager.PrefManager;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class ContactSyncService extends IntentService {

    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_SYNC = "com.mobilenumbertracker.mobilenumbertracker.action.SYNC";

    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "com.mobilenumbertracker.mobilenumbertracker.extra.PARAM1";

    Cursor phones;
    ContentResolver resolver;
    private PrefManager prefManager;
    private String postUrl = "http://api.mobilenumbertracker.com/v1/person/contacts/?auth_token=A525CKA30B760953CC8018C57C49FDA8";


    private AsyncHttpClient aClient = new SyncHttpClient();

    public ContactSyncService() {
        super("ContactSyncService");
    }


    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionFoo(Context context, String param1, String param2) {
        Intent intent = new Intent(context, ContactSyncService.class);
        intent.setAction(ACTION_SYNC);
        intent.putExtra(EXTRA_PARAM1, param1);
//        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
//    // TODO: Customize helper method
//    public static void startActionBaz(Context context, String param1, String param2) {
//        Intent intent = new Intent(context, ContactSyncService.class);
//        intent.setAction(ACTION_BAZ);
//        intent.putExtra(EXTRA_PARAM1, param1);
//        intent.putExtra(EXTRA_PARAM2, param2);
//        context.startService(intent);
//    }
    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_SYNC.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
//                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionSync(param1);
            }
//            else if (ACTION_BAZ.equals(action)) {
//                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
//                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
//                handleActionBaz(param1, param2);
//            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionSync(String param1) {
        // TODO: Handle action Foo

//        StringEntity entity = getContactEntity();
//
//        Toast.makeText(this,entity.toString(),Toast.LENGTH_LONG).show();
//
//        String temp=entity.toString();
//
//        Log.d("UPLOAD SERVICE",entity.toString());
//        aClient.post(this, postUrl, entity, "application/json", new AsyncHttpResponseHandler() {
//            @Override
//            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
//                String resp;
//                if(statusCode==200)
//                    Toast.makeText(ContactSyncService.this,responseBody.toString()+" Contacts synced with server",Toast.LENGTH_SHORT).show();
//            }
//
//            @Override
//            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
//                Toast.makeText(ContactSyncService.this,statusCode+" "+error.toString(),Toast.LENGTH_SHORT).show();
//            }
//        });

        HTTPPost postContact=new HTTPPost();

        try {
            String response= postContact.post(postUrl,getContactJson().toString());
            Toast.makeText(getApplicationContext(),"Contacts Synced with server" + response,Toast.LENGTH_SHORT).show();
            prefManager = new PrefManager(this);
            if (prefManager.isFirstTimeSync()) {
                prefManager.setFirstTimeSync(false);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private StringEntity getContactEntity() {
        StringEntity contactEntity=null;
        JSONArray contactArray = getContactJson();

        try {
            contactEntity=new StringEntity(contactArray.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return contactEntity;
    }

    @NonNull
    private JSONArray getContactJson() {
        resolver = this.getContentResolver();
        phones = this.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");

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

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
//    private void handleActionBaz(String param1, String param2) {
//        // TODO: Handle action Baz
//        throw new UnsupportedOperationException("Not yet implemented");
//    }
}
