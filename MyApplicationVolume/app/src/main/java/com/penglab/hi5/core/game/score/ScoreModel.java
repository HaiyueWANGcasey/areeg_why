package com.penglab.hi5.core.game.score;

import androidx.lifecycle.MutableLiveData;

import com.penglab.hi5.core.MainActivity;
import com.penglab.hi5.core.game.quest.DailyQuestsModel;
import com.penglab.hi5.core.game.quest.Quest;
import com.penglab.hi5.data.dataStore.database.User;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Yihang zhu 12/29/21
 */
public class ScoreModel {
    private String id;
    private MutableLiveData<Integer> score = new MutableLiveData<>();
    private int curveNum;
    private int markerNum;
    private int lastLoginYear;
    private int lastLoginDay;
    private int curveNumToday;
    private int markerNumToday;
    private int editImageNum;
    private int editImageNumToday;

    private DailyQuestsModel dailyQuestsModel;

    public ScoreModel() {
        dailyQuestsModel = new DailyQuestsModel();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
        dailyQuestsModel.setUserId(id);
    }

    public MutableLiveData<Integer> getObservableScore() {
        return score;
    }

    public int getScore() {
        return score.getValue();
    }

    public void setScore(int score) {
        this.score.postValue(score);
    }

    public int getCurveNum() {
        return curveNum;
    }

    public void setCurveNum(int curveNum) {
        this.curveNum = curveNum;
    }

    public int getMarkerNum() {
        return markerNum;
    }

    public void setMarkerNum(int markerNum) {
        this.markerNum = markerNum;
    }

    public int getLastLoginYear() {
        return lastLoginYear;
    }

    public void setLastLoginYear(int lastLoginYear) {
        this.lastLoginYear = lastLoginYear;
    }

    public int getLastLoginDay() {
        return lastLoginDay;
    }

    public void setLastLoginDay(int lastLoginDay) {
        this.lastLoginDay = lastLoginDay;
    }

    public int getCurveNumToday() {
        return curveNumToday;
    }

    public void setCurveNumToday(int curveNumToday) {
        this.curveNumToday = curveNumToday;
    }

    public int getMarkerNumToday() {
        return markerNumToday;
    }

    public void setMarkerNumToday(int markerNumToday) {
        this.markerNumToday = markerNumToday;
    }

    public int getEditImageNum() {
        return editImageNum;
    }

    public void setEditImageNum(int editImageNum) {
        this.editImageNum = editImageNum;
    }

    public int getEditImageNumToday() {
        return editImageNumToday;
    }

    public void setEditImageNumToday(int editImageNumToday) {
        this.editImageNumToday = editImageNumToday;
    }

    public DailyQuestsModel getDailyQuestsModel() {
        return dailyQuestsModel;
    }

    public void setDailyQuestsModel(DailyQuestsModel dailyQuestsModel) {
        this.dailyQuestsModel = dailyQuestsModel;
    }

    public boolean initFromLitePal(){
        boolean result = true;
        ScoreLitePalConnector scoreLitePalConnector = new ScoreLitePalConnector(id);
        ScoreModel scoreModel = scoreLitePalConnector.getScoreModelFromLitePal();
        if (scoreModel == null) {
            result = false;
        } else {
            updateWithNewScoreModel(scoreModel);
        }

        dailyQuestsModel.initFromLitePal();

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int date = calendar.get(Calendar.DAY_OF_YEAR);
        if (year > lastLoginYear || (year == lastLoginYear && date > lastLoginDay)){
            dailyQuestsModel.updateNDailyQuest(0, 1);
        }

        return result;
    }

    public boolean serverUpdateScore(int serverScore){
        ScoreLitePalConnector scoreLitePalConnector = new ScoreLitePalConnector(id);
        if (score.getValue() < serverScore){
            score.postValue(serverScore);
            scoreLitePalConnector.updateScore(serverScore);
            return true;
        } else if (score.getValue() >= serverScore){
            return false;
        }
        return true;
    }

    public void drawACurve(){
        curveNum += 1;
        curveNumToday += 1;
        addScore(ScoreRule.getScorePerCurve());

        dailyQuestsModel.updateCurveNum(curveNumToday);

        User user = new User();
        user.setScore(score.getValue());
        user.setCurveNum(curveNum);
        user.setCurveNumToday(curveNumToday);
        user.updateAll("userid = ?", id);
    }

    public void pinpoint(){
        markerNum += 1;
        markerNumToday += 1;
        addScore(ScoreRule.getScorePerPinPoint());

        dailyQuestsModel.updateMarkerNum(markerNumToday);

        User user = new User();
        user.setScore(score.getValue());
        user.setMarkerNum(markerNum);
        user.setMarkerNumToday(markerNumToday);
        user.updateAll("userid = ?", id);
    }

    public void getScorePerRewardLevel (int level){

        addScore(ScoreRule.getScorePerRewardLevel()*level);

        User user = new User();
        user.setScore(score.getValue());
        user.updateAll("userid = ?", id);

    }

    public void getScorePerGuessMusic (){
        addScore(ScoreRule.getScorePerGuessMusic());
        User user = new User();
        user.setScore(score.getValue());
        user.updateAll("userid = ?", id);
    }


    public void finishAnImage(){
        addScore(ScoreRule.getScorePerImage());

        editImageNum += 1;
        editImageNumToday += 1;

        User user = new User();
        user.setScore(score.getValue());
        user.setEditImageNum(editImageNum);
        user.setEditImageNumToday(editImageNumToday);
        user.updateAll("userid = ?", id);
    }

    public void addScore(int s){
        score.setValue(score.getValue() + s);

        User user = new User();
        user.setScore(score.getValue());
        user.updateAll("userid = ?", id);
    }

    private void updateWithNewScoreModel(ScoreModel scoreModel) {
        this.score = scoreModel.score;
        this.curveNum = scoreModel.curveNum;
        this.markerNum = scoreModel.markerNum;
        this.curveNumToday = scoreModel.curveNumToday;
        this.markerNumToday = scoreModel.markerNumToday;
        this.lastLoginDay = scoreModel.lastLoginDay;
        this.lastLoginYear = scoreModel.lastLoginYear;
        this.editImageNum = scoreModel.editImageNum;
        this.editImageNumToday = scoreModel.editImageNumToday;
    }

    public void questFinished(Quest quest) {
        int rewardScore = dailyQuestsModel.questFinished(quest);
        addScore(rewardScore);
    }
}
