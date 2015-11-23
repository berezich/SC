package com.berezich.sportconnector.Fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.berezich.sportconnector.AlertDialogFragment;
import com.berezich.sportconnector.AppPref;
import com.berezich.sportconnector.EndpointApi.EndpointApi;
import com.berezich.sportconnector.ErrorVisualizer;
import com.berezich.sportconnector.LocalDataManager;
import com.berezich.sportconnector.MainActivity;
import com.berezich.sportconnector.R;
import com.berezich.sportconnector.backend.sportConnectorApi.model.Person;

import java.io.IOException;


public class LoginFragment extends Fragment implements EndpointApi.AuthorizePersonAsyncTask.OnAction,
        AlertDialogFragment.OnActionDialogListener,
        RegistrationFragment.RegFragmentAction,
        ResetPassFragment.ResetPassFragmentAction {

    private static final String ARG_SECTION_NUMBER = "section_number";
    private final String TAG = "MyLog_LoginFragment";
    OnActionListenerLoginFragment listenerLoginFragment = null;
    private Person myPersonInfo;
    private AppPref appPref;
    private String pass;
    private boolean isPassChanged = false;
    private View rootView;
    private AlertDialogFragment dialog;
    private String defaultPass;

    public LoginFragment() {
    }

    public LoginFragment setArgs(int sectionNumber) {
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        this.setArguments(args);
        return this;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setHasOptionsMenu(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        try {
            EditText editTxt;
            rootView = inflater.inflate(R.layout.fragment_login, container, false);
            if (rootView != null) {
                Button btn = (Button) rootView.findViewById(R.id.login_btn_ok);
                if (btn != null)
                    btn.setOnClickListener(new OnClickLoginListener());

                btn = (Button) rootView.findViewById(R.id.loginRegister_btn);
                if (btn != null)
                    btn.setOnClickListener(new OnClickRegistrationListener());
                TextView textView = (TextView) rootView.findViewById(R.id.login_text_forgotPass);
                if (textView != null)
                    textView.setOnClickListener(new OnClickForgotPassListener());
                if ((editTxt = (EditText) rootView.findViewById(R.id.login_pass_value)) != null)
                    editTxt.setOnFocusChangeListener(new OnPassFocused());
                defaultPass = "";
                if ((myPersonInfo = LocalDataManager.getMyPersonInfo()) != null) {
                    if (!myPersonInfo.getPass().isEmpty())
                        defaultPass = getActivity().getString(R.string.login_defaultPass);
                    if ((editTxt = (EditText) rootView.findViewById(R.id.login_email_value)) != null) {
                        editTxt.setText(myPersonInfo.getEmail());
                    }
                    if ((editTxt = (EditText) rootView.findViewById(R.id.login_pass_value)) != null) {
                        editTxt.setText(defaultPass);

                        if ((appPref = LocalDataManager.getAppPref()) == null) {
                            AppPref appPref = new AppPref(false);
                            try {
                                LocalDataManager.saveAppPref(appPref, getActivity());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                if (getActivity() != null)
                    ((MainActivity) getActivity()).setupUI(rootView);
            }
            return rootView;
        } catch (Exception e) {
            e.printStackTrace();
            return rootView = null;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            if (dialog != null) {
                dialog.dismiss();
                setVisibleProgressBar(false);
                dialog = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onAttach(Activity activity) {
        try {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));
            try {
                listenerLoginFragment = (OnActionListenerLoginFragment) activity;
            } catch (ClassCastException e) {
                throw new ClassCastException(activity.toString() + " must implement OnActionListener for LoginFragment");
            }
        } catch (ClassCastException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onResume() {
        try {
            super.onResume();

            if (dialog != null) {
                dialog.dismiss();
                dialog = null;
                setVisibleProgressBar(false);
            } else if ((appPref = LocalDataManager.getAppPref()) != null) {
                if (appPref.isAutoLogin() && myPersonInfo.getPass() != null && !myPersonInfo.getPass().equals("")) {
                    setVisibleProgressBar(true);
                    new EndpointApi.AuthorizePersonAsyncTask(this).execute(myPersonInfo.getEmail(), (pass = myPersonInfo.getPass()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setVisibleProgressBar(boolean isVisible) {
        View view;
        if (rootView != null) {
            if ((view = rootView.findViewById(R.id.loginFragment_linearLayout)) != null)
                view.setVisibility(!isVisible ? View.VISIBLE : View.GONE);
            if ((view = rootView.findViewById(R.id.loginFragment_frameLayout)) != null)
                view.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onAuthorizePersonAsyncTaskFinish(Pair<Person, Exception> result) {
        try {
            Person person = result.first;
            Exception error = result.second;
            if (getActivity() == null) {
                Log.e(TAG, "current fragment isn't attached to activity");
                return;
            }
            if (error == null && person != null) {
                Log.d(TAG, "AuthorizePerson OK");
                try {
                    person.setPass(pass);
                    LocalDataManager.saveMyPersonInfoToPref(person, getActivity());
                    AppPref appPref = LocalDataManager.getAppPref();
                    if (appPref == null)
                        appPref = new AppPref(true);
                    else
                        appPref.setIsAutoLogin(true);
                    LocalDataManager.saveAppPref(appPref, getActivity());
                    listenerLoginFragment.onAuthorized();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return;
            }
            Log.e(TAG, "Error AuthorizePerson");


            try {
                if (isPassChanged) {
                    if (myPersonInfo != null) {
                        myPersonInfo.setPass("");
                        LocalDataManager.saveMyPersonInfoToPref(myPersonInfo, getActivity());
                    }
                    if (appPref != null) {
                        appPref.setIsAutoLogin(false);
                        LocalDataManager.saveAppPref(appPref, getActivity());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            String dialogMsg;
            Pair<ErrorVisualizer.ERROR_CODE, String> errTxtCode = ErrorVisualizer.getTextCodeOfRespException(getActivity().getBaseContext(), error);
            if (errTxtCode != null && !errTxtCode.second.equals(""))
                dialogMsg = errTxtCode.second;
            else
                dialogMsg = getString(R.string.server_unknown_err);
            Log.e(TAG, "request error: " + ErrorVisualizer.getDebugMsgOfRespException(error));
            dialog = AlertDialogFragment.newInstance(dialogMsg, false);
            dialog.setTargetFragment(this, 0);
            dialog.setCancelable(false);
            FragmentManager ft = getActivity().getSupportFragmentManager();
            if (ft != null)
                dialog.show(ft, "");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onPositiveClick() {
        try {
            setVisibleProgressBar(false);
            dialog = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onNegativeClick() {
        try {
            setVisibleProgressBar(false);
            dialog = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCancelDialog() {
        try {
            setVisibleProgressBar(false);
            dialog = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        try {
            super.onCreateOptionsMenu(menu, inflater);
            menu.clear();
            ActionBar actionBar = ((MainActivity) getActivity()).getSupportActionBar();
            if(actionBar!=null)
                actionBar.setTitle(R.string.login_fragmentTitle);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreateAccount(String msgResult) {
        try {
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            if (fragmentManager != null)
                fragmentManager.popBackStack();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResetPass(String msgResult) {
        try {
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            if (fragmentManager != null)
                fragmentManager.popBackStack();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public interface OnActionListenerLoginFragment {
        void onAuthorized();
    }

    private class OnPassFocused implements View.OnFocusChangeListener {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            try {
                EditText passEdt = (EditText) v;
                if (hasFocus) {
                    if (!isPassChanged) {
                        passEdt.setText("");
                        passEdt.setFilters(new InputFilter[]{new OnPassChanged()});
                    }
                } else {
                    passEdt.setFilters(new InputFilter[0]);
                    if (!isPassChanged)
                        passEdt.setText(defaultPass);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private class OnPassChanged implements InputFilter {
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            isPassChanged = true;
            return null;
        }
    }

    private class OnClickLoginListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            try {
                String login = "";
                pass = "";
                EditText editTxt;
                if ((editTxt = (EditText) rootView.findViewById(R.id.login_email_value)) != null)
                    login = editTxt.getText().toString().trim();
                if ((editTxt = (EditText) rootView.findViewById(R.id.login_pass_value)) != null)
                    if (isPassChanged)
                        pass = editTxt.getText().toString();
                    else {
                        Person myPersonInfo = LocalDataManager.getMyPersonInfo();
                        if (myPersonInfo != null)
                            pass = myPersonInfo.getPass();
                        else
                            pass = "";
                    }

                setVisibleProgressBar(true);
                new EndpointApi.AuthorizePersonAsyncTask(LoginFragment.this).execute(login, pass);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class OnClickRegistrationListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            try {
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                RegistrationFragment registrationFragment = new RegistrationFragment();
                registrationFragment.setTargetFragment(LoginFragment.this, 0);
                if (fragmentManager != null) {
                    fragmentManager.beginTransaction().replace(R.id.container, registrationFragment)
                            .addToBackStack(registrationFragment.getClass().getName()).commit();
                    Log.d(TAG, String.format("prev fragment replaced with %s", registrationFragment.getClass().getName()));

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class OnClickForgotPassListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            try {
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                ResetPassFragment resetPassFragment = new ResetPassFragment();
                resetPassFragment.setTargetFragment(LoginFragment.this, 0);
                if (fragmentManager != null) {
                    fragmentManager.beginTransaction().replace(R.id.container, resetPassFragment)
                            .addToBackStack(resetPassFragment.getClass().getName()).commit();
                    Log.d(TAG, String.format("prev fragment replaced with %s", resetPassFragment.getClass().getName()));

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}