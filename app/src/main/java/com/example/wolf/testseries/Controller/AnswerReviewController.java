package com.example.wolf.testseries.Controller;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.util.Log;

import com.example.wolf.testseries.InterfaceController.QuestionPageInterface;
import com.example.wolf.testseries.ParseModelController.QuestionInfo;
import com.example.wolf.testseries.R;
import com.example.wolf.testseries.fragmentController.ImageAnswerReviewFragment;
import com.example.wolf.testseries.fragmentController.PlainAnswerReviewFragment;
import com.example.wolf.testseries.fragmentController.TestResultController;
import com.example.wolf.testseries.modelController.QuestionModel;
import com.example.wolf.testseries.sqliteController.DatabaseController;

import java.util.ArrayList;

/**
 * Created by WOLF on 26-04-2015.
 */
public class AnswerReviewController implements QuestionPageInterface,  TestResultController.TestResultControllerInterface
    {
        private Activity activity;
        private int questionsCount, yearId;
        private ArrayList<QuestionModel> questionInfos;
        public static String RESULT_PAGE_TAG="RESULT_PAGE_TAG";

        private static final int QUESTION_WITH_IMAGE=1;
        private static final int PLAIN_QUESTION=2;

        public AnswerReviewController(Activity activity, int yearId, int questionsCount, ArrayList<QuestionModel> questionInfos)
        {
            this.activity=activity;
            this.questionsCount=questionsCount;
            this.yearId = yearId;
            this.questionInfos=questionInfos;
            initialization();
        }


    private void initialization()
    {
//        questionInfos=new ArrayList<QuestionModel>();
        /**/
        moveToNextQuestion(0);
        DatabaseController databaseController=new DatabaseController(activity);
        databaseController.selectAllQuestions();
    }




    @Override
    public void optionSelected(int questionNumber, int questionId, int optionSelected)
    {
        QuestionModel questionInfo=getSavedResultForQuestionNumber(questionNumber);
        questionInfo.setOptionSelected(optionSelected);
        questionInfo.setQuestionId(questionId);
    }


    private QuestionModel getSavedResultForQuestionNumber(int questionNumber)
    {
        for(QuestionModel questionInfo : questionInfos)
        {
            if(questionInfo.getQuestionNumber()==questionNumber)
            {
                return questionInfo;
            }
        }
        QuestionModel questionInfo=new QuestionModel() ;
        questionInfo.setQuestionNumber(questionNumber);
        questionInfos.add(questionInfo);
        return questionInfo;
    }

    @Override
    public void moveToPreviousQuestion(int currentQuestionNumber) {
        FragmentManager fm = activity.getFragmentManager();
        if (fm.getBackStackEntryCount() > 0)
        {
            Log.d("MainActivity", "popping backstack");
            fm.popBackStack();
        }
    }

    @Override
    public void moveToNextQuestion(int currentQuestionNumber)
    {
        if(currentQuestionNumber==questionsCount)
        {
            //TODO
            Log.d("check", "no more question Available");
            navigateToResultPage();
            return;
        }
        navigateToQuestion(++currentQuestionNumber);
    }

    private void navigateToResultPage()
    {
        double percentage=calculateResult();
        Log.d("percent","percent  ---> "+percentage );
        TestResultController testResultController=TestResultController.newInstance(percentage);
        testResultController.setTestResultInterface(this);
        FragmentManager fragmentManager = activity.getFragmentManager();
        FragmentTransaction fragmentTransaction= fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_container, testResultController, RESULT_PAGE_TAG);
        fragmentTransaction.addToBackStack(RESULT_PAGE_TAG);
        fragmentTransaction.commit();
    }

    private void navigateToQuestion(int questionNumber)
    {
        Fragment fragment=null;
        switch(questionType(questionNumber))
        {
            case PLAIN_QUESTION:
                PlainAnswerReviewFragment questionPage= PlainAnswerReviewFragment.newInstance(questionNumber, yearId, getSavedResultForQuestionNumber(questionNumber).getOptionSelected());
                questionPage.setQuestionPageInterface(this);
                fragment=questionPage;
                break;
            case QUESTION_WITH_IMAGE:
                ImageAnswerReviewFragment questionWithImage= ImageAnswerReviewFragment.newInstance(questionNumber, yearId, getSavedResultForQuestionNumber(questionNumber).getOptionSelected());
                questionWithImage.setQuestionPageInterface(this);
                fragment=questionWithImage;
                break;
        }

        FragmentManager fragmentManager = activity.getFragmentManager();
        FragmentTransaction fragmentTransaction= fragmentManager.beginTransaction();
        if(questionNumber==1)
        {
            fragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
//
        fragmentTransaction.replace(R.id.frame_container, fragment, ""+questionNumber);
        fragmentTransaction.addToBackStack(""+questionNumber);
        fragmentTransaction.commit();
    }


    private double calculateResult()
    {
        int correctOptions=0, unAttemptedQuestions=0;

        for(int i=0; i< questionsCount; i++)
        {
            int originalQuestionNumber=i+1;
            QuestionInfo questionInfo=fetchQuestionInfo(originalQuestionNumber, yearId);
            QuestionModel questionModel=getSavedResultForQuestionNumber(originalQuestionNumber);
            if(questionModel.getOptionSelected()==0)
            {
                unAttemptedQuestions++;
                continue;
            }
            if(questionModel.getOptionSelected()==questionInfo.getAnswerCode())
            {
                correctOptions++;
            }
        }
        Log.d("result", "correct options --> "+correctOptions+"  unAttemptedQuestions --> "+unAttemptedQuestions);
        return calculatePercentage(correctOptions);
    }

    private double calculatePercentage(int correctQuestions)
    {
        double percentage=((double)(correctQuestions*100)/questionsCount);
        return percentage;
    }

    private QuestionInfo fetchQuestionInfo(int questionNumber, int testId)
    {
        DatabaseController databaseController=new DatabaseController(activity);
        QuestionInfo questionInfo=databaseController.getQuestion(questionNumber, testId);
        return questionInfo;
    }


    private int questionType(int questionNumber)
    {
        QuestionInfo questionInfo=fetchQuestionInfo(questionNumber, yearId);
        if(questionInfo.getImageURL()!=null && !questionInfo.getImageURL().equals(""))
        {
            return QUESTION_WITH_IMAGE;
        }
        return PLAIN_QUESTION;
    }


        @Override
        public void reviewQuiz()
        {
            navigateToQuestion(0);
        }
    }
