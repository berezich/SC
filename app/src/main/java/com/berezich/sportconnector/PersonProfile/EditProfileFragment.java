package com.berezich.sportconnector.PersonProfile;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.LabeledIntent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.InputFilter;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.berezich.sportconnector.AlertDialogFragment;
import com.berezich.sportconnector.DatePickerFragment;
import com.berezich.sportconnector.EndpointApi.EndpointApi;
import com.berezich.sportconnector.ErrorVisualizer;
import com.berezich.sportconnector.FileManager;
import com.berezich.sportconnector.LocalDataManager;
import com.berezich.sportconnector.MainActivity;
import com.berezich.sportconnector.PhoneMaskUtil;
import com.berezich.sportconnector.R;
import com.berezich.sportconnector.UsefulFunctions;
import com.berezich.sportconnector.backend.sportConnectorApi.model.Person;
import com.berezich.sportconnector.backend.sportConnectorApi.model.Picture;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class EditProfileFragment extends Fragment implements DatePickerFragment.OnActionDatePickerDialogListener,
                                                             ChangePassFragment.OnActionPassDialogListener,ChangeEmailFragment.OnActionEmailDialogListener,
                                                             EndpointApi.ChangeEmailAsyncTask.OnAction,
                                                             EndpointApi.ChangePassAsyncTask.OnAction,
                                                             EndpointApi.UpdatePersonAsyncTask.OnAction,
                                                             EndpointApi.GetUrlForUploadAsyncTask.OnAction,
                                                             FileManager.UploadAndReplacePersonFileAsyncTask.OnAction {

    private final String TAG = "MyLog_EditProfileFrg";
    private final String mSex = "MALE";
    private final String fSex = "FEMALE";
    private String tempPhotoNameLocal = "tempPhoto";
    private String photoAvatarNameOnServer = "avatar_";
    private final String STATE_TEMP_PERSON = "tempPerson";
    private final String STATE_PICINFO = "picInfo";
    private final String STATE_TEMP_FILE_PATH = "tempFilePath";
    private String tempFileForPickedImage;
    public final int PICK_IMAGE = 111;
    public final int CAMERA_IMAGE = 112;
    public final int GALLERY_CAMERA_IMAGE = 113;
    FileManager.PicInfo picInfo;
    View rootView;
    Person tempMyPerson = null;
    String newEmail ="";
    private static GsonFactory gsonFactory = new GsonFactory();
    private static GsonBuilder gsonBuilder = new GsonBuilder();
    private static Gson gson;
    private FragmentActivity activity;
    private PersonProfileFragment personProfileFragment;

    public EditProfileFragment() {
        gsonBuilder.serializeNulls().excludeFieldsWithoutExposeAnnotation();
        gson = gsonBuilder.create();

    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        try {
            super.onCreate(savedInstanceState);
            setHasOptionsMenu(true);
            LocalDataManager.init(getActivity());
            try {
                personProfileFragment = (PersonProfileFragment) getTargetFragment();
            }
            catch (Exception ex){
                Log.e(TAG,"For EditProfileFragment there is no targetFragment PersonProfileFragment!");
                ex.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        try {
            rootView = inflater.inflate(R.layout.fragment_edit_profile, container, false);
            EditText txtEdt;
            if ((txtEdt = (EditText) rootView.findViewById(R.id.editProfile_txtEdt_phone)) != null)
                txtEdt.addTextChangedListener( PhoneMaskUtil.insert(txtEdt));
            return rootView;
        } catch (Exception e) {
            e.printStackTrace();
            return rootView=null;
        }
    }

    @Override
    public void onAttach(Activity activity) {
        try {
            this.activity = getActivity();
            super.onAttach(activity);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        try {
            super.onActivityCreated(savedInstanceState);
            if (savedInstanceState != null) {
                //Restore the fragment's state here
                try {
                    String tempPersonStr = savedInstanceState.getString(STATE_TEMP_PERSON);
                    if(tempPersonStr!=null && !tempPersonStr.equals("")) {
                        tempMyPerson = gsonFactory.fromString(tempPersonStr, Person.class);
                        Log.d(TAG, "tempMyPerson got out of instanceState");
                    }
                    else {
                        Log.d(TAG, "instanceState.tempMyPerson == null");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "tempMyPerson getting out of instanceState failed");
                    e.printStackTrace();
                }
                try {
                    String picInfoStr = savedInstanceState.getString(STATE_PICINFO);
                    if(picInfoStr!=null && !picInfoStr.equals("")) {
                        picInfo = gson.fromJson(picInfoStr, FileManager.PicInfo.class);
                        Log.d(TAG, "picInfo got out of instanceState");
                    }
                    else {
                        Log.d(TAG, "instanceState.picInfo == null");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "picInfo getting out of instanceState failed");
                    e.printStackTrace();
                }
                tempFileForPickedImage = savedInstanceState.getString(STATE_TEMP_FILE_PATH);
            }
            else
                Log.d(TAG, "savedInstanceState == null");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        try {
            super.onSaveInstanceState(outState);
            //Save the fragment's state here
            updateTempMyPerson();
            try {
                outState.putString(STATE_TEMP_PERSON,gsonFactory.toString(tempMyPerson));
                outState.putString(STATE_PICINFO, gson.toJson(picInfo));
                outState.putString(STATE_TEMP_FILE_PATH, tempFileForPickedImage);
                Log.d(TAG, "tempMyPerson and picInfo saved to instanceState");
            } catch (Exception e) {
                Log.e(TAG, "tempMyPerson or picInfo saving to instanceState failed");
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        try {
            super.onResume();
            TextView txtView;
            EditText txtEdt;
            DateTime birthday;
            ImageButton imgBtn;
            Spinner spinner;
            ImageView imageView;

            ((MainActivity)this.activity).setmTitle(activity.getString(R.string.editprofile_fragmentTitle));
            ((MainActivity)this.activity).getSupportActionBar().setHomeAsUpIndicator(null);
            ((MainActivity)this.activity).restoreActionBar();

            final Person myPersonInfo = LocalDataManager.getMyPersonInfo();
            RadioGroup radioGroup;
            if (myPersonInfo != null && rootView != null) {
                if(tempMyPerson ==null) {
                    tempMyPerson = myPersonInfo.clone();
                    Log.d(TAG,"tempMyPerson cloned myPersonInfo");
                }else{

                    if(tempMyPerson.getPhoto()!=null)
                        Log.d(TAG,String.format("temp userPic = %s",UsefulFunctions.getDigest(tempMyPerson.getPhoto().getBlobKey())));
                }
                if((imageView = (ImageView) rootView.findViewById(R.id.editProfile_img_photo))!=null) {
                    Picture photoInfo = tempMyPerson.getPhoto();
                    FileManager.providePhotoForImgView(activity.getBaseContext(), imageView,
                            photoInfo, FileManager.PERSON_CACHE_DIR + "/" + tempMyPerson.getId().toString());
                }
                LinearLayout linearLayout;
                if((linearLayout=(LinearLayout) rootView.findViewById(R.id.editProfile_hLayout_changePhoto))!=null)
                    linearLayout.setOnClickListener(new ImageOnClick());
                if ((linearLayout = (LinearLayout) rootView.findViewById(R.id.editProfile_hLayout_rotateImgL)) != null)
                    linearLayout.setOnClickListener(new RotateImageOnClick());
                if ((linearLayout = (LinearLayout) rootView.findViewById(R.id.editProfile_hLayout_rotateImgR)) != null)
                    linearLayout.setOnClickListener(new RotateImageOnClick());
                if(picInfo!=null) {
                    if ((linearLayout = (LinearLayout) rootView.findViewById(R.id.editProfile_vLayout_rotateImgBlock)) != null)
                        linearLayout.setVisibility(View.VISIBLE);
                }
                else if ((linearLayout = (LinearLayout) rootView.findViewById(R.id.editProfile_vLayout_rotateImgBlock)) != null)
                        linearLayout.setVisibility(View.GONE);

                if ((txtEdt = (EditText) rootView.findViewById(R.id.editProfile_txtEdt_name)) != null) {
                    txtEdt.setText(tempMyPerson.getName());
                    txtEdt.setFilters(new InputFilter[]{new UsefulFunctions.NameSurnameInputFilter(
                            activity.getResources().getInteger(R.integer.nameMaxLength_edtTxt))});
                }
                if ((txtEdt = (EditText) rootView.findViewById(R.id.editProfile_txtEdt_surname)) != null) {
                    txtEdt.setText(tempMyPerson.getSurname());
                    txtEdt.setFilters(new InputFilter[]{new UsefulFunctions.NameSurnameInputFilter(
                            activity.getResources().getInteger(R.integer.surnameMaxLength_edtTxt))});
                }
                if ((txtView = (TextView) rootView.findViewById(R.id.editProfile_txtView_birthday)) != null) {
                    Date date;
                    if ((birthday = tempMyPerson.getBirthday()) != null) {
                        date = new Date(birthday.getValue());
                        txtView.setText(new SimpleDateFormat("dd.MM.yyyy").format(date));
                    }
                    else {
                        txtView.setText("");
                    }
                    txtView.setOnClickListener(new OnBirthdayClickListener());
                }
                if ((radioGroup = (RadioGroup) rootView.findViewById(R.id.editProfile_radioGrp_sex)) != null) {
                    String sex = tempMyPerson.getSex() != null ? tempMyPerson.getSex() : "";
                    switch (sex){
                        case mSex:
                            radioGroup.check(R.id.editProfile_radio_male);
                            break;
                        case fSex:
                            radioGroup.check(R.id.editProfile_radio_female);
                            break;
                        default:
                            radioGroup.clearCheck();
                    }
                }

                String email = tempMyPerson.getEmail(), phone = tempMyPerson.getPhone();
                if ((txtView = (TextView) rootView.findViewById(R.id.editProfile_txtEdt_email)) != null) {
                    txtView.setText(email);
                    txtView.setOnClickListener(new EmailOnClickListener());
                }
                if ((txtEdt = (EditText) rootView.findViewById(R.id.editProfile_txtEdt_phone)) != null) {
                    txtEdt.setText(phone);
                }

                if ((txtView = (TextView) rootView.findViewById(R.id.editProfile_txtView_changePass)) != null) {
                    txtView.setOnClickListener(new PassOnClickListener());
                }

                if ((txtView = (TextView) rootView.findViewById(R.id.editProfile_txtView_setRating)) != null) {
                    txtView.setText(activity.getString(R.string.personprofile_rating));
                }

                if ((spinner = (Spinner) rootView.findViewById(R.id.editProfile_spinner_rating)) != null) {
                    String ratings = activity.getString(R.string.ratingInfo_ratingValLst);
                    if(ratings!=null) {
                        final String ratingArr[] = ratings.split(",");
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(activity, R.layout.spinner_item, ratingArr);
                        adapter.setDropDownViewResource(R.layout.spinner_item);
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
                                try {
                                    float rating = Float.valueOf(ratingArr[position]);
                                    if( EditProfileFragment.this.tempMyPerson.getRating()!=rating) {
                                        EditProfileFragment.this.tempMyPerson.setRating(rating);
                                        ImageButton imgBtn;
                                        if ((imgBtn = (ImageButton) rootView.findViewById(R.id.editProfile_btn_ratingInfo)) != null) {
                                            imgBtn.performClick();
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
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

                if(activity!=null)
                    ((MainActivity)activity).setupUI(rootView);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private Fragment getCurFragment(){
        return this;
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        try {
            menu.clear();
            super.onCreateOptionsMenu(menu, inflater);
            inflater.inflate(R.menu.fragment_edit_profile, menu);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        try {
            ((MainActivity) activity).hideSoftKeyboard();
            Context ctx = activity.getBaseContext();
            switch (item.getItemId())
            {
                case R.id.menu_save_profile:
                    Person myPersonInfo = LocalDataManager.getMyPersonInfo();
                    String validationErr = updateTempMyPerson();
                    if(!validationErr.isEmpty()) {
                        showWarnDialog(validationErr);
                        break;
                    }

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
                            FragmentManager fragmentManager = activity.getSupportFragmentManager();
                            if(fragmentManager!=null)
                                fragmentManager.popBackStack();
                        }
                    }
                    else
                        Toast.makeText(ctx, activity.getString(R.string.editprofile_saveError), Toast.LENGTH_SHORT).show();
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.onOptionsItemSelected(item);
    }

    private class OnBirthdayClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            try {
                DatePickerFragment datePickerFragment = new DatePickerFragment();
                TextView txtView = (TextView) v;

                datePickerFragment.setArgs(txtView.getText().toString());
                datePickerFragment.setTargetFragment(getCurFragment(), -1);
                datePickerFragment.show(activity.getSupportFragmentManager(), null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    @Override
    public void onDateSet(int year, int month, int day) {
        try {
            TextView textView;
            if(rootView!=null)
                if((textView = (TextView) rootView.findViewById(R.id.editProfile_txtView_birthday))!=null)
                {
                    textView.setText(String.format("%02d",day)+ "." + String.format("%02d",month+1) + "." + year);
                    textView.setTextColor( activity.getResources().getColor(R.color.blackColor));
                }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String updateTempMyPerson(){
        EditText txtEdt;
        TextView txtView;
        String validationError = "";
        if((txtEdt = (EditText) rootView.findViewById(R.id.editProfile_txtEdt_name))!=null){
            String name = txtEdt.getText().toString().trim();
            if(validationError.isEmpty() && name.isEmpty())
                validationError = activity.getString(R.string.registration_err_nameNull);
            tempMyPerson.setName(name);
        }
        if((txtEdt = (EditText) rootView.findViewById(R.id.editProfile_txtEdt_surname))!=null) {
            String surname = txtEdt.getText().toString().trim();
            tempMyPerson.setSurname(surname);
        }
        if((txtView = (TextView) rootView.findViewById(R.id.editProfile_txtView_birthday))!=null) {
            DateTime dtBirthday = UsefulFunctions.parseDateTime(txtView.getText().toString());
            if(dtBirthday!=null)
                tempMyPerson.setBirthday(dtBirthday);
            else
                Log.e(TAG, "parsing of birthday failed");
        }
        RadioGroup radioGroup;
        if((radioGroup = (RadioGroup) rootView.findViewById(R.id.editProfile_radioGrp_sex))!=null)
            switch (radioGroup.getCheckedRadioButtonId())
            {
                case R.id.editProfile_radio_male:
                    tempMyPerson.setSex(mSex);
                    break;
                case R.id.editProfile_radio_female:
                    tempMyPerson.setSex(fSex);
                    break;
                /*default:
                    if(validationError.isEmpty()){
                        validationError = activity.getString(R.string.editprofile_invalidSex);
                    }*/
            }

        if((txtView = (TextView) rootView.findViewById(R.id.editProfile_txtEdt_phone))!=null) {
            String phone = txtView.getText().toString();
            if(validationError.isEmpty() && !PhoneMaskUtil.validate(phone))
                validationError = activity.getString(R.string.editprofile_invalidPhone);
            tempMyPerson.setPhone(txtView.getText().toString());
        }

        if((txtEdt = (EditText) rootView.findViewById(R.id.editProfile_txtEdt_desc))!=null)
            tempMyPerson.setDescription(txtEdt.getText().toString());
        return validationError;
    }

    private class EmailOnClickListener implements View.OnClickListener
    {
        @Override
        public void onClick(View v) {
            try {
                FragmentManager fragmentManager = activity.getSupportFragmentManager();
                Person myPersonInfo = LocalDataManager.getMyPersonInfo();
                if(myPersonInfo!=null) {
                    ChangeEmailFragment changeEmailFragment = ChangeEmailFragment.newInstance(myPersonInfo.getEmail());
                    changeEmailFragment.setTargetFragment(getCurFragment(), 0);
                    changeEmailFragment.show(fragmentManager, null);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public void onChangeEmailClick(String newEmail) {
        try {
            this.newEmail = newEmail;
            setVisibleProgressBar(true);
            new EndpointApi.ChangeEmailAsyncTask(this).execute(new Pair<>(
                    new Pair<>(tempMyPerson.getId(),tempMyPerson.getPass())
                    ,new Pair<>(tempMyPerson.getEmail(),newEmail)));

        }
        catch (Exception ex)
        {
            Log.e(TAG,"ChangeEmailAsyncTask.execute() exception: "+ex.getMessage());
            ex.printStackTrace();
        }
    }

    @Override
    public void onChangeEmailFinish(Exception error) {
        try {
            String dialogMsg;
            setVisibleProgressBar(false);
            if(error==null)
                dialogMsg = String.format(activity.getString(R.string.changeEmail_msgChangeEmail)+
                        " "+activity.getString(R.string.spam_warning_msg),newEmail);
            else {

                Pair<ErrorVisualizer.ERROR_CODE, String> errTxtCode =
                        ErrorVisualizer.getTextCodeOfRespException(activity.getBaseContext(), error);
                if (errTxtCode != null && !errTxtCode.second.equals(""))
                    dialogMsg = errTxtCode.second;
                else
                    dialogMsg = activity.getString(R.string.server_unknown_err);
                if(errTxtCode!=null)
                    Log.d(TAG, "registrationError code = " + errTxtCode.first + " msg = " + errTxtCode.second);

            }
            showWarnDialog(dialogMsg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class PassOnClickListener implements View.OnClickListener
    {
        @Override
        public void onClick(View v) {
            try {
                FragmentManager fragmentManager = activity.getSupportFragmentManager();
                Person myPersonInfo = LocalDataManager.getMyPersonInfo();
                if(myPersonInfo!=null) {
                    ChangePassFragment changePassFragment = ChangePassFragment.newInstance(myPersonInfo.getPass());
                    changePassFragment.setTargetFragment(getCurFragment(), 0);
                    changePassFragment.show(fragmentManager, null);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onChangePassClick(String newPass) {

        try {
            setVisibleProgressBar(true);
            new EndpointApi.ChangePassAsyncTask(this).execute(new Pair<>(
                    tempMyPerson.getId(), tempMyPerson.getPass()),
                    new Pair<Long, String>(null, newPass));
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
        try {
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
                dialogMsg = activity.getString(R.string.changePass_msgChangePass);
            }
            else {
                if(tempMyPerson!=null)
                {
                    Person myPersonInfo = LocalDataManager.getMyPersonInfo();
                    if(myPersonInfo!=null)
                        tempMyPerson.setPass(myPersonInfo.getPass());
                }
                Pair<ErrorVisualizer.ERROR_CODE, String> errTxtCode =
                        ErrorVisualizer.getTextCodeOfRespException(activity.getBaseContext(), error);
                if (errTxtCode != null && !errTxtCode.second.equals(""))
                    dialogMsg = errTxtCode.second;
                else
                    dialogMsg = activity.getString(R.string.server_unknown_err);
                if(errTxtCode!=null)
                    Log.d(TAG, "registrationError code = " + errTxtCode.first + " msg = " + errTxtCode.second);

            }
            showWarnDialog(dialogMsg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class RatingInfoOnClickListener implements View.OnClickListener
    {
        @Override
        public void onClick(View v) {
            try {
                ImageButton imageButton = (ImageButton) v;
                imageButton.setFocusable(false);
                imageButton.setFocusableInTouchMode(false);
                if(rootView!=null) {
                    Spinner spinner = (Spinner) rootView.findViewById(R.id.editProfile_spinner_rating);
                    if (spinner!=null)
                        spinner.requestFocus();
                }

                String msgInfoId;
                if(tempMyPerson.getRating() == 1.0)
                    msgInfoId = activity.getString(R.string.ratingInfo_1_0);
                else if(tempMyPerson.getRating() == 1.5)
                    msgInfoId = activity.getString(R.string.ratingInfo_1_5);
                else if(tempMyPerson.getRating() == 2.0)
                    msgInfoId = activity.getString(R.string.ratingInfo_2_0);
                else if(tempMyPerson.getRating() == 2.5)
                    msgInfoId = activity.getString(R.string.ratingInfo_2_5);
                else if(tempMyPerson.getRating() == 3.0)
                    msgInfoId = activity.getString(R.string.ratingInfo_3_0);
                else if(tempMyPerson.getRating() == 3.5)
                    msgInfoId = activity.getString(R.string.ratingInfo_3_5);
                else if(tempMyPerson.getRating() == 4.0)
                    msgInfoId = activity.getString(R.string.ratingInfo_4_0);
                else if(tempMyPerson.getRating() == 4.5)
                    msgInfoId = activity.getString(R.string.ratingInfo_4_5);
                else if(tempMyPerson.getRating() == 5.0)
                    msgInfoId = activity.getString(R.string.ratingInfo_5_0);
                else if(tempMyPerson.getRating() == 5.5)
                    msgInfoId = activity.getString(R.string.ratingInfo_5_5);
                else if(tempMyPerson.getRating() == 6.0)
                    msgInfoId = activity.getString(R.string.ratingInfo_6_0);
                else if(tempMyPerson.getRating() == 6.5)
                    msgInfoId = activity.getString(R.string.ratingInfo_6_5);
                else if(tempMyPerson.getRating() == 7.0)
                    msgInfoId = activity.getString(R.string.ratingInfo_7_0);
                else
                    msgInfoId="";

                if(!msgInfoId.equals("")) {
                    showWarnDialog(activity.getString(R.string.ratingInfo_title) + " " + tempMyPerson.getRating(), msgInfoId);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    private class ImageOnClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            try {

                int validIntentFlag = 0;
                v.setEnabled(false);

                PackageManager pm = activity.getPackageManager();
                Intent intentGallery = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intentGallery.setType("image/*");

                Intent takePictureIntent = null;
                File tempFileForPickedImage = FileManager.createTempFile(TAG,activity.getBaseContext(),
                        photoAvatarNameOnServer+tempMyPerson.getId().toString());
                if(pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)&&tempFileForPickedImage!=null) {
                    EditProfileFragment.this.tempFileForPickedImage = tempFileForPickedImage.getAbsolutePath();
                    takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                }

                if( intentGallery.resolveActivity(pm)!=null)
                    validIntentFlag |=  0x01;
                if(takePictureIntent!=null && takePictureIntent.resolveActivity(pm)!=null)
                    validIntentFlag |=  0x10;

                switch (validIntentFlag){
                    case 0x01:
                        startActivityForResult(intentGallery, PICK_IMAGE);
                        return;
                    case 0x10:
                        startActivityForResult(takePictureIntent, CAMERA_IMAGE);
                        return;
                    case 0x00:
                        return;
                }

                Intent openInChooser = Intent.createChooser(intentGallery,
                        activity.getString(R.string.personprofile_intentChooserTitle));
                // Append " (for editing)" to applicable apps, otherwise they will show up twice identically
                Spannable mark = new SpannableString(" "
                        +activity.getString(R.string.editprofile_chooserItemPhotoMark));
                if(mark.toString().trim().isEmpty())
                    mark = new SpannableString("");
                else
                    mark.setSpan(new ForegroundColorSpan(Color.CYAN), 0,
                            mark.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                List<ResolveInfo> resInfo = pm.queryIntentActivities(takePictureIntent,
                        PackageManager.MATCH_DEFAULT_ONLY);
                Intent[] extraIntents = new Intent[resInfo.size()];
                for (int i = 0; i < resInfo.size(); i++) {
                    // Extract the label, append it, and repackage it in a LabeledIntent
                    ResolveInfo ri = resInfo.get(i);
                    String packageName = ri.activityInfo.packageName;
                    Intent intent = new Intent();
                    intent.setComponent(new ComponentName(packageName, ri.activityInfo.name));
                    intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(tempFileForPickedImage));

                    CharSequence label = TextUtils.concat(ri.loadLabel(pm), mark);
                    extraIntents[i] = new LabeledIntent(intent, packageName, label, ri.icon);
                }

                openInChooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, extraIntents);
                startActivityForResult(openInChooser, GALLERY_CAMERA_IMAGE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class RotateImageOnClick implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            int rotation;
            if(v.getId()==R.id.editProfile_hLayout_rotateImgL)
                rotation=-90;
            else
                rotation=90;
            //final View progressBar = rootView.findViewById(R.id.editProfile_imgProgressBar);
            final View progressBar = rootView.findViewById(R.id.editProfile_frameImgProgressBar);
            if(progressBar!=null)
                progressBar.setVisibility(View.VISIBLE);
            final String cacheDir = FileManager.PERSON_CACHE_DIR + "/" + tempMyPerson.getId();

            new AsyncTask<Integer, Void, Void>(){
                    @Override
                    protected Void doInBackground(Integer... params) {
                        try {
                            int rotation = params[0];
                            picInfo.rotateImg(rotation);

                            picInfo.savePicPreviewToCache(TAG, activity, UsefulFunctions.getDigest(tempPhotoNameLocal), cacheDir);
                            Picture picture = new Picture();
                            picture.setBlobKey(tempPhotoNameLocal);
                            tempMyPerson.setPhoto(picture);


                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                    @Override
                    protected void onPostExecute(Void aVoid) {
                        ImageView imageView;
                        if((imageView = (ImageView) rootView.findViewById(R.id.editProfile_img_photo))!=null) {
                            File file = FileManager.getAlbumStorageDir(TAG, activity, cacheDir);
                            File imgFile = new File(file,UsefulFunctions.getDigest(tempPhotoNameLocal));
                            FileManager.setPicToImageView(imgFile, imageView,
                                    (int) activity.getResources().getDimension(R.dimen.personProfile_photoWidth),
                                    (int) activity.getResources().getDimension(R.dimen.personProfile_photoHeight));
                        }
                        if(progressBar!=null)
                            progressBar.setVisibility(View.GONE);
                    }
                }.execute(rotation);

        }
    }
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent returnIntent) {
        try {
            LinearLayout linearLayout=(LinearLayout) rootView.findViewById(R.id.editProfile_hLayout_changePhoto);
            linearLayout.setEnabled(true);
            if (resultCode != Activity.RESULT_OK) {
                Log.d(TAG, "resultCode !=  Activity.RESULT_OK");
            } else {
                switch (requestCode) {
                    case GALLERY_CAMERA_IMAGE:
                    case PICK_IMAGE:
                    case CAMERA_IMAGE:
                        final Context context = activity.getBaseContext();
                        new AsyncTask<Intent,Void,Void>(){
                            @Override
                            protected Void doInBackground(Intent... params) {
                                try {
                                    FileManager.PicInfo tempPicInfo = null;
                                    // Get the file's content URI from the incoming Intent
                                    Intent returnIntent = params[0];
                                    if (returnIntent == null) {
                                        Log.e(TAG, "RETURN Intent == null => photo was saved to tempFile");
                                        try {
                                            File tempPhotoFile = new File(tempFileForPickedImage);
                                            tempPicInfo = new FileManager.PicInfo(tempPhotoFile);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    } else {
                                        Uri returnUri = returnIntent.getData();
                                        String url = returnUri.toString();
                                        Bitmap bitmap1 = null;
                                        InputStream is = null;
                                        if (url.startsWith("content://com.google.android.apps.photos.content")) {
                                            try {
                                                try {
                                                    is = activity.getContentResolver().openInputStream(Uri.parse(url));
                                                    bitmap1 = BitmapFactory.decodeStream(is);

                                                } catch (Exception ex) {
                                                    ex.printStackTrace();
                                                } finally {
                                                    if (is != null)
                                                        is.close();
                                                }
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                            try {
                                                if (bitmap1 != null) {
                                                    File tempPhotoFile = new File(tempFileForPickedImage);

                                                    FileOutputStream out = null;
                                                    try {
                                                        out = new FileOutputStream(tempPhotoFile);
                                                        bitmap1.compress(Bitmap.CompressFormat.JPEG, FileManager.COMPRESS_QUALITY_HIGHEST, out);
                                                        bitmap1.recycle();
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    } finally {
                                                        try {
                                                            if (out != null) {
                                                                out.close();
                                                            }
                                                        } catch (IOException e) {
                                                            e.printStackTrace();
                                                        }
                                                    }

                                                    tempPicInfo = new FileManager.PicInfo(tempPhotoFile);
                                                }
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        } else {//image from local gallery
                                            try {
                                                //copy image to tempFileForPickedImage
                                                Cursor returnCursor = activity.getContentResolver().query(returnUri, null, null, null, null);
                                                int dataIdx = returnCursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                                                returnCursor.moveToFirst();
                                                String filePath = returnCursor.getString(dataIdx);
                                                returnCursor.close();
                                                File photoFile = new File(filePath);
                                                File tempPhotoFile = new File(tempFileForPickedImage);
                                                if(FileManager.copy(photoFile,tempPhotoFile))
                                                    tempPicInfo = new FileManager.PicInfo(tempPhotoFile);
                                                /*tempPicInfo = new FileManager.PicInfo(
                                                        EditProfileFragment.this, returnUri.toString(),
                                                        photoAvatarNameOnServer + tempMyPerson.getId().toString());*/
                                            } catch (Exception e) {
                                                Log.e(TAG, String.format("PicInfo constructor exception %s", e.getMessage()));
                                                e.printStackTrace();
                                                Toast.makeText(context, activity.getString(R.string.editprofile_pickImageError),
                                                        Toast.LENGTH_SHORT).show();
                                                return null;
                                            }

                                            if (tempPicInfo.getPath() == null) {
                                                Log.e(TAG, "PICK_IMAGE returned not valid URI");
                                                Toast.makeText(context, activity.getString(R.string.editprofile_pickImageError),
                                                        Toast.LENGTH_SHORT).show();
                                                return null;
                                            }
                                            File pickedFile = new File(tempPicInfo.getPath());
                                            if (!pickedFile.exists()) {
                                                Log.e(TAG, String.format("PICK_IMAGE %s not exist", tempPicInfo.getPath()));
                                                Toast.makeText(context, activity.getString(R.string.editprofile_pickImageError),
                                                        Toast.LENGTH_SHORT).show();
                                                return null;
                                            }
                                        }
                                    }
                                    if (tempMyPerson != null) {
                                        String cacheDir = FileManager.PERSON_CACHE_DIR + "/" + tempMyPerson.getId();
                                        if (tempPicInfo != null)
                                            tempPicInfo.savePicPreviewToCache(TAG, context, UsefulFunctions.getDigest(tempPhotoNameLocal),
                                                    cacheDir);
                                        Picture picture = new Picture();
                                        picture.setBlobKey(tempPhotoNameLocal);
                                        tempMyPerson.setPhoto(picture);
                                        picInfo = tempPicInfo;

                                    }
                                    return null;
                                }
                                catch (Exception ex){
                                    ex.printStackTrace();
                                    return null;
                                }
                            }

                            @Override
                            protected void onPostExecute(Void aVoid) {
                                try {
                                    ImageView imageView;
                                    if((imageView = (ImageView) rootView.findViewById(R.id.editProfile_img_photo))!=null) {
                                        Picture photoInfo = tempMyPerson.getPhoto();
                                        FileManager.providePhotoForImgView(activity.getBaseContext(), imageView,
                                                photoInfo, FileManager.PERSON_CACHE_DIR + "/" + tempMyPerson.getId().toString());
                                    }
                                    LinearLayout linearLayout;
                                    if(picInfo!=null) {
                                        if ((linearLayout = (LinearLayout) rootView.findViewById(R.id.editProfile_vLayout_rotateImgBlock)) != null)
                                            linearLayout.setVisibility(View.VISIBLE);
                                    }
                                    else if ((linearLayout = (LinearLayout) rootView.findViewById(R.id.editProfile_vLayout_rotateImgBlock)) != null)
                                        linearLayout.setVisibility(View.GONE);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }.execute(returnIntent);

                        break;
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    @Override
    public void onGetUrlForUploadAsyncTaskFinish(Pair<String, Exception> result) {
        try {
            String urlForUpload = result.first;
            Exception ex = result.second;
            String replaceBlob = "";
            Context ctx = activity.getBaseContext();
            if(ex!=null)
            {
                Log.e(TAG,String.format("getUrlForUpload error %s", ErrorVisualizer.getDebugMsgOfRespException(ex)));
                ex.printStackTrace();
                setVisibleProgressBar(false);
                Toast.makeText(ctx,activity.getString(R.string.editprofile_saveError),Toast.LENGTH_SHORT).show();
                return;
            }
            if(urlForUpload==null || urlForUpload.equals(""))
            {
                Log.e(TAG, "getUrlForUpload error urlForUpload not valid");
                setVisibleProgressBar(false);
                Toast.makeText(ctx, activity.getString(R.string.editprofile_saveError), Toast.LENGTH_SHORT).show();
                return;
            }
            Log.d(TAG, String.format("url for upload file = %s",urlForUpload));
            Person myPersonInfo = LocalDataManager.getMyPersonInfo();
            if(myPersonInfo!=null && myPersonInfo.getPhoto()!=null)
                replaceBlob = myPersonInfo.getPhoto().getBlobKey();
            new FileManager.UploadAndReplacePersonFileAsyncTask(this).execute(
                    new Pair<>(picInfo, new Pair<>(urlForUpload,replaceBlob)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onUploadFileFinish(Exception exception) {
        try {
            if(exception!=null) {
                exception.printStackTrace();
                Log.e(TAG, String.format("UploadFile failed: %s", exception.getMessage()));
                exception.printStackTrace();
                setVisibleProgressBar(false);
                Toast.makeText(activity.getBaseContext(),activity.getString(R.string.editprofile_saveError),
                        Toast.LENGTH_SHORT).show();
            }
            else {
                Log.d(TAG, "File uploaded!");
                new EndpointApi.UpdatePersonAsyncTask(this).execute(tempMyPerson);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private void showWarnDialog(String dialogMsg){
        showWarnDialog("", dialogMsg);
    }
    private void showWarnDialog(String title, String dialogMsg){
        AlertDialogFragment dialog = AlertDialogFragment.newInstance(title, dialogMsg, false, false);
        dialog.setTargetFragment(this, 0);
        FragmentManager ft = activity.getSupportFragmentManager();
        if (ft != null)
            dialog.show(ft, "");
    }

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
            activity.invalidateOptionsMenu();
    }

    @Override
    public void onUpdatePersonFinish(Pair<Person, Exception> result) {
        try {
            Person updatedPerson = result.first;
            Exception ex = result.second;
            Person myPerson = LocalDataManager.getMyPersonInfo();
            if(ex==null && updatedPerson!=null)
            {
                if(myPerson!=null) {
                    Picture newPic = updatedPerson.getPhoto();
                    Picture oldPic = myPerson.getPhoto();
                    if(newPic!=null &&(oldPic==null || !newPic.getBlobKey().equals(oldPic.getBlobKey()))) {
                        String tempFileName = UsefulFunctions.getDigest(tempPhotoNameLocal);
                        String newFileName = UsefulFunctions.getDigest(newPic.getBlobKey());
                        FileManager.renameFile(TAG, activity.getBaseContext(), FileManager.PERSON_CACHE_DIR
                                + "/" + updatedPerson.getId() + "/" + tempFileName, newFileName);
                    }
                    updatedPerson.setPass(myPerson.getPass());
                    try {
                        LocalDataManager.saveMyPersonInfoToPref(updatedPerson, activity);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if(personProfileFragment!=null && personProfileFragment.isResumed())
                        personProfileFragment.onResume();
                }
                FragmentManager fragmentManager = activity.getSupportFragmentManager();
                if(fragmentManager!=null)
                    fragmentManager.popBackStack();
            }
            else {
                Log.e(TAG, "updatePerson failed error");
                String debugMsg = ErrorVisualizer.getDebugMsgOfRespException(ex);
                if(debugMsg!=null)
                    Log.e(TAG, debugMsg);

                setVisibleProgressBar(false);
                Toast.makeText(activity.getBaseContext(), activity.getString(R.string.editprofile_saveError), Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isChanged(Person pNew, Person p)
    {
        if(!UsefulFunctions.isSameStrValue(p.getName(),pNew.getName()))
            return true;
        if(!UsefulFunctions.isSameStrValue(p.getSurname(),pNew.getSurname()))
            return true;
        long newBirthday = 0;
        if(pNew.getBirthday()!=null)
            newBirthday = pNew.getBirthday().getValue();
        long oldBirthday = 0;
        if(p.getBirthday()!=null)
            oldBirthday = p.getBirthday().getValue();
        if(newBirthday != oldBirthday)
            return true;
        if(!UsefulFunctions.isSameStrValue(p.getPhone(), pNew.getPhone()))
            return true;
        if(!UsefulFunctions.isSameStrValue(p.getPrice(),pNew.getPrice()))
            return true;
        if(pNew.getRating()!= p.getRating())
            return true;
        if(!UsefulFunctions.isSameStrValue(p.getDescription(), pNew.getDescription()))
            return true;
        if(!UsefulFunctions.isSameStrValue(p.getSex(),pNew.getSex()))
            return true;
        return false;
    }

}
