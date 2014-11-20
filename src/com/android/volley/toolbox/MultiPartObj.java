package com.android.volley.toolbox;

import java.io.File;
import java.util.Map;
import java.util.Set;

public abstract class MultiPartObj {
	private Image[] images;

	private File[] files;

	public Image[] getImages() {
		return images;
	}

	public void setImages(Image[] images) {
		this.images = images;
	}

	public File[] getFiles() {
		return files;
	}

	public void setFiles(File[] files) {
		this.files = files;
	}

	public abstract Set<Map.Entry<Object, Object>> getParams();

}
