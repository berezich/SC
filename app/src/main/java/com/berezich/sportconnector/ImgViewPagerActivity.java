/*******************************************************************************
 * Copyright 2011, 2012 Chris Banes.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.berezich.sportconnector;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

import com.berezich.sportconnector.backend.sportConnectorApi.model.Picture;
import com.google.api.client.json.gson.GsonFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
//import com.nostra13.universalimageloader.core.ImageLoader;
//import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import uk.co.senab.photoview.PhotoView;

/**
 * Lock/Unlock button is added to the ActionBar.
 * Use it to temporarily disable ViewPager navigation in order to correctly interact with ImageView by gestures.
 * Lock/Unlock state of ViewPager is saved and restored on configuration changes.
 * 
 * Julia Zudikova
 */

public class ImgViewPagerActivity extends Activity {

	//private static final String ISLOCKED_ARG = "isLocked";
	public static String PIC_LIST_EXTRAS = "picList";
	private ViewPager mViewPager;
	//private MenuItem menuLockItem;

	private static List<Picture> picLst = new ArrayList<>();

    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.acitvity_img_view_pager);
        //mViewPager = (HackyViewPager) findViewById(R.id.view_pager);
		GsonFactory gsonFactory = new GsonFactory();
		Intent intent = getIntent();
		if(intent!=null)
		{
			picLst.clear();
			ArrayList<String> picLstStr = intent.getStringArrayListExtra(PIC_LIST_EXTRAS);
			Picture pic;
			for (String str:picLstStr)
				try {
					pic = gsonFactory.fromString(str, Picture.class);
					if(pic!=null)
						picLst.add(pic);
				} catch (IOException e) {
					e.printStackTrace();
				}
		}

		mViewPager = (ViewPager) findViewById(R.id.img_view_pager);
		setContentView(mViewPager);

		mViewPager.setAdapter(new SamplePagerAdapter());
		
		/*if (savedInstanceState != null) {
			boolean isLocked = savedInstanceState.getBoolean(ISLOCKED_ARG, false);
			((HackyViewPager) mViewPager).setLocked(isLocked);
		}*/
	}

	static class SamplePagerAdapter extends PagerAdapter {

		/*private static final int[] sDrawables = { R.drawable.wallpaper, R.drawable.wallpaper, R.drawable.wallpaper,
				R.drawable.wallpaper, R.drawable.wallpaper, R.drawable.wallpaper };*/

		@Override
		public int getCount() {
			if(picLst!=null)
				return picLst.size();
			else
				return 0;
		}
		/*public int getCount() {
			return sDrawables.length;
		}*/

		@Override
		public View instantiateItem(ViewGroup container, int position) {
			PhotoView photoView = new PhotoView(container.getContext());
			//photoView.setImageResource(sDrawables[position]);

			/*if (!ImageLoader.getInstance().isInited()) {
				ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(container.getContext()).build();
				ImageLoader.getInstance().init(config);
			}

			ImageLoader.getInstance().displayImage("http://lh3.googleusercontent.com/D3NzglFDsCgdKu0s6jxTMTQjmctPvv2B7LzLAO6wRHzw8JiPH5Siw9vWUBXfA2Y_3BYfDfn6CRvCOZNjyW07Br0hSLIKdDPe=s0", photoView);
*/
			Picture picture = picLst.get(position);

			FileManager.providePhotoForImgView(container.getContext(), photoView, picture, "temp",0);

			// Now just add PhotoView to ViewPager and return it
			container.addView(photoView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

			return photoView;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}

	}
	/*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.viewpager_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menuLockItem = menu.findItem(R.id.menu_lock);
        toggleLockBtnTitle();
        menuLockItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				toggleViewPagerScrolling();
				toggleLockBtnTitle();
				return true;
			}
		});

        return super.onPrepareOptionsMenu(menu);
    }
    
    private void toggleViewPagerScrolling() {
    	if (isViewPagerActive()) {
    		//((HackyViewPager) mViewPager).toggleLock();
    		((ViewPager) mViewPager).toggleLock();
    	}
    }
    
    private void toggleLockBtnTitle() {
    	boolean isLocked = false;
    	if (isViewPagerActive()) {
    		isLocked = ((HackyViewPager) mViewPager).isLocked();
    	}
    	String title = (isLocked) ? getString(R.string.menu_unlock) : getString(R.string.menu_lock);
    	if (menuLockItem != null) {
    		menuLockItem.setTitle(title);
    	}
    }*/

    private boolean isViewPagerActive() {
    	//return (mViewPager != null && mViewPager instanceof HackyViewPager);
    	return (mViewPager != null && mViewPager instanceof ViewPager);
    }
    
	/*@Override
	protected void onSaveInstanceState(@NonNull Bundle outState) {
		if (isViewPagerActive()) {
			outState.putBoolean(ISLOCKED_ARG, ((HackyViewPager) mViewPager).isLocked());
    	}
		super.onSaveInstanceState(outState);
	}*/
    
}
