package com.berezich.sportconnector.PersonProfile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.berezich.sportconnector.AlertDialogFragment;
import com.berezich.sportconnector.DatePickerFragment;
import com.berezich.sportconnector.EndpointApi;
import com.berezich.sportconnector.ErrorVisualizer;
import com.berezich.sportconnector.FileManager;
import com.berezich.sportconnector.LocalDataManager;
import com.berezich.sportconnector.MainActivity;
import com.berezich.sportconnector.R;
import com.berezich.sportconnector.UsefulFunctions;
import com.berezich.sportconnector.backend.sportConnectorApi.model.Person;
import com.berezich.sportconnector.backend.sportConnectorApi.model.Picture;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Sashka on 25.07.2015.
 */
public class EditProfileFragment extends Fragment implements DatePickerFragment.OnActionDatePickerDialogListener,
                                                             ChangePassFragment.OnActionPassDialogListener,ChangeEmailFragment.OnActionEmailDialogListener,
                                                             EndpointApi.ChangeEmailAsyncTask.OnAction,
                                                             EndpointApi.ChangePassAsyncTask.OnAction,
                                                             EndpointApi.UpdatePersonAsyncTask.OnAction,
                                                             EndpointApi.GetUrlForUploadAsyncTask.OnAction,
                                                             FileManager.UploadFileAsyncTask.OnAction/*,
                                                             EndpointApi.GetListPersonByIdLstAsyncTask.OnAction*/ {

    private final String TAG = "MyLog_EditProfileFrg";
    private final String mSex = "MALE";
    private final String fSex = "FEMALE";
    private final String tempPhotoName = "tempPhoto";
    private final String STATE_TEMP_PERSON = "tempPerson";
    private final String STATE_PICINFO = "picInfo";
    public static final int PICK_IMAGE = 111;
    FileManager.PicInfo picInfo;
    View rootView;
    Person tempMyPerson = null;
    private static GsonFactory gsonFactory = new GsonFactory();
    private static GsonBuilder gsonBuilder = new GsonBuilder();
    private static Gson gson;

    public EditProfileFragment() {
        gsonBuilder.setPrettyPrinting().serializeNulls().excludeFieldsWithoutExposeAnnotation();
        gson = gsonBuilder.create();

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
        rootView = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            //Restore the fragment's state here
            try {
                String tempPersonStr = savedInstanceState.getString(STATE_TEMP_PERSON);
                if(tempPersonStr!=null && !tempPersonStr.equals("")) {
                    tempMyPerson = gsonFactory.fromString(tempPersonStr, Person.class);
                    Log.d(TAG, String.format("tempMyPerson got out of instanceState"));
                }
                else {
                    Log.d(TAG, String.format("instanceState.tempMyPerson == null"));
                }
            } catch (Exception e) {
                Log.e(TAG, String.format("tempMyPerson getting out of instanceState failed"));
                e.printStackTrace();
            }
            try {
                String picInfoStr = savedInstanceState.getString(STATE_PICINFO);
                picInfo = gson.fromJson(picInfoStr, FileManager.PicInfo.class);
                if(picInfoStr!=null && !picInfoStr.equals("")) {
                    picInfo = gsonFactory.fromString(picInfoStr, FileManager.PicInfo.class);
                    Log.d(TAG, String.format("picInfo got out of instanceState"));
                }
                else {
                    Log.d(TAG, String.format("instanceState.picInfo == null"));
                }
            } catch (Exception e) {
                Log.e(TAG, String.format("picInfo getting out of instanceState failed"));
                e.printStackTrace();
            }

        }
        else
            Log.d(TAG, String.format("savedInstanceState == null"));
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //Save the fragment's state here
        updateTempMyPerson();
        try {
            outState.putString(STATE_TEMP_PERSON,gsonFactory.toString(tempMyPerson));
            outState.putString(STATE_PICINFO, gson.toJson(picInfo));
            Log.d(TAG, String.format("tempMyPerson and picInfo saved to instanceState"));
        } catch (Exception e) {
            Log.e(TAG, String.format("tempMyPerson or picInfo saving to instanceState failed"));
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        TextView txtView;
        EditText txtEdt;
        DateTime birthday;
        ImageButton imgBtn;
        Spinner spinner;
        ImageView imageView;
        FrameLayout frameLayout;

        final Person myPersonInfo = LocalDataManager.getMyPersonInfo();
        RadioGroup radioGroup;
        if (myPersonInfo != null && rootView != null) {
            if(tempMyPerson ==null) {
                tempMyPerson = myPersonInfo.clone();
                Log.d(TAG,"tempMyPerson cloned myPersonInfo");
            }
            if((imageView = (ImageView) rootView.findViewById(R.id.editProfile_img_photo))!=null) {
                Picture photoInfo = tempMyPerson.getPhoto();
                FileManager.providePhotoForImgView(this.getActivity().getBaseContext(), imageView,
                        photoInfo, FileManager.PERSON_CACHE_DIR + "/" + tempMyPerson.getId().toString());
            }
            if((frameLayout=(FrameLayout) rootView.findViewById(R.id.editProfile_frame_changePhoto))!=null)
                frameLayout.setOnClickListener(new ImageOnClick());
            if ((txtEdt = (EditText) rootView.findViewById(R.id.editProfile_txtEdt_name)) != null)
                txtEdt.setText(tempMyPerson.getName());
            if ((txtEdt = (EditText) rootView.findViewById(R.id.editProfile_txtEdt_surname)) != null)
                txtEdt.setText(tempMyPerson.getSurname());
            if ((txtView = (TextView) rootView.findViewById(R.id.editProfile_txtView_birthday)) != null) {
                Date date;
                if ((birthday = tempMyPerson.getBirthday()) != null)
                    date = new Date(birthday.getValue());
                else {
                    Calendar calendar = Calendar.getInstance();
                    date = calendar.getTime();
                }
                txtView.setText(String.format("%1$td.%1$tm.%1$tY", date));
                txtView.setOnClickListener(new OnBirthdayClickListener());
            }
            if ((radioGroup = (RadioGroup) rootView.findViewById(R.id.editProfile_radioGrp_sex)) != null) {
                String sex = tempMyPerson.getSex() != null ? tempMyPerson.getSex() : "";
                if (sex.equals(mSex))
                    radioGroup.check(R.id.editProfile_radio_male);
                else if (sex.equals(fSex))
                    radioGroup.check(R.id.editProfile_radio_female);
                else
                    radioGroup.clearCheck();
            }

            String email = tempMyPerson.getEmail(), phone = tempMyPerson.getPhone();
            if ((txtView = (TextView) rootView.findViewById(R.id.editProfile_txtEdt_email)) != null) {
                txtView.setText(email);
                txtView.setOnClickListener(new EmailOnClickListener());
            }
            if ((txtView = (TextView) rootView.findViewById(R.id.editProfile_txtEdt_phone)) != null)
                txtView.setText(phone);

            if ((txtView = (TextView) rootView.findViewById(R.id.editProfile_txtView_changePass)) != null) {
                txtView.setOnClickListener(new PassOnClickListener());
            }

            if ((txtView = (TextView) rootView.findViewById(R.id.editProfile_txtView_setRating)) != null) {
                txtView.setText(getString(R.string.personprofile_rating));
            }

            if ((spinner = (Spinner) rootView.findViewById(R.id.editProfile_spinner_rating)) != null) {
                String ratings = getString(R.string.ratingInfo_ratingValLst);
                if(ratings!=null) {
                    final String ratingArr[] = ratings.split(",");
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, ratingArr);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
                    spinner.setAdapter(adapter);
                    int pos = (int)(this.tempMyPerson.getRating()/0.5-2);
                    if(pos<=ratingArr.length)
                        spinner.setSelection(pos);
                    else
                        spinner.setSelection(0);

                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view,
                                                   int position, long id) {
                            float rating = Float.valueOf(ratingArr[position]);
                            if( EditProfileFragment.this.tempMyPerson.getRating()!=rating) {
                                EditProfileFragment.this.tempMyPerson.setRating(rating);
                                ImageButton imgBtn;
                                if ((imgBtn = (ImageButton) rootView.findViewById(R.id.editProfile_btn_ratingInfo)) != null) {
                                    imgBtn.performClick();
                                }
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> arg0) {
                        }
                    });
                }
            }

            if ((imgBtn = (ImageButton) rootView.findViewById(R.id.editProfile_btn_ratingInfo)) != null) {
                imgBtn.setOnClickListener(new RatingInfoOnClickListener());
            }

            if ((txtView = (TextView) rootView.findViewById(R.id.editProfile_txtEdt_desc)) != null)
                txtView.setText(tempMyPerson.getDescription());

            if(getActivity()!=null)
                ((MainActivity)getActivity()).setupUI(rootView);
        }
    }
    private Fragment getCurFragment(){
        return this;
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        menu.clear();
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_edit_profile, menu);
        ActionBar actionBar =((AppCompatActivity) getActivity()).getSupportActionBar();
        if(actionBar!=null)
            actionBar.setTitle(R.string.editprofile_fragmentTitle);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        ((MainActivity) getActivity()).hideSoftKeyboard();
        Context ctx = getActivity().getBaseContext();
        switch (item.getItemId())
        {
            case R.id.menu_save_profile:
                Person myPersonInfo = LocalDataManager.getMyPersonInfo();
                updateTempMyPerson();
                if(myPersonInfo!=null && tempMyPerson!=null) {
                    if (picInfo != null) {
                        item.setVisible(false);
                        setVisibleProgressBar(true);
                        new EndpointApi.GetUrlForUploadAsyncTask(this).execute();
                    }
                    else if (isChanged(tempMyPerson, myPersonInfo)) {
                        item.setVisible(false);
                        setVisibleProgressBar(true);
                        new EndpointApi.UpdatePersonAsyncTask(this).execute(tempMyPerson);
                    }
                    else{
                        FragmentManager fragmentManager = getFragmentManager();
                        if(fragmentManager!=null)
                            fragmentManager.popBackStack();
                    }
                }
                else
                    Toast.makeText(ctx, getString(R.string.editprofile_saveError), Toast.LENGTH_SHORT).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private class OnBirthdayClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            DatePickerFragment datePickerFragment = new DatePickerFragment();
            TextView txtView = (TextView) v;
            datePickerFragment.setArgs(txtView.getText().toString());
            datePickerFragment.setTargetFragment(getCurFragment(), -1);
            datePickerFragment.show(getFragmentManager(), null);
        }
    }
    @Override
    public void onDateSet(int year, int month, int day) {
        TextView textView;
        if(rootView!=null)
            if((textView = (TextView) rootView.findViewById(R.id.editProfile_txtView_birthday))!=null)
            {
                textView.setText(String.format("%02d",day)+ "." + String.format("%02d",month+1) + "." + year);
                textView.setTextColor( getResources().getColor(R.color.blackColor));
            }
    }

    private void updateTempMyPerson(){
        EditText txtEdt;
        TextView txtView;
        RadioGroup radioGroup;
        if((txtEdt = (EditText) rootView.findViewById(R.id.editProfile_txtEdt_name))!=null)
            tempMyPerson.setName(txtEdt.getText().toString());
        if((txtEdt = (EditText) rootView.findViewById(R.id.editProfile_txtEdt_surname))!=null)
            tempMyPerson.setSurname(txtEdt.getText().toString());
        if((txtView = (TextView) rootView.findViewById(R.id.editProfile_txtView_birthday))!=null) {
            DateTime dtBirthday = UsefulFunctions.parseDateTime(txtView.getText().toString());
            if(dtBirthday!=null)
                tempMyPerson.setBirthday(dtBirthday);
            else
                Log.e(TAG, "parsing of birthday failed");
        }
        if((radioGroup = (RadioGroup) rootView.findViewById(R.id.editProfile_radioGrp_sex))!=null)
            switch (radioGroup.getCheckedRadioButtonId())
            {
                case R.id.editProfile_radio_male:
                    tempMyPerson.setSex(mSex);
                    break;
                case R.id.editProfile_radio_female:
                    tempMyPerson.setSex(fSex);
                    break;
            }

        //middle block
        if((txtView = (TextView) rootView.findViewById(R.id.editProfile_txtEdt_email))!=null)
            tempMyPerson.setEmail(txtView.getText().toString());
        if((txtView = (TextView) rootView.findViewById(R.id.editProfile_txtEdt_phone))!=null)
            tempMyPerson.setPhone(txtView.getText().toString());

        if((txtEdt = (EditText) rootView.findViewById(R.id.editProfile_txtEdt_desc))!=null)
            tempMyPerson.setDescription(txtEdt.getText().toString());
    }

    private class EmailOnClickListener implements View.OnClickListener
    {
        @Override
        public void onClick(View v) {
            FragmentManager fragmentManager = getFragmentManager();
            Person myPersonInfo = LocalDataManager.getMyPersonInfo();
            ChangeEmailFragment changeEmailFragment = new ChangeEmailFragment().newInstance(myPersonInfo.getEmail());
            changeEmailFragment.setTargetFragment(getCurFragment(), 0);
            changeEmailFragment.show(fragmentManager, null);

        }
    }

    @Override
    public void onChangeEmailClick(String newEmail) {
        try {
            setVisibleProgressBar(true);
            new EndpointApi.ChangeEmailAsyncTask(this).execute(new Pair<Long, String>(tempMyPerson.getId(), tempMyPerson.getEmail()), new Pair<Long, String>(null, newEmail));
        }
        catch (Exception ex)
        {
            Log.e(TAG,"ChangeEmailAsyncTask.execute() exception: "+ex.getMessage());
            ex.printStackTrace();
        }
    }

    @Override
    public void onChangeEmailFinish(Exception error) {
        AlertDialogFragment dialog;
        FragmentManager ft;
        String dialogMsg;
        setVisibleProgressBar(false);
        if(error==null)
            dialogMsg = getString(R.string.changeEmail_msgChangeEmail);
        else {

            Pair<ErrorVisualizer.ERROR_CODE, String> errTxtCode = ErrorVisualizer.getTextCodeOfRespException(getActivity().getBaseContext(), error);
            if (errTxtCode != null && !errTxtCode.second.equals(""))
                dialogMsg = errTxtCode.second;
            else
                dialogMsg = getString(R.string.server_unknow_err);
            Log.d(TAG, "registrationError code = " + errTxtCode.first + " msg = " + errTxtCode.second);

        }
        dialog = AlertDialogFragment.newInstance("",dialogMsg, false,false);
        dialog.setTargetFragment(this, 0);
        ft = getFragmentManager();
        if (ft != null)
            dialog.show(getFragmentManager(), "");
    }

    private class PassOnClickListener implements View.OnClickListener
    {
        @Override
        public void onClick(View v) {
            FragmentManager fragmentManager = getFragmentManager();
            Person myPersonInfo = LocalDataManager.getMyPersonInfo();
            ChangePassFragment changePassFragment = new ChangePassFragment().newInstance(myPersonInfo.getPass());
            changePassFragment.setTargetFragment(getCurFragment(),0);
            changePassFragment.show(fragmentManager,null);

        }
    }

    @Override
    public void onChangePassClick(String newPass) {

        //tempMyPerson.setPass(newPass);
        try {
            setVisibleProgressBar(true);
            new EndpointApi.ChangePassAsyncTask(this).execute(new Pair<>(tempMyPerson.getId(), tempMyPerson.getPass()), new Pair<Long, String>(null, newPass));
            if(tempMyPerson!=null)
                tempMyPerson.setPass(newPass);
        }
        catch (Exception ex)
        {
            Log.e(TAG,"ChangePassAsyncTask.execute() exception: "+ex.getMessage());
            ex.printStackTrace();
        }
    }

    @Override
    public void onChangePassFinish(Exception error) {
        AlertDialogFragment dialog;
        FragmentManager ft;
        String dialogMsg;
        setVisibleProgressBar(false);
        if(error==null)
        {
            if(tempMyPerson!=null)
            {
                Person myPersonInfo = LocalDataManager.getMyPersonInfo();
                if(myPersonInfo!=null)
                {
                    myPersonInfo.setPass(tempMyPerson.getPass());
                    LocalDataManager.setMyPersonInfo(myPersonInfo);
                }
            }
            dialogMsg = getString(R.string.changePass_msgChangePass);
        }
        else {
            if(tempMyPerson!=null)
            {
                Person myPersonInfo = LocalDataManager.getMyPersonInfo();
                if(myPersonInfo!=null)
                    tempMyPerson.setPass(myPersonInfo.getPass());
            }
            Pair<ErrorVisualizer.ERROR_CODE, String> errTxtCode = ErrorVisualizer.getTextCodeOfRespException(getActivity().getBaseContext(), error);
            if (errTxtCode != null && !errTxtCode.second.equals(""))
                dialogMsg = errTxtCode.second;
            else
                dialogMsg = getString(R.string.server_unknow_err);
            Log.d(TAG, "registrationError code = " + errTxtCode.first + " msg = " + errTxtCode.second);

        }
        dialog = AlertDialogFragment.newInstance("",dialogMsg, false,false);
        dialog.setTargetFragment(this, 0);
        ft = getFragmentManager();
        if (ft != null)
            dialog.show(getFragmentManager(), "");
    }

    private class RatingInfoOnClickListener implements View.OnClickListener
    {
        @Override
        public void onClick(View v) {
            ImageButton imageButton = (ImageButton) v;
            imageButton.setFocusable(false);
            imageButton.setFocusableInTouchMode(false);
            if(rootView!=null) {
                Spinner spinner = (Spinner) rootView.findViewById(R.id.editProfile_spinner_rating);
                if (spinner!=null)
                    spinner.requestFocus();
            }

            FragmentManager fragmentManager = getFragmentManager();
            AlertDialogFragment dialog;
            String msgInfoId;
            if(tempMyPerson.getRating() == 1.0)
                msgInfoId = getString(R.string.ratingInfo_1_0);
            else if(tempMyPerson.getRating() == 1.5)
                msgInfoId = getString(R.string.ratingInfo_1_5);
            else if(tempMyPerson.getRating() == 2.0)
                msgInfoId = getString(R.string.ratingInfo_2_0);
            else if(tempMyPerson.getRating() == 2.5)
                msgInfoId = getString(R.string.ratingInfo_2_5);
            else if(tempMyPerson.getRating() == 3.0)
                msgInfoId = getString(R.string.ratingInfo_3_0);
            else if(tempMyPerson.getRating() == 3.5)
                msgInfoId = getString(R.string.ratingInfo_3_5);
            else if(tempMyPerson.getRating() == 4.0)
                msgInfoId = getString(R.string.ratingInfo_4_0);
            else if(tempMyPerson.getRating() == 4.5)
                msgInfoId = getString(R.string.ratingInfo_4_5);
            else if(tempMyPerson.getRating() == 5.0)
                msgInfoId = getString(R.string.ratingInfo_5_0);
            else if(tempMyPerson.getRating() == 5.5)
                msgInfoId = getString(R.string.ratingInfo_5_5);
            else if(tempMyPerson.getRating() == 6.0)
                msgInfoId = getString(R.string.ratingInfo_6_0);
            else if(tempMyPerson.getRating() == 6.5)
                msgInfoId = getString(R.string.ratingInfo_6_5);
            else if(tempMyPerson.getRating() == 7.0)
                msgInfoId = getString(R.string.ratingInfo_7_0);
            else
                msgInfoId="";

            if(!msgInfoId.equals("")) {
                dialog = AlertDialogFragment.newInstance(getString(R.string.ratingInfo_title) + " "+tempMyPerson.getRating(), msgInfoId, false, false);
                dialog.setTargetFragment(getCurFragment(), 0);
                dialog.show(fragmentManager, "");
            }
        }
    }
    private class ImageOnClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent returnIntent) {
        FileManager.PicInfo tempPicInfo = null;
        if (resultCode != Activity.RESULT_OK) {
            Log.d(TAG, "resultCode !=  Activity.RESULT_OK");
        } else if (requestCode == PICK_IMAGE) {
            Context context = getActivity().getBaseContext();
            // Get the file's content URI from the incoming Intent
            Uri returnUri = returnIntent.getData();
            try {
                tempPicInfo = new FileManager.PicInfo(this,TAG,returnUri.toString());
            } catch (IOException e) {
                Log.e(TAG, String.format("PicInfo constructor exception %s", e.getMessage()));
                e.printStackTrace();
                Toast.makeText(context, getString(R.string.editprofile_pickImageError), Toast.LENGTH_SHORT).show();
                return;
            }
            if(tempPicInfo!=null) {
                if (tempPicInfo.getPath()==null) {
                    Log.e(TAG, String.format("PICK_IMAGE returned not valid URI"));
                    Toast.makeText(context, getString(R.string.editprofile_pickImageError), Toast.LENGTH_SHORT).show();
                    return;
                }
                File pickedFile = new File(tempPicInfo.getPath());
                if (!pickedFile.exists()){
                    Log.e(TAG, String.format("PICK_IMAGE %s not exist",tempPicInfo.getPath()));
                    Toast.makeText(context, getString(R.string.editprofile_pickImageError), Toast.LENGTH_SHORT).show();
                    return;
                }

            }
            if(tempMyPerson!=null) {
                String cacheDir = FileManager.PERSON_CACHE_DIR + "/" + tempMyPerson.getId();
                tempPicInfo.savePicPreviewToCache(TAG, context, UsefulFunctions.getDigest(tempPhotoName),
                        cacheDir);
                Picture picture = new Picture();
                picture.setBlobKey(tempPhotoName);
                tempMyPerson.setPhoto(picture);
                picInfo = tempPicInfo;
                //setting picture to imageView is proceeded in onResume()
            }
        }
    }
    @Override
    public void onGetUrlForUploadAsyncTaskFinish(Pair<String, Exception> result) {
        String urlForUpload = result.first;
        Exception ex = result.second;
        String replaceBlob = "";
        Context ctx = getActivity().getBaseContext();
        if(ex!=null)
        {
            Log.e(TAG,String.format("getUrlForUpload error %s", ErrorVisualizer.getDebugMsgOfRespException(ex)));
            ex.printStackTrace();
            setVisibleProgressBar(false);
            Toast.makeText(ctx,getString(R.string.editprofile_saveError),Toast.LENGTH_SHORT).show();
            return;
        }
        if(urlForUpload==null || urlForUpload.equals(""))
        {
            Log.e(TAG, String.format("getUrlForUpload error urlForUpload not valid"));
            setVisibleProgressBar(false);
            Toast.makeText(ctx, getString(R.string.editprofile_saveError), Toast.LENGTH_SHORT).show();
            return;
        }
        Person myPersonInfo = LocalDataManager.getMyPersonInfo();
        if(myPersonInfo!=null && myPersonInfo.getPhoto()!=null)
            replaceBlob = myPersonInfo.getPhoto().getBlobKey();
        new FileManager.UploadFileAsyncTask(this).execute(new Pair<>(picInfo, new Pair<>(urlForUpload,replaceBlob)));
    }
    @Override
    public void onUploadFileFinish(Exception exception) {
        if(exception!=null) {
            exception.printStackTrace();
            Log.e(TAG, String.format("UploadFile failed: %s", exception.getMessage()));
            exception.printStackTrace();
            setVisibleProgressBar(false);
            Toast.makeText(getActivity().getBaseContext(),getString(R.string.editprofile_saveError),
                    Toast.LENGTH_SHORT).show();
        }
        else {
            Log.d(TAG, "File uploaded!");
            new EndpointApi.UpdatePersonAsyncTask(this).execute(tempMyPerson);
        }

    }
    /*@Override
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
                    cacheImage = picInfo.savePicPreviewToCache(TAG, getActivity().getBaseContext(),
                            UsefulFunctions.getDigest(myPersonInfo.getPhoto().getBlobKey()), FileManager.PERSON_CACHE_DIR + "/" + myPersonInfo.getId());
                    if(cacheImage!=null && rootView!=null) {
                        ImageView imageView = (ImageView) rootView.findViewById(R.id.editProfile_img_photo);
                        if(imageView!=null)
                        {
                            int height = (int)getResources().getDimension(R.dimen.personProfile_photoHeight);
                            int width = (int)getResources().getDimension(R.dimen.personProfile_photoWidth);
                            FileManager.setPicToImageView(cacheImage, imageView,height,width);
                        }
                    }
                }
            }
        }

    }*/
    private void setVisibleProgressBar(boolean isVisible)
    {
        View view;
        if(rootView!=null) {
            if ((view = rootView.findViewById(R.id.editProfile_scrollView))!=null)
                view.setVisibility(!isVisible ? View.VISIBLE : View.GONE);
            if ((view = rootView.findViewById(R.id.editProfile_frameLayout))!=null)
                view.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        }
        if(!isVisible)
            getActivity().invalidateOptionsMenu();
    }

    @Override
    public void onUpdatePersonFinish(Pair<Person, Exception> result) {
        Person updatedPerson = result.first;
        Exception ex = result.second;
        Person myPerson = LocalDataManager.getMyPersonInfo();
        if(ex==null && updatedPerson!=null)
        {
            if(myPerson!=null) {
                Picture newPic = updatedPerson.getPhoto();
                Picture oldPic = myPerson.getPhoto();
                if(newPic!=null &&(oldPic==null || !newPic.getBlobKey().equals(oldPic.getBlobKey()))) {
                    String tempFileName = UsefulFunctions.getDigest(tempPhotoName);
                    String newFileName = UsefulFunctions.getDigest(newPic.getBlobKey());
                    FileManager.renameFile(TAG, getActivity().getBaseContext(), FileManager.PERSON_CACHE_DIR
                            + "/" + updatedPerson.getId() + "/" + tempFileName, newFileName);
                }
                updatedPerson.setPass(myPerson.getPass());
                LocalDataManager.setMyPersonInfo(updatedPerson);
            }
            FragmentManager fragmentManager = getFragmentManager();
            if(fragmentManager!=null)
                fragmentManager.popBackStack();
        }
        else {
            Log.e(TAG, "updatePerson failed error");
            String debugMsg = ErrorVisualizer.getDebugMsgOfRespException(ex);
            if(debugMsg!=null)
                Log.e(TAG, debugMsg);

            setVisibleProgressBar(false);
            Toast.makeText(getActivity().getBaseContext(), getString(R.string.editprofile_saveError), Toast.LENGTH_LONG).show();
        }
    }

    public boolean isChanged(Person pNew, Person p)
    {
        if(!UsefulFunctions.isSameStrValue(p.getName(),pNew.getName()))
            return true;
        if(!UsefulFunctions.isSameStrValue(p.getSurname(),pNew.getSurname()))
            return true;
        if(pNew.getBirthday().getValue() != p.getBirthday().getValue())
            return true;
        if(!UsefulFunctions.isSameStrValue(p.getPhone(), pNew.getPhone()))
            return true;
        if(!UsefulFunctions.isSameStrValue(p.getPrice(),pNew.getPrice()))
            return true;
        if(pNew.getRating() != p.getRating())
            return true;
        if(!UsefulFunctions.isSameStrValue(p.getDescription(), pNew.getDescription()))
            return true;
        if(!UsefulFunctions.isSameStrValue(p.getSex(),pNew.getSex()))
            return true;
        return false;
    }
}
