package com.yang.jigsaw.dao;

import com.yang.jigsaw.bean.ThreadInfo;

import java.util.List;

/**
 * Created by Administrator on 2016/3/6.
 */
public interface ThreadDAO {
    List<ThreadInfo> getThreads(String url);

    void insertThread(ThreadInfo threadInfo);

    void deleteThread(String url, int thread_id);

    void updateThread(String url, int thread_id, int finished);

    boolean isExists(String url, int thread_id);
}
