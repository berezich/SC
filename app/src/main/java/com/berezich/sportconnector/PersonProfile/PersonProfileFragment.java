package com.berezich.sportconnector.PersonProfile;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.berezich.sportconnector.EndpointApi;
import com.berezich.sportconnector.FileManager;
import com.berezich.sportconnector.GoogleMap.SpotsData;
import com.berezich.sportconnector.LocalDataManager;
import com.berezich.sportconnector.MainActivity;
import com.berezich.sportconnector.R;
import com.berezich.sportconnector.UsefulFunctions;
import com.berezich.sportconnector.backend.sportConnectorApi.model.Person;
import com.berezich.sportconnector.backend.sportConnectorApi.model.Picture;
import com.berezich.sportconnector.backend.sportConnectorApi.model.Spot;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.IOUtils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by berezkin on 17.07.2015.
 */
public class PersonProfileFragment extends Fragment
        implements EndpointApi.GetUrlForUploadAsyncTask.OnAction,
                   FileManager.UploadFileAsyncTask.OnAction,
                   EndpointApi.GetListPersonByIdLstAsyncTask.OnAction,
                   FileManager.DownloadImageTask.OnAction
{
    private static final String ARG_SECTION_NUMBER = "section_number";
    private final String TAG = "MyLog_profileFragment";
    public static final int PICK_IMAGE = 111;
    FileManager.PicInfo picInfo;
    int _sectionNumber;
    View rootView;
    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public PersonProfileFragment setArgs(int sectionNumber) {
        _sectionNumber = sectionNumber;
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        this.setArguments(args);
        return this;
    }

    public PersonProfileFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_person_profile, container, false);

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        ((MainActivity) activity).onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onResume()
    {
        super.onResume();
        TextView txtView;
        ImageView imageView;
        getActivity().setTitle(R.string.personprofile_fragmentTitle);
        Person myPersonInfo = LocalDataManager.getMyPersonInfo();
        if(myPersonInfo!=null && rootView!=null)
        {
            if((imageView = (ImageView) rootView.findViewById(R.id.profile_img_photo))!=null) {
                imageView.setOnClickListener(new ImageOnClick());
                Picture photoInfo = myPersonInfo.getPhoto();
                if(photoInfo!=null)
                {
                    String photoId = UsefulFunctions.getDigest(photoInfo.getBlobKey());
                    File myFolder = FileManager.getAlbumStorageDir(TAG, getActivity().getBaseContext(), myPersonInfo.getId().toString());
                    boolean isNeedLoad=true;
                    if(myFolder !=null)
                    {
                        File myPhoto = new File(myFolder,photoId);
                        if(myPhoto.exists()) {
                            setPicToImageView(myPhoto, imageView);
                            isNeedLoad = false;
                        }
                    }
                    if(isNeedLoad)
                    {
                        Log.d(TAG,"need to load myPhoto from server");
                        String dynamicUrl = String.format("%s=s%d-c",photoInfo.getServingUrl(),(int) getResources().getDimension(R.dimen.personProfile_photoHeight));
                        Log.d(TAG,String.format("url for download image = %s",dynamicUrl));
                        new FileManager.DownloadImageTask(this,photoId).execute(dynamicUrl);
                    }
                }
            }
            if((txtView = (TextView) rootView.findViewById(R.id.profile_txt_name))!=null) {
                String name = myPersonInfo.getName(), surname = myPersonInfo.getSurname();
                txtView.setText( ((name!=null && !name.equals("")) ? name :"") + ((surname!=null && !surname.equals("")) ? " "+surname :""));
            }
            if((txtView = (TextView) rootView.findViewById(R.id.profile_txt_typeAge))!=null) {
                String str = myPersonInfo.getType().equals("PARTNER")? getString(R.string.personprofile_type_partner):getString(R.string.personprofile_type_coach);
                DateTime birthday;
                int age = UsefulFunctions.calcPersonAge(myPersonInfo.getBirthday());
                if(age>=0)
                    str += ", "+age ;
                txtView.setText(str);
            }
            if((txtView = (TextView) rootView.findViewById(R.id.profile_txt_raiting))!=null) {
                String ratings = getString(R.string.ratingInfo_ratingValLst);
                if(myPersonInfo.getRating()<1.0 && ratings!=null) {
                    String ratingArr[] = ratings.split(",");
                    if(ratingArr.length>0)
                    txtView.setText(getString(R.string.personprofile_rating) + " " + ratingArr[0]);
                }
                else
                    txtView.setText(getString(R.string.personprofile_rating) + " " + myPersonInfo.getRating());
            }

            //contacts block
            String email = myPersonInfo.getEmail(),phone = myPersonInfo.getPhone();
            LinearLayout linearLayout = (LinearLayout) rootView.findViewById(R.id.profile_contactsBlock);
            LinearLayout propertyLstLayout = (LinearLayout) rootView.findViewById(R.id.profile_linearlayout_propertyLst);
            LinearLayout propertyLayout;
            if(linearLayout!=null ) {
                linearLayout.setVisibility(View.GONE);
                if((propertyLayout = (LinearLayout) rootView.findViewById(R.id.profile_linearlayout_email))!=null && propertyLstLayout!=null)
                    if (email != null && !email.equals("")) {
                        if((txtView = (TextView) rootView.findViewById(R.id.profile_txt_emailValue))!=null)
                            txtView.setText(email);
                        propertyLayout.setVisibility(View.VISIBLE);
                        linearLayout.setVisibility(View.VISIBLE);
                    }
                    else
                        propertyLstLayout.removeView(propertyLayout);

                if((propertyLayout = (LinearLayout) rootView.findViewById(R.id.profile_linearlayout_phone))!=null && propertyLstLayout!=null)
                    if (phone != null && !phone.equals("")) {
                        if((txtView = (TextView) rootView.findViewById(R.id.profile_txt_phoneValue))!=null)
                            txtView.setText(phone);
                        propertyLayout.setVisibility(View.VISIBLE);
                        linearLayout.setVisibility(View.VISIBLE);
                    }
                    else
                        propertyLstLayout.removeView(propertyLayout);
            }

            //description block
            String desc = myPersonInfo.getDescription();
            linearLayout = (LinearLayout) rootView.findViewById(R.id.profile_descBlock);
            if(linearLayout!=null) {
                linearLayout.setVisibility(View.GONE);
                if (desc != null && !desc.equals("")) {
                    if((txtView = (TextView) rootView.findViewById(R.id.profile_txt_desc))!=null)
                        txtView.setText(desc);
                    linearLayout.setVisibility(View.VISIBLE);
                }

            }

            //favorite spots lst
            boolean isVisibleSpotLst = false;
            if((linearLayout = (LinearLayout) rootView.findViewById(R.id.profile_linearLayout_favoriteSpotLst))!=null) {
                List<Long> spotLst = myPersonInfo.getFavoriteSpotIdLst();
                if(spotLst!=null && spotLst.size()>0) {
                    Spot spot=null;
                    HashMap<Long,Spot> hshMapSpot = SpotsData.get_allSpots();
                    ArrayList<Spot> spots = new ArrayList<>();
                    for(int i=0; i<spotLst.size(); i++)
                        if((spot = hshMapSpot.get(spotLst.get(i)))!=null)
                            spots.add(spot);

                    if(spots.size()>0) {
                        /*ListView lstView = (ListView) rootView.findViewById(R.id.profile_lstView_favoriteSpots);
                        if(lstView!=null) {

                            SpotItemLstAdapter spotItemLstAdapter = new SpotItemLstAdapter(getActivity().getApplicationContext(),spots);
                            lstView.setAdapter(spotItemLstAdapter);
                            lstView.setScrollContainer(false);
                        }*/
                        SpotItemLstAdapter spotItemLstAdapter = new SpotItemLstAdapter(getActivity().getApplicationContext(),spots);
                        if( linearLayout.getChildAt(0) instanceof TextView) {
                            txtView = (TextView) linearLayout.getChildAt(0);
                            linearLayout.removeAllViews();
                            linearLayout.addView(txtView);
                            for (int i = 0; i < spotItemLstAdapter.getCount(); i++)
                                linearLayout.addView(spotItemLstAdapter.getView(i, null, null));
                            isVisibleSpotLst = true;
                        }
                    }

                }
                linearLayout.setVisibility(isVisibleSpotLst? View.VISIBLE : View.GONE);
            }
        }
    }

    private class ImageOnClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            //intent.setAction();
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent returnIntent) {
        if (resultCode != Activity.RESULT_OK) {
            Log.d(TAG, "resultCode !=  Activity.RESULT_OK");
            return;
        } else if (requestCode == PICK_IMAGE) {
            // Get the file's content URI from the incoming Intent

            Uri returnUri = returnIntent.getData();

            new EndpointApi.GetUrlForUploadAsyncTask(this).execute(returnUri.toString());

        }
    }
    @Override
    public void onGeUrlForUploadAsyncTaskFinish(Pair<List<String>, Exception> result) {
        /*
         * Try to open the file for "read" access using the
         * returned URI. If the file isn't found, write to the
         * error log and return.
         */
        String urlForUpload = result.first.get(1);
        String fileUri = result.first.get(0);
        FileManager.PicInfo picInfo = new FileManager.PicInfo(this,TAG,fileUri);
        new FileManager.UploadFileAsyncTask(this).execute(new Pair<>(picInfo, urlForUpload));
    }

    @Override
    public void onUploadFileFinish(Pair<FileManager.PicInfo,Exception> result) {
        String text;
        picInfo = result.first;
        Exception exception = result.second;
        if(exception!=null) {
            exception.printStackTrace();
            text = "File not uploaded!";
            Log.e(TAG,text );
        }
        else {
            text = "File uploaded!";
            Log.d(TAG, text);
        }

        //TODO: turn off toast message
        //Toast.makeText(getActivity().getBaseContext(),text,Toast.LENGTH_LONG).show();
        Person myPersonInfo = LocalDataManager.getMyPersonInfo();
        if(myPersonInfo!=null) {
            List list = new ArrayList();
            list.add(new Long(myPersonInfo.getId()));
            new EndpointApi.GetListPersonByIdLstAsyncTask(this).execute(list);
        }
    }

    @Override
    public void onDownloadFileFinish(Bitmap bitmap,String imgId,Exception exception) {
        if (exception==null) {
            if(bitmap==null) {
                Log.e(TAG, "bitmap not loaded from server cause: bitmap == null");
                Toast.makeText(getActivity().getBaseContext(),getString(R.string.personprofile_reqError),Toast.LENGTH_SHORT).show();
                return;
            }
            Log.d(TAG, "bitmap loaded from server");
            if(rootView!=null) {
                ImageView imageView = (ImageView) rootView.findViewById(R.id.profile_img_photo);
                imageView.setImageBitmap(bitmap);
                Person myPresonInfo = LocalDataManager.getMyPersonInfo();
                if(myPresonInfo!=null) {
                    FileManager.savePicPreviewToCache(TAG, getActivity().getBaseContext(), imgId, myPresonInfo.getId(), bitmap);
                }
            }
        }
        else {
            Log.e(TAG,"bitmap not loaded from server");
            Log.e(TAG, exception.getMessage());
            exception.printStackTrace();
            Toast.makeText(getActivity().getBaseContext(),getString(R.string.personprofile_reqError),Toast.LENGTH_SHORT).show();
            return;
        }
    }

    @Override
    public void onGetListPersonByIdLstFinish(Pair<List<Person>, Exception> result) {
        Exception exception = result.second;
        List<Person> personList = result.first;
        File cacheImage;
        if(exception==null && personList.size()>0)
        {
            Person myPersonInfoOld = LocalDataManager.getMyPersonInfo();
            if(myPersonInfoOld!=null) {
                Person myPersonInfo = personList.get(0);
                LocalDataManager.setMyPersonInfo(myPersonInfo.setPass(myPersonInfo.getPass()));
                Picture pic = myPersonInfo.getPhoto();
                if(pic!=null){
                    cacheImage = picInfo.savePicPreviewToCache(TAG,getActivity().getBaseContext(),
                            UsefulFunctions.getDigest(myPersonInfo.getPhoto().getBlobKey()), myPersonInfo.getId());
                    if(cacheImage!=null && rootView!=null) {
                        ImageView imageView = (ImageView) rootView.findViewById(R.id.profile_img_photo);
                        if(imageView!=null)
                            setPicToImageView(cacheImage, imageView);
                    }
                }
            }
        }

    }
    private void setPicToImageView(File imgFile,ImageView imgView)
    {
        int height = (int) getResources().getDimension(R.dimen.personProfile_photoHeight);
        int width = (int) getResources().getDimension(R.dimen.personProfile_photoWidth);
        InputStream in = null;
        try {
            in = new BufferedInputStream(new FileInputStream(imgFile));
            if(in!=null) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                try {
                    IOUtils.copy(in, bos);
                    in.close();

                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
                finally {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                            return;
                        }
                    }
                }
                Bitmap bitmap = BitmapFactory.decodeByteArray(bos.toByteArray(), 0, bos.toByteArray().length);
                if (bitmap != null && imgView != null) {
                    imgView.setImageBitmap(Bitmap.createScaledBitmap(bitmap, width, height, false));
                }
                if (bos != null) {
                    try {
                        bos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                        return;
                    }
                }
            }
        }
        catch (FileNotFoundException ex)
        {
            ex.printStackTrace();
        }


    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Log.d(TAG,"onCreateOptionsMenu");
        menu.clear();
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_person_profile, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_edit_profile:
                FragmentManager fragmentManager = (FragmentManager) getFragmentManager();
                if(fragmentManager!=null)
                    fragmentManager.beginTransaction().replace(R.id.container, new EditProfileFragment()).addToBackStack(null).commit();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public String getTAG() {
        return TAG;
    }
}
