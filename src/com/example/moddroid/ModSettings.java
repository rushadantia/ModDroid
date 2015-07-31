package com.example.moddroid;

import java.util.Set;
import java.util.TreeSet;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

public class ModSettings extends Activity {

	private SharedPreferences preferences; 
	private EditText ip;
	private EditText addresses;
	private Button save;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mod_settings);
	
		ip = (EditText)findViewById(R.id.ipTextField);
		addresses = (EditText)findViewById(R.id.addresses);
		save = (Button)findViewById(R.id.saveSettings);
		
		View view = this.getCurrentFocus();
		if (view != null) {  
		    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
		}
	
		preferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
		ip.setText(preferences.getString("ip", ""));
		addresses.setText(preferences.getStringSet("addresses", null).toString());
	
		final SharedPreferences.Editor editor = preferences.edit();
		
		save.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				editor.putString("ip", ip.getText().toString());
				
				Set<String> ads = new TreeSet<String>();
				
				for(String address: addresses.getText().toString().split(",")) 
				ads.add(address.trim());
				
				editor.putStringSet("addresses", ads);
				editor.apply();
				startActivity(new Intent(getApplicationContext(),MainActivity.class));
				finish();
			}
		});
		
	}

	
}