package com.yang.jigsaw.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.yang.jigsaw.R;
import com.yang.jigsaw.view.AlertDialogPopupWindow;
import com.yang.jigsaw.view.JigsawLayout;

/**
 * Created by YangHaiPing on 2016/2/16.
 */
public class JigsawFragment extends android.support.v4.app.Fragment {
    //拼图游戏View
    private JigsawLayout jigsawLayout;
    //计时时间、游戏模式、重新载入
    private TextView mTime, mMode, mReload;
    //是否开启计时标志
    private boolean isTimeEnable = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.jigsaw_game, container, false);
        jigsawLayout = (JigsawLayout) view.findViewById(R.id.id_jigsaw);
        //切换模式选择
        TextView mModeSwitcher = (TextView) view.findViewById(R.id.id_jigsaw_switch_mode);
        mMode = (TextView) view.findViewById(R.id.id_jigsaw_mode);
        mTime = (TextView) view.findViewById(R.id.id_jigsaw_time);
        mReload = (TextView) view.findViewById(R.id.id_jigsaw_reload);
        mModeSwitcher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //改变游戏模式
                changeMode();
            }
        });
        mReload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //重新载入新拼图
                jigsawLayout.reload();
            }
        });
        jigsawLayout.setOnJigsawListener(new JigsawLayout.JigsawListener() {
            //监听下一关事件
            @Override
            public void nextLevel(int level) {
                final AlertDialogPopupWindow alertDialog = new AlertDialogPopupWindow(getActivity());
                alertDialog.setPositiveButton("确定", new AlertDialogPopupWindow.OnClickListener() {
                    @Override
                    public void onClick() {
                        jigsawLayout.nextLevel();
                        alertDialog.dismiss();
                    }
                });
                alertDialog.setNegativeButton("取消", new AlertDialogPopupWindow.OnClickListener() {
                    @Override
                    public void onClick() {
                        jigsawLayout.continueCurrentLevel();
                        alertDialog.dismiss();
                    }
                });

                alertDialog.setContent("继续下一幅拼图");
                alertDialog.setTitle("拼图成功！");
                alertDialog.addPicture(R.mipmap.smile);
                alertDialog.setCancelAble(false);
                alertDialog.show();
            }
            //监听时间改变事件
            @Override
            public void timeChange(int currentTime) {
                mTime.setText(currentTime + "");
                if (currentTime <= 10) {
                    mTime.setTextColor(Color.RED);
                } else {
                    mTime.setTextColor(getResources().getColor(R.color.actionbar));
                }
            }
            //监听游戏结束事件
            @Override
            public void gameOver() {
                final AlertDialogPopupWindow alertDialog = new AlertDialogPopupWindow(getActivity());
                alertDialog.setPositiveButton("确定", new AlertDialogPopupWindow.OnClickListener() {
                    @Override
                    public void onClick() {
                        jigsawLayout.continueCurrentLevel();
                        alertDialog.dismiss();
                    }
                });

                alertDialog.setNegativeButton("退出计时模式", new AlertDialogPopupWindow.OnClickListener() {
                    @Override
                    public void onClick() {
                        changeMode();
                        alertDialog.dismiss();
                    }
                });

                alertDialog.setContent("路漫漫其修远兮，\n吾将上下而求索。");
                alertDialog.setTitle("拼图失败！");
                alertDialog.addPicture(R.mipmap.cry);
                alertDialog.setCancelAble(false);
                alertDialog.show();
            }
        });
        return view;
    }

    private void changeMode() {
        isTimeEnable = !isTimeEnable;
        if (isTimeEnable) {
            mMode.setText("计时模式");
            mReload.setVisibility(View.INVISIBLE);
            mTime.setVisibility(View.VISIBLE);
            jigsawLayout.setTimeEnable(true);
        } else {
            mMode.setText("休闲模式");
            mReload.setVisibility(View.VISIBLE);
            mTime.setVisibility(View.INVISIBLE);
            jigsawLayout.setTimeEnable(false);
        }
        //重新加载新的拼图
        jigsawLayout.reload();
    }

    public void pause() {
        //拼图计时模式暂停
        jigsawLayout.pause();
    }

    public void resume() {
        //拼图计时模式继续
        jigsawLayout.resume();
    }

    public boolean isPause() {
        //判断拼图计时模式是否已经暂停
        return jigsawLayout.isPause();
    }

    public boolean isCounting() {
        //判断是否可以计时
        return jigsawLayout.checkIsTimeEnable();
    }

}
