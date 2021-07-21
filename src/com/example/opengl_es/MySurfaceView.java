package com.example.opengl_es;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGL11;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL10;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
/**
 * http://docs.oracle.com/javame/config/cldc/opt-pkgs/api/jb/jsr239/javax/microedition/khronos/egl/EGL10.html
 * http://www.360doc.com/content/14/1212/17/18578054_432440967.shtml
 * @author 刘伦
 */
public class MySurfaceView extends SurfaceView implements SurfaceHolder.Callback{
	private SurfaceHolder mHolder;
	public float mAngle;
	private GLThread mGLThread;
	public MySurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public MySurfaceView(Context context) {
		this(context,null);
	}

	private void init(){
		mHolder=getHolder();
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_GPU);
		mHolder.addCallback(this);
	}
	
	
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		mGLThread=new GLThread();
		mGLThread.start();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		mGLThread.onWindowResize(width, height);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		mGLThread.reqestExitAndWait();
		mGLThread=null;
	}

	class GLThread extends Thread{
		private boolean mDone;
		private boolean mSizeChanged=true;
		private int mWidth,mHeight;
		private MyCute mCube;
		GLThread(){
			super();
			mDone=false;
			mWidth=0;
			mHeight=0;
			//决定欲绘制出的立方体的面
			byte indices[]={
					6,0,1, 5,1,0,
					1,5,6, 0,6,5,
					2,3,7, 6,2,7
			};
			mCube=new MyCute(indices);
		}
		@Override
		public void run() {
			EGL10 egl=(EGL10) EGLContext.getEGL();
			
			//1 dpy表示显示设备
			javax.microedition.khronos.egl.EGLDisplay dpy=egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
			
			
			int[] version=new int[2];
			egl.eglInitialize(dpy, version);
			int[] configSpec={
				EGL10.EGL_RED_SIZE,8,
				EGL10.EGL_GREEN_SIZE,8,
				EGL10.EGL_BLUE_SIZE,8,
				EGL10.EGL_DEPTH_SIZE,8,
				EGL10.EGL_NONE
			};
			EGLConfig[] configs=new EGLConfig[1];
			int [] num_config=new int[1];
			egl.eglGetConfigs(dpy, null, 0, num_config);
			//2 初始化参数设置
			//attrib_list:选择配置时需要参照配置的属性
			//configs:将返回按照attrib_list排序的平台有效的所有配置
			//config_size:configs总配置个数
			//num_config：实际匹配的配置总数
			egl.eglChooseConfig(dpy, configSpec, configs, 1, num_config);
			
			//3 configs[]存有OpenGL ES 组态值(configuration)
			EGLConfig config=configs[0];
			
			//4 诞生OpenGL ES幕后环境
			EGLContext glc=egl.eglCreateContext(dpy, config, EGL10.EGL_NO_CONTEXT, null);
			//glc代表OpenGL ES的current context
			//5 下面的surface则代表绘图面
			EGLSurface surface=null;
			GL10 gl=null;
			while(!mDone){
				int w,h;
				boolean changed;
				synchronized(this){
					changed=mSizeChanged;
					w=mWidth;
					h=mHeight;
					mSizeChanged=false;
				}
				//当窗口大小改变了，立即重新诞生一个绘图面
				if(changed){
					//诞生新的绘图面
					surface=egl.eglCreateWindowSurface(dpy, config, mHolder, null);
					//将新的绘图面加入到current_context
					egl.eglMakeCurrent(dpy, surface, surface, glc);
					//向current_context取得它的绘图接口
					gl=(GL10) glc.getGL();
					gl.glDisable(GL10.GL_DITHER);
					//起始参数设定 (http://baike.baidu.com/link?url=m4Cebwxx50-bjN-6LsLgyOAxAGdJhE1yHy05SRHUN9LTcw3Jj3FRdxaby3Ek2O8aOpcARaVTnOehgr05neEHfq)
					gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT,GL10.GL_FASTEST);
					gl.glClearColor(1, 1, 1, 1);
					//(http://baike.baidu.com/view/1280546.htm)
					gl.glEnable(GL10.GL_CULL_FACE);
					//GL_SMOOTH：光滑着色 线段上各点的颜色将根据两个顶点的颜色通过插值得到
					gl.glShadeModel(GL10.GL_SMOOTH);
					gl.glEnable(GL10.GL_DEPTH_TEST);
					gl.glViewport(0, 0, w, h);
					
					//设定投影(projection)矩阵
					float ratio=(float)w/h;
					//设置矩阵模型 projection投影 modelview模型视镜 texture纹理
					gl.glMatrixMode(GL10.GL_PROJECTION);
					//重置当前指定的矩阵为单位矩阵
					gl.glLoadIdentity();
					//zNear, float zFar 近裁面和远裁面的距离 任何远于10或近于1的对象都将被过滤掉
					gl.glFrustumf(-ratio, ratio, -1, 1, 1f, 10);
				}
				//实际绘图
				drawFrame(gl);
				//变换buffer 用于显示
				egl.eglSwapBuffers(dpy, surface);
				//如果egl上下文丢失则退出
				if(egl.eglGetError()==EGL11.EGL_CONTEXT_LOST){
					Context c=getContext();
					if(c instanceof Activity){
						((Activity)c).finish();
					}
				}
			}
			//准备结束
			egl.eglMakeCurrent(dpy, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
			egl.eglDestroySurface(dpy, surface);
			egl.eglDestroyContext(dpy, glc);
			egl.eglTerminate(dpy);
		}
		/**
		 * 绘图3D对象
		 * @param gl
		 * @author 刘伦
		 * @Time 2016-2-13 下午9:21:47
		 */
		private void drawFrame(GL10 gl){
			gl.glClear(GL10.GL_COLOR_BUFFER_BIT|GL10.GL_DEPTH_BUFFER_BIT);
			//设定旋转度
			gl.glMatrixMode(GL10.GL_MODELVIEW);
			gl.glLoadIdentity();
			gl.glTranslatef(0, 0, -3f);
			gl.glRotatef(mAngle, 0, 1, 0);
			gl.glRotatef(mAngle*0.25f, 1, 0, 0);
			gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
			gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
			mCube.draw(gl);
			mAngle+=1.2f;
		}
		public void onWindowResize(int w,int h){
			synchronized (this) {
				mWidth=w;
				mHeight=h;
				mSizeChanged=true;
			}
		}
		public void reqestExitAndWait(){
			//避免deadlock
			mDone=true;
			try{
				join();
			}catch(InterruptedException ex){
				
			}
		}
	}
}
