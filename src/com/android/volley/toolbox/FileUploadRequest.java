package com.android.volley.toolbox;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.apache.http.util.ByteArrayBuffer;
import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;

public class FileUploadRequest<T> extends Request<T> {

	String MULTIPART_FORM_DATA = "multipart/form-data";
	String TWOHYPHENS = "--";
	String BOUNDARY = "****************yqhuibao"; // 数据分隔符
	String LINEEND = System.getProperty("line.separator");
	private MultiPartObj mMultiPartObj;
	private Response.Listener<T> mListener;

	public FileUploadRequest(MultiPartObj obj, String url,
			Response.Listener<T> listener, ErrorListener errorListener) {
		super(Method.POST, url, errorListener);
		mMultiPartObj = obj;
		mListener = listener;
	}

	@Override
	protected Response<T> parseNetworkResponse(NetworkResponse response) {
		return Response.success(null,null);
	}

	@Override
	protected void deliverResponse(T response) {
		mListener.onResponse(response);

	}

	@Override
	public Map<String, String> getHeaders() throws AuthFailureError {
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Connection", "keep-alive");
		return headers;
	}

	@Override
	public String getBodyContentType() {
		return MULTIPART_FORM_DATA + "; boundary=" + BOUNDARY;
	}

	@Override
	public byte[] getBody() throws AuthFailureError {
		ByteArrayBuffer bab = new ByteArrayBuffer(32);
		byte[] fields = addFormField(mMultiPartObj.getParams());
		bab.append(fields, 0, fields.length);
		byte[] imgs = addImageContent(mMultiPartObj.getImages());
		bab.append(imgs, 0, imgs.length);
		return bab.toByteArray();
	}

	private byte[] addImageContent(Image[] files) {
		ByteArrayBuffer bab = new ByteArrayBuffer(32);
		for (Image file : files) {
			StringBuilder split = new StringBuilder();
			split.append(TWOHYPHENS + BOUNDARY + LINEEND);
			split.append("Content-Disposition: form-data; name=\""
					+ file.getFormName() + "\"; filename=\""
					+ file.getFormName() + "\"" + LINEEND);
			split.append("Content-Type: " + file.getContentType() + LINEEND);
			split.append(LINEEND);
			byte[] bytes = split.toString().getBytes();
			bab.append(bytes, 0, bytes.length);
		}
		return bab.toByteArray();
	}

	private byte[] addFormField(Set<Map.Entry<Object, Object>> params) {
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<Object, Object> param : params) {
			sb.append(TWOHYPHENS + BOUNDARY + LINEEND);
			sb.append("Content-Disposition: form-data; name=\""
					+ param.getKey() + "\"" + LINEEND);
			sb.append(LINEEND);
			sb.append(param.getValue() + LINEEND);
		}
		return sb.toString().getBytes();
	}
}
