package com.jordan.wimm.gallery;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class ViewerActivity extends Activity {
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		Viewer viewer = new Viewer(this);
		setContentView(viewer);
		
        if(getIntent() != null && getIntent().getAction() != null && 
        		getIntent().getAction().equals(Intent.ACTION_VIEW)) {
           Log.d("Load Image: ", getIntent().getStringExtra("uri"));
           viewer.setBitmap(getIntent().getStringExtra("uri"));
        }
	}
}
