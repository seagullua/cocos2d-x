/****************************************************************************
Copyright (c) 2010-2013 cocos2d-x.org

http://www.cocos2d-x.org

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
 ****************************************************************************/
package org.cocos2dx.lib;

import org.cocos2dx.lib.Cocos2dxHelper.Cocos2dxHelperListener;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.view.ViewGroup;
import android.util.Log;
import android.widget.FrameLayout;
import android.content.res.Configuration;
import android.widget.Toast;
import android.content.pm.ActivityInfo;
import android.view.OrientationEventListener;
import android.hardware.SensorManager;

public abstract class Cocos2dxActivity extends Activity implements Cocos2dxHelperListener {
	// ===========================================================
	// Constants
	// ===========================================================

	private static final String TAG = Cocos2dxActivity.class.getSimpleName();

	// ===========================================================
	// Fields
	// ===========================================================
	
	private Cocos2dxGLSurfaceView mGLSurfaceView;
	private Cocos2dxHandler mHandler;
	private static Context sContext = null;
	
	public static Context getContext() {
		return sContext;
	}
	
	// ===========================================================
	// Constructors
	// ===========================================================
	
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sContext = this;
    	this.mHandler = new Cocos2dxHandler(this);

    	this.init();

		Cocos2dxHelper.init(this, this);
		
