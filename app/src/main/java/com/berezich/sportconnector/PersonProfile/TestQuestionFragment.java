package com.berezich.sportconnector.PersonProfile;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.berezich.sportconnector.LocalDataManager;
import com.berezich.sportconnector.R;
import com.berezich.sportconnector.backend.sportConnectorApi.model.Person;

/**
 * Created by Sashka on 05.08.2015.
 */
public class TestQuestionFragment extends Fragment {
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
    public TestQuestionFragment setArgs(int seriesNum, int quetsionNum) {
        Bundle args = new Bundle();
        args.putInt(ARG_SERIES, seriesNum);
        args.putInt(ARG_QUESTION, quetsionNum);
        this.setArguments(args);
        return this;
    }

    public TestQuestionFragment() {
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
        rootView = inflater.inflate(R.layout.fragment_test_question, container, false);

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
        TextView txtView;
        //getActivity().setTitle(R.string.personprofile_fragmentTitle);
        Person myPersonInfo = LocalDataManager.getMyPersonInfo();
        if(myPersonInfo!=null && rootView!=null)
        {

        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.fragment_test_question, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item!=null && item.getItemId()==R.id.menu_testQuestion)
        {
            TestEqualizerFragment testEqualizerFragment = new TestEqualizerFragment().setArgs(1,1);
            FragmentManager fragmentManager = (FragmentManager) getFragmentManager();
            if(fragmentManager!=null)
                fragmentManager.beginTransaction().replace(R.id.container, testEqualizerFragment).commit();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
