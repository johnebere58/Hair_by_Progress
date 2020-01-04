package com.hairbyprogress;

import android.os.AsyncTask;
import android.os.Build;
import android.text.TextUtils;

import com.hairbyprogress.utils.StringUtil;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

public class DownloadTask extends AsyncTask<Object, Integer, Object> {

    private String fileUrl;

	private String fileSavePath;

	private String fileName;

    private int fileSize;


    private DownloadListener downloadListener;

	public DownloadTask(String fileUrl, String fileSavePath,
							 String fileName,DownloadListener downloadListener) {
		this.fileUrl = fileUrl;
		this.fileSavePath = fileSavePath;
		this.fileName = fileName;
        this.downloadListener = downloadListener;
	}

	@Override
	protected Object doInBackground(Object... params) {
		disableConnectionReuseIfNecessary();
		BufferedInputStream bis = null;
		FileOutputStream fos = null;
		try {
			if (TextUtils.isEmpty(fileName)) {
				fileName = StringUtil.getUrlFileName(fileUrl);
			}
			final URL url = new URL(fileUrl);
			HttpURLConnection urlConnection = (HttpURLConnection) url
					.openConnection();
			urlConnection.connect();
			fileSize = urlConnection.getContentLength();
            String fileSizeM = String.format(Locale.ENGLISH, "%.2f",
                    (float) fileSize / 1024 / 1024);
			bis = new BufferedInputStream(urlConnection.getInputStream());
			File saveFile = new File(fileSavePath, fileName);
			fos = new FileOutputStream(saveFile);
			downloadFile(bis, fos);

			downloadListener.onComplete(null,null);
			return true;
		} catch (MalformedURLException e) {
			e.printStackTrace();
			tryToDeleteFile();
			downloadListener.onComplete("error",null);
		} catch (IOException e) {
			e.printStackTrace();
			tryToDeleteFile();
			downloadListener.onComplete("error",null);
		} catch (Exception e) {
			e.printStackTrace();
			tryToDeleteFile();
			downloadListener.onComplete("error",null);
		} finally {
			try {
				if (bis != null)
					bis.close();
				if (fos != null)
					fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	private void tryToDeleteFile(){
		File saveFile = new File(fileSavePath, fileName);
		try {
			if(saveFile.exists())saveFile.delete();
		}catch (Exception e){};
	}

	private void downloadFile(InputStream in, OutputStream out) throws IOException {
		int len = 0;
		byte[] buffer = new byte[1024 * 2];
		int current = 0;
		int bytesInThreshold = 0;
		long updateDelta = 0;
		long updateStart = System.currentTimeMillis();
		while ((len = in.read(buffer)) != -1) {
			out.write(buffer, 0, len);
			out.flush();
			current += len;
			bytesInThreshold += len;
			// 因为Notification状态栏不能更新太过于频繁
			// 该算法有待优化
			// if (percent % 5 == 0) {
			// if (percent > random.nextInt(10)) {
            long UPDATE_INTERVAL = 1000;
            if (updateDelta > UPDATE_INTERVAL) {
				int percent = (current * 100) / fileSize;
				long downloadSpeed = bytesInThreshold / updateDelta;

                downloadListener.onProgress(percent);
				// reset data
				updateStart = System.currentTimeMillis();
				bytesInThreshold = 0;
			}
			updateDelta = System.currentTimeMillis() - updateStart;
		}

	}


	@Override
	protected void onPreExecute() {
		super.onPreExecute();

	}

	@Override
	protected void onPostExecute(Object result) {
		super.onPostExecute(result);

	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		super.onProgressUpdate(values);
        int progress = values[0];
		int percent = values[1];
		int speed = values[2];
		downloadListener.onProgress(progress);

	}

	/**
	 * Workaround for bug pre-Froyo, see here for more info:
	 * http://android-developers.blogspot.com/2011/09/androids-http-clients.html
	 */
    private void disableConnectionReuseIfNecessary() {
		// HTTP connection reuse which was buggy pre-froyo
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
			System.setProperty("http.keepAlive", "false");
		}
	}

}