package com.example.opengl_es;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import javax.microedition.khronos.opengles.GL10;

public class MyCute {
	private int mTriangles;
	private IntBuffer mVertexBuffer,mColorBuffer;
	private ByteBuffer mIndexBuffer;
	public MyCute(byte[] indices){
		int one=0x10000;
		int vertices[]={
				-one, -one, -one,
				one, -one, -one,
				one, one, -one,
				-one, one, -one,
				-one, -one, one,
				one, -one, one,
				one, one, one,
				-one, one, one,
		};
		int colors[] = {
				0, 0, 0, one,
				one, 0, 0, one,
				one, 0, one, one,
				0, one, 0, one,
				0, 0, one, one,
				one, one, 0, one,
				one, one, one, one,
				0, one, one, one,
		};
		ByteBuffer vbb=ByteBuffer.allocateDirect(vertices.length*4);
		vbb.order(ByteOrder.nativeOrder());
		mVertexBuffer=vbb.asIntBuffer();
		mVertexBuffer.put(vertices);
		mVertexBuffer.position(0);
		
		ByteBuffer cbb=ByteBuffer.allocateDirect(colors.length*4);
		cbb.order(ByteOrder.nativeOrder());
		mColorBuffer=cbb.asIntBuffer();
		mColorBuffer.put(colors);
		mColorBuffer.position(0);
		
		mIndexBuffer=ByteBuffer.allocateDirect(indices.length);
		mIndexBuffer.put(indices);
		mIndexBuffer.position(0);
		mTriangles=indices.length;
	}
	
	public void draw(GL10 gl){
		//控制多边形的正面是如何决定的
		gl.glFrontFace(GL10.GL_CW);
		
		gl.glVertexPointer(3, GL10.GL_FIXED, 0, mVertexBuffer);
		gl.glColorPointer(4, GL10.GL_FIXED, 0, mColorBuffer);
		gl.glDrawElements(GL10.GL_TRIANGLES, mTriangles, GL10.GL_UNSIGNED_BYTE, mIndexBuffer);
	}
}
