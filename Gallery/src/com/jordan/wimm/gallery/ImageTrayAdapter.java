package com.jordan.wimm.gallery;

import java.util.ArrayList;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class ImageTrayAdapter extends BaseAdapter {
	ArrayList<Thumbnail> mThumbs;
	LayoutInflater mInflate;
	Handler mHandler;
	Context mContext;

	public ImageTrayAdapter(Context context, Handler handler, ArrayList<Thumbnail> thumbs) {
		super();
		
		mThumbs = thumbs;
		mContext = context;
		mHandler = handler;
		mInflate = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	public void setBitmaps(ArrayList<Thumbnail> thumbs) {
		mThumbs = thumbs;
		notifyDataSetChanged();
	}
	
	@Override
	public int getCount() {
		return mThumbs.size();
	}

	@Override
	public View getView(int index, View convertView, ViewGroup parent) {
		ImageView view = (ImageView) convertView;
		
		if(view==null)
			view = new ImageView(mContext);

		if(mThumbs.get(index) != null) {
			view.setImageBitmap(mThumbs.get(index).getThumb());
			view.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mHandler.sendEmptyMessage(GalleryActivity.INTENT);
				}
			});
		}
		return view;
	}
	
	@Override
	public Object getItem(int index) { return null; }

	@Override
	public long getItemId(int arg0) { return -1; }
}
