package com.android.volley.toolbox;

import java.io.IOException;

import android.graphics.Bitmap;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyLog;
import com.android.volley.Response.ErrorListener;
import com.android.volley.toolbox.HttpHeaderParser;

public class FileRequest extends Request<NetworkResponse> {
	private final Response.Listener<NetworkResponse> mListener;

	public FileRequest(int method, String url,
			Listener<NetworkResponse> listener, ErrorListener elistener) {
		super(method, url, elistener);
		mListener = listener;
	}

	@Override
	protected Response<NetworkResponse> parseNetworkResponse(
			NetworkResponse response) {

		return Response.success(response,
				HttpHeaderParser.parseCacheHeaders(response));
	}

	@Override
	protected void deliverResponse(NetworkResponse response) {
		mListener.onResponse(response);
	}

}
