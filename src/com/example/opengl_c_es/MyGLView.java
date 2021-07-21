package com.example.opengl_c_es;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

import com.example.opengl_es.R;

public class MyGLView extends GLSurfaceView implements GLSurfaceView.Renderer{
	private Context mContext;
	public MyGLView(Context context){
		super(context);
		setRenderer(this);
		requestFocus();
		setFocusableInTouchMode(true);
		mContext=context;
	}
	

	private static native void nativeInit();
	private static native void nativePause();
	
	private static native void nativeResize(int w,int h);
	private static native void nativeRender();
	private static native void nativePushTexture(int[] pixels,int w,int h);
	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		nativeInit();
		Bitmap bitmap=BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_launcher);
		int[] pixels=new int[bitmap.getWidth()*bitmap.getHeight()];
		bitmap.getPixels(pixels,0,bitmap.getWidth(),0,0,bitmap.getWidth(),bitmap.getHeight());
		nativePushTexture(pixels, bitmap.getWidth(), bitmap.getHeight());
	}
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if(event.getAction()==MotionEvent.ACTION_UP){
			nativePause();
		}
		return true;
	}
	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		nativeResize(width,height);
	}

	@Override
	public void onDrawFrame(GL10 gl) {
		nativeRender();
	}
	

}