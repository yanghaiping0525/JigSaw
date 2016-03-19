package com.yang.jigsaw.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import com.yang.jigsaw.R;

/**
 * Created by YangHaiPing on 2016/2/16.
 */
public class SettingFragment extends Fragment implements RadioGroup.OnCheckedChangeListener {
    //游戏拼图背景图片的选择,游戏拼图布局的选择,游戏每局时间的选择,游戏递进关卡的选择
    private RadioGroup mBackGroundGroup, mGameLayoutGroup, mGameTimeGroup, mGameProgressiveGroup;
    /*private RadioButton mChoiceDefaultButton, mChoiceMyButton, mChoiceAllButton;
    private RadioButton m2x2LayoutButton, m3x3LayoutButton, m4x4LayoutButton;
    private RadioButton mTimeShortButton, mTimeNormalButton, mTimeLongButton;
    private RadioButton mProgressiveOne,mProgressiveTwo,mProgressiveThree;*/

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.setting_fragment, container, false);
        mBackGroundGroup = (RadioGroup) view.findViewById(R.id.id_radio_group_setting_background);
        mGameLayoutGroup = (RadioGroup) view.findViewById(R.id.id_radio_group_setting_layout);
        mGameTimeGroup = (RadioGroup) view.findViewById(R.id.id_radio_group_setting_time);
        mGameProgressiveGroup = (RadioGroup) view.findViewById(R.id.id_radio_group_progressive);
        /*mChoiceDefaultButton = (RadioButton) view.findViewById(R.id.id_radio_button_pic_default);
        mChoiceAllButton = (RadioButton) view.findViewById(R.id.id_radio_button_pic_all);
        mChoiceMyButton = (RadioButton) view.findViewById(R.id.id_radio_button_pic_my);
        m2x2LayoutButton = (RadioButton) view.findViewById(R.id.id_radio_button_layout_2x2);
        m3x3LayoutButton = (RadioButton) view.findViewById(R.id.id_radio_button_layout_3x3);
        m4x4LayoutButton = (RadioButton) view.findViewById(R.id.id_radio_button_layout_4x4);
        mTimeLongButton = (RadioButton) view.findViewById(R.id.id_radio_button_time_long);
        mTimeNormalButton = (RadioButton) view.findViewById(R.id.id_radio_button_time_normal);
        mTimeShortButton = (RadioButton) view.findViewById(R.id.id_radio_button_time_short);
        mProgressiveOne = (RadioButton) view.findViewById(R.id.id_radio_button_progressive_one);
        mProgressiveTwo = (RadioButton) view.findViewById(R.id.id_radio_button_progressive_two);
        mProgressiveThree = (RadioButton) view.findViewById(R.id.id_radio_button_progressive_three);*/
        //读取游戏图片选择配置文件,显示被选中的radioButton
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("backgroundPictures", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (sharedPreferences.getString("backgroundPictures", "empty").equals("empty")) {
            final String pic = "defaultPictures";
            editor.clear();
            editor.commit();
            editor.putString("backgroundPictures", pic);
            editor.commit();
            mBackGroundGroup.check(R.id.id_radio_button_pic_default);
        } else if (sharedPreferences.getString("backgroundPictures", "empty").equals("defaultPictures")) {
            mBackGroundGroup.check(R.id.id_radio_button_pic_default);
        } else if (sharedPreferences.getString("backgroundPictures", "empty").equals("myPictures")) {
            // mChoiceMyButton.setChecked(true);
            mBackGroundGroup.check(R.id.id_radio_button_pic_my);
        } else if (sharedPreferences.getString("backgroundPictures", "empty").equals("allPictures")) {
            // mChoiceAllButton.setChecked(true);
            mBackGroundGroup.check(R.id.id_radio_button_pic_all);
        }
        //读取游戏拼图布局配置文件,显示被选中的radioButton
        sharedPreferences = getContext().getSharedPreferences("gameLayout", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        if (sharedPreferences.getInt("gameLayout", -1) == -1) {
            final int layout = 3;
            editor.clear();
            editor.commit();
            editor.putInt("gameLayout", layout);
            editor.commit();
            mGameLayoutGroup.check(R.id.id_radio_button_layout_3x3);
        } else if (sharedPreferences.getInt("gameLayout", -1) == 2) {
            mGameLayoutGroup.check(R.id.id_radio_button_layout_2x2);
        } else if (sharedPreferences.getInt("gameLayout", -1) == 3) {
            mGameLayoutGroup.check(R.id.id_radio_button_layout_3x3);
        } else if (sharedPreferences.getInt("gameLayout", -1) == 4) {
            mGameLayoutGroup.check(R.id.id_radio_button_layout_4x4);
        }
        //读取游戏通关时间配置文件,显示被选中的radioButton
        sharedPreferences = getContext().getSharedPreferences("gameTime", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        if (sharedPreferences.getString("gameTime", "empty").equals("empty")) {
            final String time = "normal";
            editor.clear();
            editor.commit();
            editor.putString("gameTime", time);
            editor.commit();
            mGameTimeGroup.check(R.id.id_radio_button_time_normal);
        } else if (sharedPreferences.getString("gameTime", "empty").equals("short")) {
            mGameTimeGroup.check(R.id.id_radio_button_time_short);
        } else if (sharedPreferences.getString("gameTime", "empty").equals("normal")) {
            mGameTimeGroup.check(R.id.id_radio_button_time_normal);
        } else if (sharedPreferences.getString("gameTime", "empty").equals("long")) {
            mGameTimeGroup.check(R.id.id_radio_button_time_long);
        }
        //读取游戏递进关卡配置文件,显示被选中的radioButton
        sharedPreferences = getContext().getSharedPreferences("gameProgressive", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        if(sharedPreferences.getInt("gameProgressive",-1) == -1){
            final int progressive = 3;
            editor.clear();
            editor.commit();
            editor.putInt("gameLayout", progressive);
            editor.commit();
            mGameProgressiveGroup.check(R.id.id_radio_button_progressive_one);
        }else if(sharedPreferences.getInt("gameProgressive",-1) == 1){
            mGameProgressiveGroup.check(R.id.id_radio_button_progressive_one);
        }else if(sharedPreferences.getInt("gameProgressive",-1) == 2){
            mGameProgressiveGroup.check(R.id.id_radio_button_progressive_two);
        }else if(sharedPreferences.getInt("gameProgressive",-1) == 3){
            mGameProgressiveGroup.check(R.id.id_radio_button_progressive_three);
        }

        mBackGroundGroup.setOnCheckedChangeListener(this);
        mGameLayoutGroup.setOnCheckedChangeListener(this);
        mGameTimeGroup.setOnCheckedChangeListener(this);
        mGameProgressiveGroup.setOnCheckedChangeListener(this);
        return view;
    }

    //当某个设置项被选中,更新配置文件
    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.id_radio_button_pic_default: {
                SharedPreferences sharedPreferences = getContext().getSharedPreferences("backgroundPictures", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                final String pic = "defaultPictures";
                editor.clear();
                editor.commit();
                editor.putString("backgroundPictures", pic);
                editor.commit();
                break;
            }
            case R.id.id_radio_button_pic_all: {
                SharedPreferences sharedPreferences = getContext().getSharedPreferences("backgroundPictures", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                final String pic = "allPictures";
                editor.clear();
                editor.commit();
                editor.putString("backgroundPictures", pic);
                editor.commit();
                break;
            }
            case R.id.id_radio_button_pic_my: {
                SharedPreferences sharedPreferences = getContext().getSharedPreferences("backgroundPictures", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                final String pic = "myPictures";
                editor.clear();
                editor.commit();
                editor.putString("backgroundPictures", pic);
                editor.commit();
                break;
            }
            case R.id.id_radio_button_layout_2x2: {
                SharedPreferences sharedPreferences = getContext().getSharedPreferences("gameLayout", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                final int layout = 2;
                editor.clear();
                editor.commit();
                editor.putInt("gameLayout", layout);
                editor.commit();
                break;
            }
            case R.id.id_radio_button_layout_3x3: {
                SharedPreferences sharedPreferences = getContext().getSharedPreferences("gameLayout", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                final int layout = 3;
                editor.clear();
                editor.commit();
                editor.putInt("gameLayout", layout);
                editor.commit();
                break;
            }
            case R.id.id_radio_button_layout_4x4: {
                SharedPreferences sharedPreferences = getContext().getSharedPreferences("gameLayout", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                final int layout = 4;
                editor.clear();
                editor.commit();
                editor.putInt("gameLayout", layout);
                editor.commit();
                break;
            }
            case R.id.id_radio_button_time_long: {
                SharedPreferences sharedPreferences = getContext().getSharedPreferences("gameTime", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                final String time = "long";
                editor.clear();
                editor.commit();
                editor.putString("gameTime", time);
                editor.commit();
                break;
            }
            case R.id.id_radio_button_time_normal: {
                SharedPreferences sharedPreferences = getContext().getSharedPreferences("gameTime", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                final String time = "normal";
                editor.clear();
                editor.commit();
                editor.putString("gameTime", time);
                editor.commit();
                break;
            }
            case R.id.id_radio_button_time_short: {
                SharedPreferences sharedPreferences = getContext().getSharedPreferences("gameTime", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                final String time = "short";
                editor.clear();
                editor.commit();
                editor.putString("gameTime", time);
                editor.commit();
                break;
            }
            case R.id.id_radio_button_progressive_one:{
                SharedPreferences sharedPreferences = getContext().getSharedPreferences("gameProgressive", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                final int progressive = 1;
                editor.clear();
                editor.commit();
                editor.putInt("gameProgressive", progressive);
                editor.commit();
                break;
            }
            case R.id.id_radio_button_progressive_two:{
                SharedPreferences sharedPreferences = getContext().getSharedPreferences("gameProgressive", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                final int progressive = 2;
                editor.clear();
                editor.commit();
                editor.putInt("gameProgressive",progressive);
                editor.commit();
                break;
            }
            case R.id.id_radio_button_progressive_three:{
                SharedPreferences sharedPreferences = getContext().getSharedPreferences("gameProgressive", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                final int progressive = 3;
                editor.clear();
                editor.commit();
                editor.putInt("gameProgressive",progressive);
                editor.commit();
                break;
            }
        }
    }
}
