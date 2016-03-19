package com.yang.jigsaw.adapter;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.yang.jigsaw.R;
import com.yang.jigsaw.bean.SongBean;
import com.yang.jigsaw.utils.StringMatcher;

import java.util.List;

/**
 * Created by YangHaiPing on 2016/2/26.
 */
public class SongAdapter extends BaseAdapter implements SectionIndexer {
    private List<SongBean> mSongsInfo;
    private LayoutInflater mInflater;
    private int mLastClickPosition = -1;
    //索引条文字
    private static final String SECTIONS = "#ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public SongAdapter(Context context, List<SongBean> songsInfo) {
        mSongsInfo = songsInfo;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return mSongsInfo.size();
    }

    @Override
    public Object getItem(int position) {
        //返回歌曲的名字的首个子的首字母,特殊符号返回#号
        return mSongsInfo.get(position).getFirstLetter();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        final ViewHolder viewHolder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.song_list_item, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.name = (TextView) convertView.findViewById(R.id.id_song_name);
            viewHolder.quality = (TextView) convertView.findViewById(R.id.id_song_quality);
            viewHolder.singer = (TextView) convertView.findViewById(R.id.id_song_singer);
            viewHolder.album = (TextView) convertView.findViewById(R.id.id_song_album);
            viewHolder.letterIndex = (TextView) convertView.findViewById(R.id.id_song_letter_index);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        SongBean songBean = mSongsInfo.get(position);
        viewHolder.name.setText(songBean.getName());
        viewHolder.singer.setText(songBean.getSinger());
        viewHolder.album.setText(songBean.getAlbum());
        //设置音乐显示的品质文字
        if (songBean.getSize() >= (2 << 23)) {
            viewHolder.quality.setVisibility(View.VISIBLE);
            viewHolder.quality.setText("无损");
            viewHolder.quality.setBackgroundResource(R.drawable.text_wrapper_song_nondestructive_quality);
        } else if (songBean.getSize() >= (2 << 21)) {
            viewHolder.quality.setVisibility(View.VISIBLE);
            viewHolder.quality.setText("高品质");
            viewHolder.quality.setBackgroundResource(R.drawable.text_wrapper_song_high_quality);
        } else {
            viewHolder.quality.setVisibility(View.GONE);
        }
        //设置当前正在播放的歌曲的颜色
        if (mLastClickPosition == position) {
            viewHolder.name.setTextColor(Color.RED);
        } else {
            viewHolder.name.setTextColor(Color.BLACK);
        }
        //根据歌曲位置获得索引条中相应的索引字母位置
        int section = getSectionForPosition(position);
        //根据索引条的索引字母位置,计算出首个索引位置,如果歌曲的位置与索引条中索引字母的首个索引位置一致,设置歌曲的索引字母可见
        if (position == getPositionForSection(section)) {
            viewHolder.letterIndex.setText(songBean.getFirstLetter()+"");
            viewHolder.letterIndex.setVisibility(View.VISIBLE);
        } else {
            viewHolder.letterIndex.setVisibility(View.GONE);
        }

        return convertView;
    }

    public void recordLastClickPosition(int lastClickPosition) {
        mLastClickPosition = lastClickPosition;
    }

    @Override
    public Object[] getSections() {
        String[] sections = new String[SECTIONS.length()];
        for (int i = 0; i < sections.length; i++) {
            sections[i] = String.valueOf(SECTIONS.charAt(i));
        }
        return sections;
    }

    @Override
    public int getPositionForSection(int sectionIndex) {
        // 如果当前Section没有对应内容,从当前的Section往前查,直到遇到第一个有对应item的为止
        for (int i = sectionIndex; i >= 0; i--) {
            //从第一个歌曲首字的首字母开始比较,匹配则返回,否则继续下一个匹配
            for (int j = 0; j < getCount(); j++) {
                // 索引非文字区域
                if (i == 0) {
                    for (int k = 0; k <= 9; k++) {
                        if (StringMatcher.match(
                                String.valueOf(getItem(j)),
                                String.valueOf(k))) {
                            return j;
                        }
                    }
                }
                // 索引文字范围
                else {
                    // 查询字母
                    if (StringMatcher.match(
                            String.valueOf(getItem(j)),
                            String.valueOf(SECTIONS.charAt(i)))) {
                        return j;
                    }

                }
            }
        }
        return 0;
    }

    @Override
    public int getSectionForPosition(int position) {
        char ch = mSongsInfo.get(position).getFirstLetter();
        if(ch >= 'A' && ch <= 'Z') {
            return ch - 'A' + 1;
        }else{
            return 0;
        }
    }


    class ViewHolder {
        TextView name;
        TextView quality;
        TextView singer;
        TextView album;
        TextView letterIndex;
    }
}
