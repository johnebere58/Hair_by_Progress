package com.hairbyprogress;

/**
 * Created by John Ebere on 5/17/2017.
 */

public interface DownloadListener {

    void onComplete(String error, Object result);

    void onProgress(int progress);

}
