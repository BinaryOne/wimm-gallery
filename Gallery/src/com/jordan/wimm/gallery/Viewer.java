package com.jordan.wimm.gallery;

import java.io.FileOutputStream;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Process;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

public class Viewer extends View {
	int SCR_WIDTH = 160, SCR_HEIGHT = 160;
	
	int posX, posY;
	
	float initX, initY;
	float deltaX, deltaY;
	
	Bitmap mBitmap, mScaled, mCloseBitmap, mZoomInBitmap, mZoomOutBitmap, mSetWatchFaceBitmap;
	Rect mWindow, mClose, mZoomIn, mZoomOut, mSetWatchFace;
	
	int MAX_ZOOM = 8, MIN_ZOOM = 0;
	
	int mZoomLvl=0, mPreZoomLvl=0;
	float mScale;
	
	ViewerActivity mAct = null;
	
	Paint mPaint = new Paint();
	
	long mTime; int FADE_INTERVAL = 2; boolean mUIShow = true;
	
	boolean mWatchFaceInstalled = false;
	
	public Viewer(Context context) {
		super(context);
		
		mAct =(ViewerActivity) context;
		
		mCloseBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.close);
		mClose = new Rect(128, 0, 160, 32);
			
		mZoomInBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.zoomin); 
		mZoomIn = new Rect(0, 0, 32, 32);
		
		mZoomOutBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.zoomout);
		mZoomOut = new Rect(0, 128, 32, 160);
		
		mSetWatchFaceBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.watchface);
		mSetWatchFace = new Rect(128, 128, 160, 160);
		
		mTime = System.currentTimeMillis();
		
		mWatchFaceInstalled = watchInstalled();
		
		(new Thread() {
			public void run() {
				while(true) {
					if((System.currentTimeMillis() - mTime)/1000 > FADE_INTERVAL  && mUIShow) {
						mUIShow = false;
						Viewer.this.postInvalidate();
					}
					
					try {
						Thread.sleep(1000);
					} catch(Exception e) {}
				}
			}
		}).start();
	}
	
	public void setBitmap(String path) {
		BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, opts);
			opts.inJustDecodeBounds = false;
		
		if(opts.outHeight > 2000 || opts.outWidth > 2000)
			opts.inSampleSize = (int) Math.max(opts.outHeight, opts.outWidth) / 2000;
		
		mBitmap = BitmapFactory.decodeFile(path, opts);
		mScaled = mBitmap;
		
		posX = mScaled.getWidth() / 2 - SCR_WIDTH / 2;
		posY = mScaled.getHeight() / 2 - SCR_HEIGHT / 2;
		mWindow = new Rect(posX, posY, posX+SCR_WIDTH, posY+SCR_HEIGHT);
		
		if(needsZoom())
			mZoomLvl = mPreZoomLvl = 2;
		else 
			mZoomLvl = mPreZoomLvl = 0;
		
		invalidate();
	}
	
	private void saveImage() {
		try {
			FileOutputStream os = getContext().openFileOutput("watchface", Context.MODE_WORLD_READABLE);
			
			Bitmap bitmap = Bitmap.createBitmap(160, 160, Bitmap.Config.ARGB_8888);
			Canvas canvas = new Canvas(bitmap);

			canvas.drawColor(Color.BLACK);
			canvas.drawBitmap(mScaled, mWindow,
					new Rect(0, 0, SCR_WIDTH, SCR_HEIGHT), null);

			bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
			os.flush();
			os.close();
			
			Toast.makeText(mAct, "Watch Face Set!", Toast.LENGTH_SHORT).show();
		} catch (Exception e) {}
	}
	
	private boolean needsZoom() {
		return !(mBitmap.getWidth() <= SCR_WIDTH || mBitmap.getHeight() <= SCR_HEIGHT);
	}

	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		if(mZoomLvl != mPreZoomLvl && needsZoom()) {
			Matrix matrix = new Matrix();
			
			matrix.setScale(mScale, mScale);
							
			mScaled = Bitmap.createBitmap(mBitmap, 0, 0,
				mBitmap.getWidth(), mBitmap.getHeight(), matrix, true);
						
			mPreZoomLvl = mZoomLvl;
		}
						
		canvas.drawBitmap(mScaled, mWindow,
			new Rect(0, 0, SCR_WIDTH, SCR_HEIGHT), null);
		
		if(mUIShow) {
			if(needsZoom()) {
				canvas.drawBitmap(mZoomInBitmap, null, mZoomIn, null);		
				canvas.drawBitmap(mZoomOutBitmap, null, mZoomOut, null);
			}
			if(mWatchFaceInstalled)
				canvas.drawBitmap(mSetWatchFaceBitmap, null, mSetWatchFace, null);
			canvas.drawBitmap(mCloseBitmap, null, mClose, null);
		}
	}
	
	public boolean onTouchEvent(MotionEvent event) {
		mTime = System.currentTimeMillis();
		mUIShow = true;
		
		switch(event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			initX = event.getX();
			initY = event.getY();
			
			if(mZoomIn.contains((int)initX, (int)initY)) {
				if(mZoomLvl > MIN_ZOOM) {
					mZoomLvl--;
					mScale = (float) (1f / Math.pow(2, mZoomLvl));
					
					posX = (int) (posX * Math.pow(2, mZoomLvl));
					posY = (int) (posY * Math.pow(2, mZoomLvl));
					
					return true;
				}
			}
			
			if(mZoomOut.contains((int)initX, (int)initY)) {
				if(mZoomLvl < MAX_ZOOM) {
					mZoomLvl++;
					mScale = (float) (1f / Math.pow(2, mZoomLvl));
					
					double smallSide = Math.min(mBitmap.getHeight(), mBitmap.getWidth());					
					if(smallSide * mScale < 160) {
						mScale = (float) (160f/smallSide);
						MAX_ZOOM = mZoomLvl;
					}
					
					posX = (int) (posX / Math.pow(2, mZoomLvl));
					posY = (int) (posY / Math.pow(2, mZoomLvl));
				
					return true;
				}
			}
			
			if(mSetWatchFace.contains((int)initX, (int)initY) && mWatchFaceInstalled) {
				saveImage();
				
				GalleryActivity.KILL = true;
				mAct.finish();
			}
			
			if(mClose.contains((int)initX, (int)initY)) {
				if(mAct != null)
					mAct.finish();	
				
				return true;
			}
			
			deltaX = deltaY = 0;
			break;
		case MotionEvent.ACTION_MOVE:
			deltaX = initX - event.getX();
			deltaY = initY - event.getY();
			
			posX += (int) deltaX;
			posY += (int) deltaY;
			
			if(posX < 0)
				posX = 0;
			if(posX + SCR_WIDTH > mScaled.getWidth())
				posX = mScaled.getWidth() - SCR_WIDTH;
			
			if(posY < 0)
				posY = 0;
			if(posY + SCR_HEIGHT > mScaled.getHeight())
				posY = mScaled.getHeight() - SCR_HEIGHT;
			
			mWindow = new Rect(posX, posY, posX+SCR_WIDTH, posY+SCR_HEIGHT);
			
			initX = event.getX();
			initY = event.getY();
			
			deltaX = deltaY = 0;
			break;
		case MotionEvent.ACTION_UP:			
			deltaX = initX - event.getX();
			deltaY = initY - event.getY();
			
			posX += (int) deltaX;
			posY += (int) deltaY;
			
			if(posX < 0)
				posX = 0;
			if(posX + SCR_WIDTH > mBitmap.getWidth())
				posX = mBitmap.getWidth() - SCR_WIDTH;
			
			if(posY < 0)
				posY = 0;
			if(posY + SCR_HEIGHT > mBitmap.getHeight())
				posY = mBitmap.getHeight() - SCR_HEIGHT;
			
			mWindow = new Rect(posX, posY, posX+SCR_WIDTH, posY+SCR_HEIGHT);
			break;
		}
		
		invalidate();
		return true;
	}
	
	private boolean watchInstalled() {
		Intent watch = new Intent("com.binary.gallery.GET_WATCHFACE");
		
        List<ResolveInfo> list = mAct.getPackageManager().queryIntentActivities(watch,     
                PackageManager.MATCH_DEFAULT_ONLY);    
        
        if(list.size() > 0)  
        	return true ;    
        
        return false;  
	}
}
