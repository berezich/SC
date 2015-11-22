package com.berezich.sportconnector.ImageViewer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

import com.berezich.sportconnector.FileManager;
import com.berezich.sportconnector.R;
import com.berezich.sportconnector.backend.sportConnectorApi.model.Picture;
import com.google.api.client.json.gson.GsonFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import uk.co.senab.photoview.PhotoView;


public class ImgViewPagerActivity extends Activity {

	private final String TAG = "MyLog_ImgPager";
	public static final String PIC_LIST_EXTRAS = "picList";
	public static final String PIC_INDEX_EXTRAS = "picIndex";
	private ImgViewPager mViewPager;

	private static List<Picture> picLst = new ArrayList<>();
	int index;
	int prev;

    @Override
	public void onCreate(Bundle savedInstanceState) {
		try {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.acitvity_img_view_pager);
			GsonFactory gsonFactory = new GsonFactory();
			Intent intent = getIntent();
			if(intent!=null)
            {
                picLst.clear();
                ArrayList<String> picLstStr = intent.getStringArrayListExtra(PIC_LIST_EXTRAS);
                index = intent.getIntExtra(PIC_INDEX_EXTRAS,0);
                prev = index;
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

			mViewPager = (ImgViewPager) findViewById(R.id.img_view_pager);
			setContentView(mViewPager);
			mViewPager.setAdapter(new SamplePagerAdapter());
			mViewPager.setCurrentItem(index);

			mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

                @Override
                public void onPageSelected(int position) {
					try {
						Log.d(TAG, "onPageSelected, position = " + position);
						PhotoView photoView;
						if(prev>=0 && prev < picLst.size()) {
                            photoView = (PhotoView) mViewPager.findViewWithTag(prev);
                            if (photoView != null)
                                photoView.setScale(1);
                        }
						prev = position;
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

                @Override
                public void onPageScrolled(int position, float positionOffset,
                                           int positionOffsetPixels) {
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                }
            });
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	static class SamplePagerAdapter extends PagerAdapter {

		@Override
		public int getCount() {
			if(picLst!=null)
				return picLst.size();
			else
				return 0;
		}

		@Override
		public View instantiateItem(ViewGroup container, int position) {
			PhotoView photoView = new PhotoView(container.getContext());
			try {
				Picture picture = picLst.get(position);

				FileManager.providePhotoForImgView(container.getContext(), photoView, picture, FileManager.TEMP_DIR, 0);

				container.addView(photoView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
				photoView.setScale(1);
				photoView.setTag(position);
			} catch (Exception e) {
				e.printStackTrace();
			}
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

}
