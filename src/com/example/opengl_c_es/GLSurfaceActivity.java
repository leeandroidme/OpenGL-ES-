package com.example.opengl_c_es;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

public class GLSurfaceActivity extends Activity{
	private GLSurfaceView mGLView;
	static{
		System.loadLibrary("opengl-es");
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mGLView=new MyGLView(this);
		setContentView(mGLView);
	}
}
