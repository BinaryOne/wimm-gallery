package com.jordan.wimm.gallery;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.util.ArrayList;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import com.wimm.framework.app.LauncherActivity;
import com.wimm.framework.view.AdapterViewTray;

public class GalleryActivity extends LauncherActivity {
	AdapterViewTray mTray;
	ImageTrayAdapter mAdapter;
	
	RelativeLayout mInstructions;
	
	ArrayList<Thumbnail> mThumbs;
	Handler mHandler;
	
	boolean mGetContent=false;
	
	final File mPath = new File(Environment.getExternalStorageDirectory(),"/DCIM");
	final static int ADD = 212, INTENT = 123;
	
	boolean noImages = false;
	
	public static boolean KILL = false;
	
	public void onResume() {
		super.onResume();
		
		if(KILL) {
			KILL = false;
			finish();
		}
	}
	
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.main);        
        
        if(getIntent() != null && getIntent().getAction() != null && 
        		getIntent().getAction().equals(Intent.ACTION_GET_CONTENT)) {
           	mGetContent = true;
        }
        
        mTray = (AdapterViewTray) findViewById(R.id.tray);
        mInstructions = (RelativeLayout) findViewById(R.id.instructions);
        mThumbs = new ArrayList<Thumbnail>();
        
        mHandler = new Handler() {       	
        	public void handleMessage(Message m) {
        		switch(m.what) {
        		case ADD:
	        		mThumbs.add(new Thumbnail(
	        			(Bitmap) m.obj, m.getData().getString("path"))
	        		);
	        		
	        		mAdapter.setBitmaps(mThumbs);
	        		break;
        		case INTENT:
        			int index = mTray.getIndex();
        			sendIntent(index);
        			break;
        		}
        	}
        };
        
        File[] files = loadImages();	
        if(files!=null && files.length > 0) {     	        	
        	for(File file:files)
        		new ImageLoader(file).start();
        	
            mAdapter = new ImageTrayAdapter(this, mHandler, mThumbs);
            
            if(mTray != null && mAdapter != null)
            	mTray.setAdapter(mAdapter);
        } else {
        	Log.d("Gallery", "No images to display...");
        	mTray.setVisibility(View.GONE);
        	mInstructions.setVisibility(View.VISIBLE);
        	noImages = true;
        }
    }
    
    public boolean dragCanExit() {
    	if(mInstructions.getChildCount() == 0) 
    		return true;
        return mInstructions.getChildAt(0).getTop() == 0;
    }
    
    private void sendIntent(int index) {
		Intent intent = new Intent();
		intent.putExtra("uri", mThumbs.get(index).getPath());
		if(mGetContent) {
    		setResult(RESULT_OK, intent);
    		finish();
    	} else {
    		intent.setType("image/*");
    		intent.setAction(Intent.ACTION_VIEW);

    		startActivity(intent);
    	}
    }
    
    private File[] loadImages() { 	
        File images = new File(Environment.getExternalStorageDirectory(),"Images");
        	images.mkdir();
        	
        Log.d("path", images.getAbsolutePath());
        File[] files = images.listFiles( new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return (name.toLowerCase().endsWith(".jpg") ||
						name.toLowerCase().endsWith(".jpeg") ||
						name.toLowerCase().endsWith(".png"));
			}
		});
		
		return files;
    }
           
    private class ImageLoader extends Thread {
    	File file;
    	
    	public ImageLoader(File file) {
    		this.file = file;
    	}
    	
    	public Bitmap loadImage(File path, BitmapFactory.Options opts) {
    		try {
				return BitmapFactory.decodeStream(
						new BufferedInputStream(
								new FileInputStream(file), 512
						), null, opts
					);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			
			return null;
    	}
    	
    	public void run() {
    		//File thumbFile = new File(mPath, file.getName());
    		/*if(thumbFile.exists()) {
		   		Bundle data = new Bundle();
	   				data.putString("path", file.getAbsolutePath());
	   			Message message = new Message();
	   				//message.obj = loadImage(thumbFile, null);
	   				//message.setData(data);
	   				
	   			//mHandler.sendMessage(message);

    		} else {*/
	    		BitmapFactory.Options opts = new BitmapFactory.Options();
	    			opts.inJustDecodeBounds = true;
	    		
		    	loadImage(file, opts);
		    		    	
		    	if(opts.outWidth != -1) {
		   			try {
			    		float scale = Math.max(opts.outHeight, opts.outWidth) / 160f;
			    		
			    		opts.inJustDecodeBounds = false;
			    		opts.inSampleSize = (int) scale;
			    					   				    		
				   		Bitmap src = loadImage(file, opts);	
				   					   		
			 	   		int height = src.getHeight();
				   		int width = src.getWidth();
				    	
				   		Matrix matrix = new Matrix();
				   		float s = 160f / Math.max(height, width);
				   		matrix.setScale(s, s);
				   		
				   		int x = (int) (160 - (width * s) / 2);
				   		int y = (int) (160 - (height * s) / 2);
				   		matrix.setTranslate(x, y);
				   		
				   		Bitmap img = Bitmap.createBitmap(src, 0, 0, src.getWidth(),
					    		src.getHeight(), matrix, true);
				   				   						   		
				   		Bundle data = new Bundle();
				   			data.putString("path", file.getAbsolutePath());
						Message message = new Message();
							message.obj = img;
							message.what = ADD;
							message.setData(data);
							
						mHandler.sendMessage(message);
						
				   		img.compress(CompressFormat.PNG, 100, new FileOutputStream(
				   				new File(mPath, file.getName()))
					   	);
		    		} catch(Exception e) {
		    			//Log.e("Error Loading", e.getLocalizedMessage());
		    			//src.recycle();
		    		}
		    	}
    		//}
	    }
    }
}