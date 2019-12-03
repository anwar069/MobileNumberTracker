package com.mobilenumbertracker.mobilenumbertracker.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.mobilenumbertracker.mobilenumbertracker.HomeActivity;
//import android.widget.Toast;
//import android.util.Log;

public class Myreceiver extends BroadcastReceiver{
 
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		System.out.println("MYRECEIVER");
			//Toast.makeText(Myreceiver.this, "MyReciver", Toast.LENGTH_SHORT).show();
		     Intent serviceLauncher = new Intent(context, HomeActivity.class);
		     context.startService(serviceLauncher);
		     //Log.v("TEST", "Service loaded at start");
		  
	}
 
}