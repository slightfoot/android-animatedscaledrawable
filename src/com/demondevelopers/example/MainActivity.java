package com.demondevelopers.example;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.app.Activity;
import android.content.Context;


public class MainActivity extends Activity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		ListView listView = new ListView(this);
		listView.setAdapter(new TestAdapter(this));
		setContentView(listView);
	}
	
	
	private static class TestAdapter extends BaseAdapter
	{
		private static int[] mGravity = {
			Gravity.TOP    | Gravity.LEFT,
			Gravity.TOP    | Gravity.CENTER_HORIZONTAL,
			Gravity.TOP    | Gravity.RIGHT,
			Gravity.RIGHT  | Gravity.CENTER_VERTICAL,
			Gravity.BOTTOM | Gravity.RIGHT,
			Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL,
			Gravity.BOTTOM | Gravity.LEFT,
			Gravity.LEFT   | Gravity.CENTER_VERTICAL,
			Gravity.FILL
		};
		
		private View[] mViews = new View[3 + mGravity.length];
		
		private String[] mNames;
		
		
		public TestAdapter(Context context)
		{
			mNames = context.getResources().getStringArray(R.array.item_names);
		}
		
		@SuppressWarnings("deprecation")
		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			convertView = mViews[position];
			if(convertView == null){
				
				AnimatedScaleDrawable drawable = new AnimatedScaleDrawable(
					parent.getContext().getResources().getDrawable(R.drawable.heart));
				drawable.setInterpolator(new BounceInterpolator());
				drawable.setInvertTransformation(true);
				drawable.setDuration(500);
				
				FrameLayout frame = (FrameLayout)LayoutInflater.from(parent.getContext())
					.inflate(R.layout.item_frame, parent, false);
				
				if(position == 0){
					// ProgressBar example
					ProgressBar progress = (ProgressBar)LayoutInflater.from(parent.getContext())
						.inflate(R.layout.item_progress, frame, false);
					progress.setIndeterminateDrawable(drawable);
					frame.addView(progress);
				}
				else{
					if(position == 1 || position == 2){
						// Background drawable example
						frame.setBackgroundDrawable(drawable);
						if(position == 2){
							drawable.setUseBounds(false);
						}
					}else{
						// Foreground's with Gravity example
						frame.setForeground(drawable);
						frame.setForegroundGravity(mGravity[position - 3]);
					}
					// no need to call drawable.start() for ProgressBar widgets
					drawable.start();
				}
				
				TextView textView = (TextView)frame.findViewById(R.id.text);
				textView.setText(String.format("#%02d %s", position, mNames[position]));
				
				convertView = frame;
			}
			
			return convertView;
		}
		
		@Override
		public int getItemViewType(int position)
		{
			return IGNORE_ITEM_VIEW_TYPE;
		}
		
		@Override
		public int getCount()
		{
			return mViews.length;
		}
		
		@Override
		public Object getItem(int position)
		{
			return null;
		}
		
		@Override
		public long getItemId(int position)
		{
			return position;
		}
		
		@Override
		public boolean hasStableIds()
		{
			return true;
		}
		
		@Override
		public boolean isEnabled(int position)
		{
			return false;
		}
	}
}
