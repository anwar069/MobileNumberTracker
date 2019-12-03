package com.mobilenumbertracker.mobilenumbertracker;

import android.app.ProgressDialog;
import android.content.ContentResolver;
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
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.mobilenumbertracker.mobilenumbertracker.utils.Constants;
import com.mobilenumbertracker.mobilenumbertracker.utils.HTTPPost;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import pref.PrefManager.PrefManager;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, SearchFragment.OnFragmentInteractionListener {

    private static final String SELECTED_ITEM = "arg_selected_item";
    private BottomNavigationView mBottomNav;
    private int mSelectedItem;

    String LoginPrefFileName = "loginPref";
    SharedPreferences pref;

    ImageButton btnLogin;
    LinearLayout llLoginInfo;
    NavigationView navigationView;
    ImageView ivUserImage;
    View header;
    Menu nav_Menu;
    ContentResolver resolver;
    private PrefManager prefManager;


    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();


        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mBottomNav = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        mBottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                selectFragment(item);
                return true;
            }
        });


        header = navigationView.getHeaderView(0);
        nav_Menu = navigationView.getMenu();

        btnLogin = (ImageButton) header.findViewById(R.id.ibLogin);
        llLoginInfo = (LinearLayout) header.findViewById(R.id.llLoginInfo);
        ivUserImage = (ImageView) header.findViewById(R.id.ivLoginImg);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(HomeActivity.this, LoginActivity.class));
            }
        });

        setLoginInfo();

        MenuItem selectedItem;

        selectedItem = mBottomNav.getMenu().getItem(1);
        selectFragment(selectedItem);
        selectedItem.setChecked(true);

    }

    @Override
    protected void onStart() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        mGoogleApiClient.connect();
        super.onStart();
    }

    private void setLoginInfo() {
        pref = getApplicationContext().getSharedPreferences(LoginPrefFileName, MODE_PRIVATE);

        Boolean logged_in = pref.getBoolean("IS_LOGIN", false);

        if (!logged_in) {
            btnLogin.setVisibility(View.VISIBLE);
            llLoginInfo.setVisibility(View.GONE);
            nav_Menu.findItem(R.id.account).setVisible(false);
        } else {
            btnLogin.setVisibility(View.GONE);
            llLoginInfo.setVisibility(View.VISIBLE);
            nav_Menu.findItem(R.id.account).setVisible(true);

            String name = pref.getString("LOGIN_NAME", "");
            String imgURL = pref.getString("LOGIN_URL", "");

            if (!"".equals(imgURL)) {
                Glide.with(getApplicationContext()).load(imgURL)
                        .asBitmap()
                        .centerCrop()
                        .into(new BitmapImageViewTarget(ivUserImage) {
                            @Override
                            protected void setResource(Bitmap resource) {
                                RoundedBitmapDrawable circularBitmapDrawable =
                                        RoundedBitmapDrawableFactory.create(getApplicationContext().getResources(), resource);
                                circularBitmapDrawable.setCircular(true);
                                ivUserImage.setImageDrawable(circularBitmapDrawable);
                            }
                        });
            }
            ((TextView) header.findViewById(R.id.loginName)).setText(name);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshSearchFragment();
    }

    private void refreshSearchFragment() {
        Fragment frag = SearchFragment.newInstance("Search", "Result");
        getSupportFragmentManager().beginTransaction().replace(R.id.container, frag).commit();
    }

    private void selectFragment(MenuItem item) {
        Fragment frag = null;
        // init corresponding fragment
        switch (item.getItemId()) {
            case R.id.action_call:
                frag = CallFragment.newInstance("Call History", "Result");
                break;
            case R.id.action_search:
                frag = SearchFragment.newInstance("Search", "Result");
                break;
            case R.id.action_contact:
                frag = ContactFragment.newInstance("Contacts", "Result");
                break;
            case R.id.action_block:
                frag = BlockFragment.newInstance("Block", "Result");
                break;

        }

        // update selected item
        mSelectedItem = item.getItemId();

        // uncheck the other items.
        for (int i = 0; i < mBottomNav.getMenu().size(); i++) {
            MenuItem menuItem = mBottomNav.getMenu().getItem(i);
            menuItem.setChecked(menuItem.getItemId() == item.getItemId());
        }

        updateToolbarText(item.getTitle());

        if (frag != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.container, frag, frag.getTag());
            ft.commit();
        }
    }

    private Boolean exit = false;

    @Override
    public void onBackPressed() {
        if (exit) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Press Back again to Exit.",
                    Toast.LENGTH_SHORT).show();
            exit = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    exit = false;
                }
            }, 3 * 1000);

        }
    }

    private void updateToolbarText(CharSequence text) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(text);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_help) {
            // Handle the camera action
//        } else if (id == R.id.nav_gallery) {
//
//        } else if (id == R.id.nav_slideshow) {
//
//        } else if (id == R.id.nav_manage) {
            startActivity(new Intent(HomeActivity.this, HelpActivity.class));

        } else if (id == R.id.nav_share) {
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("text/html");
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, Html.fromHtml("<p>Track any mobile number in India on Google Maps for free. Find details like Owner Name, City, Mobile Operator, and Telecom Circle of a mobile number in few clicks, try it now. <a>http://www.mobilenumbertracker.com/</a></p>"));
            startActivity(Intent.createChooser(sharingIntent, "Share using"));

        } else if (id == R.id.nav_send) {
            Intent whatsappIntent = new Intent(Intent.ACTION_SEND);
            whatsappIntent.setType("text/plain");
            whatsappIntent.setPackage("com.whatsapp");
            whatsappIntent.putExtra(Intent.EXTRA_TEXT, "Track any mobile number in India on Google Maps for free. Find details like Owner Name, City, Mobile Operator, and Telecom Circle of a mobile number in few clicks, try it now. http://www.mobilenumbertracker.com");
            try {
                startActivity(whatsappIntent);
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(getApplicationContext(), "Whatsapp have not been installed.", Toast.LENGTH_SHORT).show();
            }
        } else if (id == R.id.nav_setting) {
            startActivity(new Intent(HomeActivity.this, SettingsActivity.class));
        } else if (id == R.id.logout) {
            revokeAccess();
        } else if (id == R.id.nav_sync) {
            new AsyncContactsSync().execute();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @NonNull
    private JSONArray getContactJson() {
        resolver = this.getContentResolver();
        Cursor phones = this.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");

        JSONArray contactArray = new JSONArray();

        while (phones.moveToNext()) {
            Bitmap bit_thumb = null;
            String id = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
            String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
//            String EmailAddr = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));

            String Email = null;
            Cursor emails = resolver.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null, ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = " + id, null, null);
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

            String[] fullName = name.split(" ");

            try {
                contactJSON.put("Id", 0);

                contactJSON.put("FirstName", fullName[0]);

                if (fullName.length > 1)
                    contactJSON.put("LastName", fullName[fullName.length - 1]);
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

        String response = "";
        ProgressDialog progressDialog;

        @Override
        protected String doInBackground(String... params) {
            publishProgress("Syncing contacts..."); // Calls onProgressUpdate()
            HTTPPost postContact = new HTTPPost();

            try {
                response = postContact.post(Constants.postUrl, getContactJson().toString());

            } catch (IOException e) {
                e.printStackTrace();
            }
            return response;
        }


        @Override
        protected void onPostExecute(String result) {
            // execution of result of Long time consuming operation
//            if (!"".equals(result))
////                Toast.makeText(getApplicationContext(), "Contacts synced successfully.", Toast.LENGTH_SHORT).show();
//
//                prefManager = new PrefManager(HomeActivity.this);
//            if (prefManager.isFirstTimeSync()) {
//                prefManager.setFirstTimeSync(false);
//            }

        }


        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(HomeActivity.this,
                    "Mobile Number Tracker",
                    "Syncing contacts...");

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    prefManager = new PrefManager(HomeActivity.this);
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

    private void showSuccesDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);

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
                        refreshSearchFragment();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    private void revokeAccess() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        // ...
                        pref = getApplicationContext().getSharedPreferences(LoginPrefFileName, MODE_PRIVATE);
                        SharedPreferences.Editor editor = pref.edit();
                        editor.putBoolean("IS_LOGIN", false);
                        editor.putString("LOGIN_NAME", "");
                        editor.putString("LOGIN_URL", "");
                        editor.commit();
                        Toast.makeText(getApplicationContext(), "Logged Out", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                        startActivity(i);
                    }
                });
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        Toast.makeText(this, "Changed", Toast.LENGTH_SHORT).show();
    }
}
