package com.berezich.sportconnector.PersonProfile;

import android.app.Activity;
import android.content.DialogInterface;
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
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.berezich.sportconnector.AlertDialogFragment;
import com.berezich.sportconnector.DatePickerFragment;
import com.berezich.sportconnector.EndpointApi;
import com.berezich.sportconnector.ErrorVisualizer;
import com.berezich.sportconnector.LocalDataManager;
import com.berezich.sportconnector.MainActivity;
import com.berezich.sportconnector.R;
import com.berezich.sportconnector.UsefulFunctions;
import com.berezich.sportconnector.backend.sportConnectorApi.model.Person;
import com.google.api.client.util.DateTime;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by Sashka on 25.07.2015.
 */
public class EditProfileFragment extends Fragment implements DatePickerFragment.OnActionDatePickerDialogListener,
                                                             ChangePassFragment.OnActionPassDialogListener,
                                                             EndpointApi.UpdatePersonAsyncTask.OnAction{

    //private static final String ARG_SECTION_NUMBER = "section_number";
    private final String TAG = "MyLog_EditProfileFrg";
    private final String mSex = "MALE";
    private final String fSex = "FEMALE";
    private final int minAge = 16;
    View rootView;
    Person tempMyPerson;
    /*public EditProfileFragment setArgs(int sectionNumber) {
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        this.setArguments(args);
        return this;
    }*/

    public EditProfileFragment() {
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
    public void onResume() {
        super.onResume();
        TextView txtView;
        EditText txtEdt;
        DateTime birthday;
        ImageButton imgBtn;
        Spinner spinner;

        final Person myPersonInfo = LocalDataManager.getMyPersonInfo();
        RadioGroup radioGroup;
        if (myPersonInfo != null && rootView != null) {
            tempMyPerson = myPersonInfo.clone();
            if ((txtEdt = (EditText) rootView.findViewById(R.id.editProfile_txtEdt_name)) != null)
                txtEdt.setText(myPersonInfo.getName());
            if ((txtEdt = (EditText) rootView.findViewById(R.id.editProfile_txtEdt_surname)) != null)
                txtEdt.setText(myPersonInfo.getSurname());
            if ((txtView = (TextView) rootView.findViewById(R.id.editProfile_txtView_birthday)) != null) {
                Date date;
                if ((birthday = myPersonInfo.getBirthday()) != null)
                    date = new Date(birthday.getValue());
                else {
                    Calendar calendar = Calendar.getInstance();
                    date = calendar.getTime();
                }
                txtView.setText(String.format("%1$td.%1$tm.%1$tY", date));
                txtView.setOnClickListener(new OnBirthdayClickListener());
            }
            if ((radioGroup = (RadioGroup) rootView.findViewById(R.id.editProfile_radioGrp_sex)) != null) {
                String sex = myPersonInfo.getSex() != null ? myPersonInfo.getSex() : "";
                if (sex.equals(mSex))
                    radioGroup.check(R.id.editProfile_radio_male);
                else if (sex.equals(fSex))
                    radioGroup.check(R.id.editProfile_radio_female);
                else
                    radioGroup.clearCheck();
            }

            String email = myPersonInfo.getEmail(), phone = myPersonInfo.getPhone();
            if ((txtView = (TextView) rootView.findViewById(R.id.editProfile_txtEdt_email)) != null)
                txtView.setText(email);
            if ((txtView = (TextView) rootView.findViewById(R.id.editProfile_txtEdt_phone)) != null)
                txtView.setText(phone);

            if ((txtView = (TextView) rootView.findViewById(R.id.editProfile_txtView_changePass)) != null) {
                txtView.setOnClickListener(new PassOnClickListener());
            }

            if ((txtView = (TextView) rootView.findViewById(R.id.editProfile_txtView_setRating)) != null) {
                //txtView.setText(getString(R.string.personprofile_rating) + " " + myPersonInfo.getRating());
                txtView.setText(getString(R.string.personprofile_rating));
                txtView.setOnClickListener(new RatingOnClickListener());
            }

            if ((spinner = (Spinner) rootView.findViewById(R.id.editProfile_spinner_rating)) != null) {
                String ratings = getString(R.string.ratingInfo_ratingValLst);
                if(ratings!=null) {
                    final String ratingArr[] = ratings.split(",");
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, ratingArr);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
                    spinner.setAdapter(adapter);
                    int pos = (int)(tempMyPerson.getRating()/0.5-2);
                    if(pos<=ratingArr.length)
                        spinner.setSelection(pos);
                    else
                        spinner.setSelection(0);

                    // устанавливаем обработчик нажатия
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view,
                                                   int position, long id) {
                            float rating = Float.valueOf(ratingArr[position]);
                            if( tempMyPerson.getRating()!=rating) {
                                tempMyPerson.setRating(rating);
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
                txtView.setText(myPersonInfo.getDescription());

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
        actionBar.setTitle(R.string.editprofile_fragmentTitle);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        EditText txtEdt;
        TextView txtView;
        RadioGroup radioGroup;
        Person myPersonInfo = LocalDataManager.getMyPersonInfo();
        ((MainActivity) getActivity()).hideSoftKeyboard();
        switch (item.getItemId())
        {
            case R.id.menu_save_profile:
                //Toast.makeText(getActivity().getBaseContext(),"SAVE",Toast.LENGTH_SHORT).show();
                if(myPersonInfo!=null && rootView!=null)
                {
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
                                tempMyPerson.setSex(mSex);
                                break;
                        }

                    //middle block
                    String email = myPersonInfo.getEmail(),phone = myPersonInfo.getPhone();
                    if((txtView = (TextView) rootView.findViewById(R.id.editProfile_txtEdt_email))!=null)
                        tempMyPerson.setEmail(txtView.getText().toString());
                    if((txtView = (TextView) rootView.findViewById(R.id.editProfile_txtEdt_phone))!=null)
                        tempMyPerson.setPhone(txtView.getText().toString());

                    if((txtEdt = (EditText) rootView.findViewById(R.id.editProfile_txtEdt_desc))!=null)
                        tempMyPerson.setDescription(txtEdt.getText().toString());
                    new EndpointApi.UpdatePersonAsyncTask(this).execute(tempMyPerson);
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private class OnBirthdayClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            DatePickerFragment datePickerFragment = new DatePickerFragment();
            TextView txtView = (TextView) v;
            if(datePickerFragment!=null) {
                datePickerFragment.setArgs(txtView.getText().toString());
                datePickerFragment.setTargetFragment(getCurFragment(), -1);
                datePickerFragment.show(getFragmentManager(), null);
            }
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
        tempMyPerson.setPass(newPass);
    }

    private class RatingOnClickListener implements View.OnClickListener
    {
        @Override
        public void onClick(View v) {
            FragmentManager fragmentManager = (FragmentManager) getFragmentManager();
            /*TestQuestionFragment testQuestionFragment = new TestQuestionFragment().setArgs(1,1);
            if(fragmentManager!=null)
                fragmentManager.beginTransaction().replace(R.id.container, testQuestionFragment).addToBackStack(null).commit();
*/
        }
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

            FragmentManager fragmentManager = (FragmentManager) getFragmentManager();
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

    @Override
    public void onUpdatePersonFinish(Pair<Person, Exception> result) {
        Person updatedPerson = result.first;
        Exception ex = result.second;
        if(ex==null && updatedPerson!=null)
        {
            Person myPerson = LocalDataManager.getMyPersonInfo();
            if(myPerson!=null) {
                updatedPerson.setPass(tempMyPerson.getPass());
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
                Log.e(TAG,debugMsg);
            Toast.makeText(getActivity().getBaseContext(), getString(R.string.editprofile_saveError), Toast.LENGTH_LONG).show();
        }
    }
}
