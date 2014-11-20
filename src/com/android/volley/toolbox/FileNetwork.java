package com.android.volley.toolbox;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.conn.ConnectTimeoutException;

import android.os.SystemClock;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.HttpStack;

public class FileNetwork extends BasicNetwork {

	private OnDownloadListener mDownloadListener;

	public FileNetwork(HttpStack httpStack, OnDownloadListener downloadListener) {
		super(httpStack);
		mDownloadListener = downloadListener;
	}

	@Override
	public NetworkResponse performRequest(Request<?> request)
			throws VolleyError {

		long requestStart = SystemClock.elapsedRealtime();
		while (true) {
			HttpResponse httpResponse = null;
			byte[] responseContents = null;
			Map<String, String> responseHeaders = new HashMap<String, String>();
			try {
				// Gather headers.
				Map<String, String> headers = new HashMap<String, String>();
				addCacheHeaders(headers, request.getCacheEntry());
				httpResponse = mHttpStack.performRequest(request, headers);
				StatusLine statusLine = httpResponse.getStatusLine();
				int statusCode = statusLine.getStatusCode();

				responseHeaders = convertHeaders(httpResponse.getAllHeaders());
				// Handle cache validation.
				if (statusCode == HttpStatus.SC_NOT_MODIFIED) {
					return new NetworkResponse(HttpStatus.SC_NOT_MODIFIED,
							request.getCacheEntry() == null ? null : request
									.getCacheEntry().data,
							responseHeaders, true);
				}

				// Some responses such as 204s do not have content. We must
				// check.
				if (httpResponse.getEntity() != null) {
					HttpEntity entity = httpResponse.getEntity();
					long contentLength = httpResponse.getEntity()
							.getContentLength();
					byte[] buf = mPool.getBuf(1024);
					int len = 0;
					BufferedInputStream bis = new BufferedInputStream(
							entity.getContent());

					if (mDownloadListener != null) {
						if (httpResponse.getEntity().getContentLength() > 0) {
							mDownloadListener.onContentLength(contentLength);
							long downloadedSize = 0;
							while ((len = bis.read(buf)) > 0) {
								mDownloadListener.onDataReceived(
										downloadedSize, buf, len);
								 
								downloadedSize += len;
							}
						} else {
							mDownloadListener.onContentLengthError();
						}
						responseContents = buf;
					}
				} else {
					// Add 0 byte response as a way of honestly representing a
					// no-content request.
					responseContents = new byte[0];
				}
				// if the request is slow, log it.
				long requestLifetime = SystemClock.elapsedRealtime()
						- requestStart;
				if (DEBUG || requestLifetime > 3000) {
					VolleyLog.d(
							"HTTP response for request=<%s> [lifetime=%d], [size=%s], "
									+ "[rc=%d], [retryCount=%s]", request,
							requestLifetime,
							responseContents != null ? responseContents.length
									: "null", statusLine.getStatusCode(),
							request.getRetryPolicy().getCurrentRetryCount());
				}

				if (statusCode < 200 || statusCode > 299) {
					throw new IOException();
				}
				return new NetworkResponse(statusCode, responseContents,
						responseHeaders, false);
			} catch (SocketTimeoutException e) {
				e.printStackTrace();
			} catch (ConnectTimeoutException e) {
				e.printStackTrace();
			} catch (MalformedURLException e) {
				throw new RuntimeException("Bad URL " + request.getUrl(), e);
			} catch (IOException e) {
				int statusCode = 0;
				NetworkResponse networkResponse = null;
				if (httpResponse != null) {
					statusCode = httpResponse.getStatusLine().getStatusCode();
				} else {
					throw new NoConnectionError(e);
				}
				VolleyLog.e("Unexpected response code %d for %s", statusCode,
						request.getUrl());
				if (responseContents != null) {
					networkResponse = new NetworkResponse(statusCode,
							responseContents, responseHeaders, false);
					if (statusCode == HttpStatus.SC_UNAUTHORIZED
							|| statusCode == HttpStatus.SC_FORBIDDEN) {
						new AuthFailureError(networkResponse).printStackTrace();
					} else {
						// TODO: Only throw ServerError for 5xx status codes.
						throw new ServerError(networkResponse);
					}
				} else {
					throw new NetworkError(networkResponse);
				}
			}
		}

	}

	public void setDownloadListener(OnDownloadListener mDownloadListener) {
		this.mDownloadListener = mDownloadListener;
	}

	public interface OnDownloadListener {

		public void onContentLengthError();

		public void onContentLength(long contentLength);

		public void onDataReceived(long downSize, byte[] data, int dataSize)
				throws IOException;
	}
}
