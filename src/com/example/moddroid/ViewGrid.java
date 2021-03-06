package com.example.moddroid;

import java.util.ArrayList;
import java.util.TreeSet;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;

public class ViewGrid extends Activity {

	private SharedPreferences preferences; 	
	private GridView grid;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_grid);

		//gets the data preferences
		preferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
		grid = (GridView)findViewById(R.id.grid);

		final ArrayList<String> buttonLabels = new ArrayList<String>();
		
		//addings the buttons to the screen
		for(String address: preferences.getStringSet("addresses", new TreeSet<String>())) 
			buttonLabels.add(address.trim());

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_dropdown_item_1line, buttonLabels);

		grid.setAdapter(adapter);

		grid.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			
				//make the pending intent and adding the data to it
				Intent intent = new Intent(getApplicationContext(),LiveData.class);
				intent.putExtra("address", buttonLabels.get((int)id));
				intent.putExtra("ip", preferences.getString("ip", ""));
				startActivity(intent);

			}
		});

	}
 
}
