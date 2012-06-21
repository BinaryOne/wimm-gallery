package com.jordan.wimm.gallery;

import android.graphics.Bitmap;

class Thumbnail {
	private Bitmap mThumb;
	private String mPath;
	
	public Thumbnail(Bitmap thumb, String path) {
		mThumb = thumb;
		mPath = path;
	}

	public String getPath() {
		return mPath;
	}

	public Bitmap getThumb() {
		return mThumb;
	}
}