		orientationListener = new OrientationEventListener(this, SensorManager.SENSOR_DELAY_NORMAL) {
			public void onOrientationChanged(int orientation) {
				if(_is_flip_enabled) {
					handleOrientationChange(orientation);
				}
			}
		};
	}
	OrientationEventListener orientationListener = null;
	
	static final int LANDSCAPE = 1;
	static final int LANDSCAPE_REVERSED = 2;
	static final int PORTRAIT = 3;
	static final int PORTRAIT_REVERSED = 4;
	
	int _last_orientation = LANDSCAPE;
	//int _last_orientation_repeated = 0;
	
	boolean _is_running = false;
	boolean _is_default_landscape = true;
	boolean _is_flip_enabled = false; 
	
	void enableOrientationListener()
	{
		if(_is_flip_enabled && orientationListener != null && !_is_running)
		{
			_is_running = true;
			orientationListener.enable();
		}
	}
	
	void disableOrientationListener()
	{
		if(_is_flip_enabled && orientationListener != null && _is_running)
		{
			_is_running = false;
			orientationListener.disable();
		}
	}
	
	int getCurrentOrientation(int angle)
	{
		if(angle >= 0+45 && angle <= 90 + 45)
		{
			return PORTRAIT;
		}
		else if(angle >= 90+45 && angle <= 180 + 45)
		{
			return LANDSCAPE_REVERSED;
		}
		else if(angle >= 180+45 && angle <= 270 +45)
		{
			return PORTRAIT_REVERSED;
		}
		return LANDSCAPE;
	}
	
	void handleOrientationChange(int ori)
	{
		//int rotate_after = 1;
		int orientation = getCurrentOrientation(ori);
		if(_last_orientation != orientation)
		{
			_last_orientation = orientation;
			
			int rotate_to = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
			boolean use_landscape = true;
			
			//Perform rotation
			if(_is_default_landscape)
			{
				//For landscaped
			}
			else
			{
				rotate_to = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
				//For portrait
				if(orientation == PORTRAIT || orientation == PORTRAIT_REVERSED)
				{
					use_landscape = false;
				}
				//else if(orientation == PORTRAIT_REVERSED)
				//{
				//	rotate_to = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT ;
				//	use_landscape = false;
				//}
				else if(orientation == LANDSCAPE || orientation == LANDSCAPE_REVERSED)
				{
					use_landscape = true;
				}
				//else if(orientation == LANDSCAPE_REVERSED)
				//{
				//	rotate_to = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
				//	use_landscape = true;
				//}
			}
			
			setRequestedOrientation(rotate_to);
			orientationChanged(use_landscape);
		}
		//Log.d("ORIENT", "Value: "+getCurrentOrientation(orientation));
	}
	
	public void enableOrientationHandler(boolean default_is_landscape)
	{
		Log.d("ORIENT", "default enabled");
		_is_flip_enabled = true;
		_is_default_landscape = default_is_landscape;
		
		enableOrientationListener();
		//
	}
	
	private native void orientationChanged(boolean is_landscape);
	
	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	@Override
	protected void onResume() {
		super.onResume();

		Cocos2dxHelper.onResume();
		this.mGLSurfaceView.onResume();
		
		enableOrientationListener();
	}

	@Override
	protected void onPause() {
		super.onPause();

		Cocos2dxHelper.onPause();
		this.mGLSurfaceView.onPause();
		
		disableOrientationListener();
	}
	
	@Override
	public void onConfigurationChanged (Configuration newConfig)
	{
		
		int current_orientation = getResources().getConfiguration().orientation;
		
		if(newConfig.orientation != current_orientation)
		{
			newConfig.orientation = current_orientation;
		}
		
		super.onConfigurationChanged(newConfig);

	}
	
	@Override
	public void showDialog(final String pTitle, final String pMessage) {
		Message msg = new Message();
		msg.what = Cocos2dxHandler.HANDLER_SHOW_DIALOG;
		msg.obj = new Cocos2dxHandler.DialogMessage(pTitle, pMessage);
		this.mHandler.sendMessage(msg);
	}

	@Override
	public void showEditTextDialog(final String pTitle, final String pContent, final int pInputMode, final int pInputFlag, final int pReturnType, final int pMaxLength) { 
		Message msg = new Message();
		msg.what = Cocos2dxHandler.HANDLER_SHOW_EDITBOX_DIALOG;
		msg.obj = new Cocos2dxHandler.EditBoxMessage(pTitle, pContent, pInputMode, pInputFlag, pReturnType, pMaxLength);
		this.mHandler.sendMessage(msg);
	}
	
	@Override
	public void runOnGLThread(final Runnable pRunnable) {
		this.mGLSurfaceView.queueEvent(pRunnable);
	}

	// ===========================================================
	// Methods
	// ===========================================================
	public void init() {
		
    	// FrameLayout
        ViewGroup.LayoutParams framelayout_params =
            new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                                       ViewGroup.LayoutParams.FILL_PARENT);
        FrameLayout framelayout = new FrameLayout(this);
        framelayout.setLayoutParams(framelayout_params);

        // Cocos2dxEditText layout
        ViewGroup.LayoutParams edittext_layout_params =
            new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                                       ViewGroup.LayoutParams.WRAP_CONTENT);
        Cocos2dxEditText edittext = new Cocos2dxEditText(this);
        edittext.setLayoutParams(edittext_layout_params);

        // ...add to FrameLayout
        framelayout.addView(edittext);

        // Cocos2dxGLSurfaceView
        this.mGLSurfaceView = this.onCreateView();

        // ...add to FrameLayout
        framelayout.addView(this.mGLSurfaceView);

        // Switch to supported OpenGL (ARGB888) mode on emulator
        if (isAndroidEmulator())
           this.mGLSurfaceView.setEGLConfigChooser(8 , 8, 8, 8, 16, 0);

        this.mGLSurfaceView.setCocos2dxRenderer(new Cocos2dxRenderer());
        this.mGLSurfaceView.setCocos2dxEditText(edittext);

        // Set framelayout as the content view
		setContentView(framelayout);
	}
	
    public Cocos2dxGLSurfaceView onCreateView() {
    	return new Cocos2dxGLSurfaceView(this);
    }

   private final static boolean isAndroidEmulator() {
      String model = Build.MODEL;
      Log.d(TAG, "model=" + model);
      String product = Build.PRODUCT;
      Log.d(TAG, "product=" + product);
      boolean isEmulator = false;
      if (product != null) {
         isEmulator = product.equals("sdk") || product.contains("_sdk") || product.contains("sdk_");
      }
      Log.d(TAG, "isEmulator=" + isEmulator);
      return isEmulator;
   }

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
