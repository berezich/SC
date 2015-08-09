package com.berezich.sportconnector.PersonProfile;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.berezich.sportconnector.LocalDataManager;
import com.berezich.sportconnector.R;
import com.berezich.sportconnector.backend.sportConnectorApi.model.Person;

/**
 * Created by Sashka on 08.08.2015.
 */
public class TestEqualizerFragment extends Fragment {
        private final String TAG = "MyLog_TestQuestion";
        private final String ARG_SERIES = "seriesNumber";
        private final String ARG_QUESTION = "questionNumber";
        enum STATE {QUESTION,EQUALIZER}
        private STATE state = STATE.QUESTION;
        private int seriesNum=-1;
        private int questionNum=-1;
        View rootView;
        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public TestEqualizerFragment setArgs(int seriesNum, int quetsionNum) {
            Bundle args = new Bundle();
            args.putInt(ARG_SERIES, seriesNum);
            args.putInt(ARG_QUESTION, quetsionNum);
            this.setArguments(args);
            return this;
        }

        public TestEqualizerFragment() {
        }

        @Override
        public void onCreate(Bundle savedInstanceState)
        {
            setHasOptionsMenu(true);
            super.onCreate(savedInstanceState);
            seriesNum = getArguments().getInt(ARG_SERIES);
            questionNum = getArguments().getInt(ARG_QUESTION);
        }
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            rootView = inflater.inflate(R.layout.fragment_equalizer, container, false);

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
        public void onResume()
        {
            super.onResume();
            SeekBar seekBar;
            TextView txtView;
            //getActivity().setTitle(R.string.personprofile_fragmentTitle);
            Person myPersonInfo = LocalDataManager.getMyPersonInfo();
            if(myPersonInfo!=null && rootView!=null)
            {
                seekBar = (SeekBar) rootView.findViewById(R.id.seekBar);
                if(seekBar!=null)
                    seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeList());
                seekBar = (SeekBar) rootView.findViewById(R.id.seekBar2);
                if(seekBar!=null)
                    seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeList());
                seekBar = (SeekBar) rootView.findViewById(R.id.seekBar3);
                if(seekBar!=null)
                    seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeList());
                seekBar = (SeekBar) rootView.findViewById(R.id.seekBar4);
                if(seekBar!=null)
                    seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeList());
            }
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            menu.clear();
            super.onCreateOptionsMenu(menu, inflater);
            inflater.inflate(R.menu.fragment_test_equalizer, menu);
            ActionBar actionBar =((AppCompatActivity) getActivity()).getSupportActionBar();
            actionBar.setTitle(R.string.equalizer_fragmentTitle);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            if(item!=null && item.getItemId()==R.id.menu_testEqualizer)
            {
                TestQuestionFragment testQuestionFragment = new TestQuestionFragment().setArgs(1,1);
                FragmentManager fragmentManager = (FragmentManager) getFragmentManager();
                if(fragmentManager!=null)
                    fragmentManager.beginTransaction().replace(R.id.container, testQuestionFragment).commit();
                return true;
            }
            return super.onOptionsItemSelected(item);
        }

        private class OnSeekBarChangeList implements SeekBar.OnSeekBarChangeListener{
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                TextView txtView;
                if(seekBar!=null && rootView!=null)
                    switch (seekBar.getId()){
                        case R.id.seekBar:
                            if((txtView = (TextView) rootView.findViewById(R.id.textView11))!=null)
                                txtView.setText(String.valueOf(progress));
                            break;
                        case R.id.seekBar2:
                            if((txtView = (TextView) rootView.findViewById(R.id.textView13))!=null)
                                txtView.setText(String.valueOf(progress));
                            break;
                        case R.id.seekBar3:
                            if((txtView = (TextView) rootView.findViewById(R.id.textView16))!=null)
                                txtView.setText(String.valueOf(progress));
                            break;
                        case R.id.seekBar4:
                            if((txtView = (TextView) rootView.findViewById(R.id.textView17))!=null)
                                txtView.setText(String.valueOf(progress));
                            break;
                    }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        }
    }

