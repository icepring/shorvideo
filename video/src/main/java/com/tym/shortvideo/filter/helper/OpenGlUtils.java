package com.tym.shortvideo.filter.helper;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES11Ext;
import android.opengl.GLES30;
import android.opengl.GLUtils;
import android.util.Log;

import com.tym.shortvideo.filter.base.GPUImageFilter;
import com.tym.shortvideo.recodrender.ParamsManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.microedition.khronos.opengles.GL10;

public class OpenGlUtils {
	public static final int NO_TEXTURE = -1;
	public static final int NOT_INIT = -1;	
	public static final int ON_DRAWN = 1;
	
	public static int loadTexture(final Bitmap img, final int usedTexId) {
		return loadTexture(img, usedTexId, false);
    }
	
	public static int loadTexture(final Bitmap img, final int usedTexId, boolean recyled) {
		if(img == null) {
			return NO_TEXTURE;
		}
        int textures[] = new int[1];
        if (usedTexId == NO_TEXTURE) {
            GLES30.glGenTextures(1, textures, 0);
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textures[0]);
            GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D,
                    GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
            GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D,
                    GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR);
            GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D,
                    GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
            GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D,
                    GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);

            GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, img, 0);
        } else {
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, usedTexId);
            GLUtils.texSubImage2D(GLES30.GL_TEXTURE_2D, 0, 0, 0, img);
            textures[0] = usedTexId;
        }
        if(recyled) {
			img.recycle();
		}
        return textures[0];
    }
	
	public static int loadTexture(final Buffer data, final int width, final int height, final int usedTexId) {
		if(data == null) {
			return NO_TEXTURE;
		}
	    int textures[] = new int[1];
	    if (usedTexId == NO_TEXTURE) {
	        GLES30.glGenTextures(1, textures, 0);
	        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textures[0]);
	        GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D,
	                GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
	        GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D,
	                GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR);
	        GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D,
	                GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
	        GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D,
	                GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);
	        GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGBA, width, height,
	                0, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, data);
	    } else {
	        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, usedTexId);
	        GLES30.glTexSubImage2D(GLES30.GL_TEXTURE_2D, 0, 0, 0, width,
	                height, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, data);
	        textures[0] = usedTexId;
	    }
	    return textures[0];
    }
    
	public static int loadTexture(final Buffer data, final int width, final int height, final int usedTexId, final int type) {
		if(data == null) {
			return NO_TEXTURE;
		}
	    int textures[] = new int[1];
	    if (usedTexId == NO_TEXTURE) {
	        GLES30.glGenTextures(1, textures, 0);
	        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textures[0]);
	        GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D,
	                GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
	        GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D,
	                GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR);
	        GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D,
	                GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
	        GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D,
	                GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);
	        GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGBA, width, height,
	                0, GLES30.GL_RGBA, type, data);
	    } else {
	        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, usedTexId);
	        GLES30.glTexSubImage2D(GLES30.GL_TEXTURE_2D, 0, 0, 0, width,
	                height, GLES30.GL_RGBA, type, data);
	        textures[0] = usedTexId;
	    }
	    return textures[0];
    }
    
    public static int loadTexture(final Context context, final String name){
		final int[] textureHandle = new int[1];
		
		GLES30.glGenTextures(1, textureHandle, 0);
		
		if (textureHandle[0] != 0){

			// Read in the resource
			final Bitmap bitmap = getImageFromAssetsFile(context,name);
						
			// Bind to the texture in OpenGL
			GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureHandle[0]);
			
			// Set filtering
			GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
			GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR);
			GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
			GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);
			// Load the bitmap into the bound texture.
			GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, bitmap, 0);
			
			// Recycle the bitmap, since its data has been loaded into OpenGL.
			bitmap.recycle();						
		}
		
		if (textureHandle[0] == 0){
			throw new RuntimeException("Error loading texture.");
		}
		
		return textureHandle[0];
	}
	
	private static Bitmap getImageFromAssetsFile(Context context, String fileName){
		Bitmap image = null;
	    AssetManager am = context.getResources().getAssets();
	    try{  
			InputStream is = am.open(fileName);
			image = BitmapFactory.decodeStream(is);
			is.close();
          	}catch (IOException e){
	          e.printStackTrace();  
	      }  	  
	      return image;  	  
	}  
    
	public static int loadProgram(final String strVSource, final String strFSource) {
        int iVShader;
        int iFShader;
        int iProgId;
        int[] link = new int[1];
        iVShader = loadShader(strVSource, GLES30.GL_VERTEX_SHADER);
        if (iVShader == 0) {
            Log.d("Load Program", "Vertex Shader Failed");
            return 0;
        }
        iFShader = loadShader(strFSource, GLES30.GL_FRAGMENT_SHADER);
        if (iFShader == 0) {
            Log.d("Load Program", "Fragment Shader Failed");
            return 0;
        }

        iProgId = GLES30.glCreateProgram();
        GLES30.glAttachShader(iProgId, iVShader);
        GLES30.glAttachShader(iProgId, iFShader);
        GLES30.glLinkProgram(iProgId);
        GLES30.glGetProgramiv(iProgId, GLES30.GL_LINK_STATUS, link, 0);
        if (link[0] <= 0) {
            Log.d("Load Program", "Linking Failed");
            return 0;
        }
        GLES30.glDeleteShader(iVShader);
        GLES30.glDeleteShader(iFShader);
        return iProgId;
    }
	
	private static int loadShader(final String strSource, final int iType) {
        int[] compiled = new int[1];
        int iShader = GLES30.glCreateShader(iType);
        GLES30.glShaderSource(iShader, strSource);
        GLES30.glCompileShader(iShader);
        GLES30.glGetShaderiv(iShader, GLES30.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            Log.e("Load Shader Failed", "Compilation\n" + GLES30.glGetShaderInfoLog(iShader));
            return 0;
        }
        return iShader;
    }
	
	public static int getExternalOESTextureID(){		
		int[] texture = new int[1];
		GLES30.glGenTextures(1, texture, 0);
		GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture[0]);
		GLES30.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
				GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
		GLES30.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
		GLES30.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
		GLES30.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
		return texture[0];
	}
	
	public static String readShaderFromRawResource(final int resourceId){
		final InputStream inputStream = ParamsManager.context.getResources().openRawResource(
				resourceId);
		final InputStreamReader inputStreamReader = new InputStreamReader(
				inputStream);
		final BufferedReader bufferedReader = new BufferedReader(
				inputStreamReader);

		String nextLine;
		final StringBuilder body = new StringBuilder();

		try{
			while ((nextLine = bufferedReader.readLine()) != null){
				body.append(nextLine);
				body.append('\n');
			}
		}
		catch (IOException e){
			return null;
		}
		return body.toString();
	}

    public static Bitmap drawToBitmapByFilter(Bitmap bitmap, GPUImageFilter filter,
                                              int displayWidth, int displayHeight, boolean rotate){
        if(filter == null) {
			return null;
		}
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] mFrameBuffers = new int[1];
        int[] mFrameBufferTextures = new int[1];
        GLES30.glGenFramebuffers(1, mFrameBuffers, 0);
        GLES30.glGenTextures(1, mFrameBufferTextures, 0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mFrameBufferTextures[0]);
        GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGBA, width, height, 0,
                GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, null);
        GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D,
                GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
        GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D,
                GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR);
        GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D,
                GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
        GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D,
                GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, mFrameBuffers[0]);
        GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0,
                GLES30.GL_TEXTURE_2D, mFrameBufferTextures[0], 0);
        GLES30.glViewport(0, 0, width, height);
        filter.onInputSizeChanged(width, height);
        filter.onDisplaySizeChanged(displayWidth, displayHeight);
        int textureId = OpenGlUtils.loadTexture(bitmap, OpenGlUtils.NO_TEXTURE, true);
        if(rotate){
            FloatBuffer gLCubeBuffer = ByteBuffer.allocateDirect(TextureRotationUtil.CUBE.length * 4)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer();
            gLCubeBuffer.put(TextureRotationUtil.CUBE).position(0);

            FloatBuffer gLTextureBuffer = ByteBuffer.allocateDirect(TextureRotationUtil.TEXTURE_NO_ROTATION.length * 4)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer();
            gLTextureBuffer.put(TextureRotationUtil.getRotation(Rotation.ROTATION_90, true, false)).position(0);
            filter.onDrawFrame(textureId, gLCubeBuffer, gLTextureBuffer);
        }else {
            filter.onDrawFrame(textureId);
        }
        IntBuffer ib = IntBuffer.allocate(width * height);
        GLES30.glReadPixels(0, 0, width, height, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, ib);
        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        result.copyPixelsFromBuffer(IntBuffer.wrap(ib.array()));
        GLES30.glDeleteTextures(1, new int[]{textureId}, 0);
        GLES30.glDeleteFramebuffers(1, mFrameBuffers, 0);
        GLES30.glDeleteTextures(1, mFrameBufferTextures, 0);
        filter.onInputSizeChanged(displayWidth, displayHeight);
        return result;
    }

	/**
	 * Checks to see if a GLES error has been raised.
	 */
	public static void checkGlError(String op) {
		int error = GLES30.glGetError();
		if (error != GLES30.GL_NO_ERROR) {
			String msg = op + ": glError 0x" + Integer.toHexString(error);
			Log.e("OpenGlUtils", msg);
			throw new RuntimeException(msg);
		}
	}
}
