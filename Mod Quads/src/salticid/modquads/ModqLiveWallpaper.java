/*
 * by Kelley Nielsen
 */

package salticid.modquads;

import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;

/**
 * This animated wallpaper draws a simple simulation of a twinkling starfield.
 */

public class ModqLiveWallpaper extends WallpaperService {

    public static final String SHARED_PREFS_NAME="modqsettings";


    @Override
    public void onCreate() {
        super.onCreate();
//         TODO: remove before release, along with calls to Log.*
//        android.os.Debug.waitForDebugger();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public Engine onCreateEngine() {
        return new ModqEngine();
    }

    class ModqEngine extends Engine 
        implements SharedPreferences.OnSharedPreferenceChangeListener {

        private final Handler mHandler = new Handler();
        Modq modq;
        float mWidth;
        float mHeight;
//        private float mTouchX = -1;
//        private float mTouchY = -1;
//        private float mCenterX;
//        private float mCenterY;
//        boolean doTouchPoint = true;
        private static final int desiredFps = 24;
        
        // for taking screenshot
//        boolean holdem = false;
       
        private final Runnable mDrawmodq = new Runnable() {
            public void run() {
                drawFrame();
            }
        };
        private boolean mVisible;
        private SharedPreferences mPrefs;

        ModqEngine() {
            modq = new Modq();
            
          mPrefs = ModqLiveWallpaper.this.getSharedPreferences(SHARED_PREFS_NAME, 0);
            mPrefs.registerOnSharedPreferenceChangeListener(this);
            onSharedPreferenceChanged(mPrefs, null);
        }
        

        public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        	String prefix = prefs.getString("modq_palette", "primary");
    		int rid = getResources().getIdentifier(prefix + "ints", "array", getPackageName());
    		modq.colorList = getResources().getIntArray(rid);
            String prefString = prefs.getString("modq_size", "3");
    		int sizePref = Integer.valueOf(prefString);
            prefString = prefs.getString("modq_density", "8");
            int densityPref = Integer.valueOf(prefString);
//            doTouchPoint = prefs.getBoolean("modq_do_touchevents", true);
//            setTouchEventsEnabled(doTouchPoint);
            modq.reset(sizePref, densityPref);
        }


        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
//            setTouchEventsEnabled(true);
            setTouchEventsEnabled(false);
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            mHandler.removeCallbacks(mDrawmodq);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            mVisible = visible;
            if (visible) {
                drawFrame();
            } else {
                mHandler.removeCallbacks(mDrawmodq);
            }
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
            // store the dimensions of the surface, so we can evenly distribute the modqs
            mWidth = width;           
            mHeight = height;
//            mCenterX = width/2.0f;
//            mCenterY = height/2.0f;
            modq.needScreenDivide = true;
           drawFrame();
        }

        @Override
        public void onSurfaceCreated(SurfaceHolder holder) {
            super.onSurfaceCreated(holder);
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
            mVisible = false;
            mHandler.removeCallbacks(mDrawmodq);
        }

        @Override
        public void onOffsetsChanged(float xOffset, float yOffset,
                float xStep, float yStep, int xPixels, int yPixels) {
        	modq.shift((float)xPixels, (float)yPixels);
            drawFrame();
        }

        /**
         * Store the position of the touch event so we can use it for drawing later
         */
//        @Override
//        public void onTouchEvent(MotionEvent event) {
//            if (event.getAction() == MotionEvent.ACTION_MOVE) {
//                mTouchX = event.getX();
//                mTouchY = event.getY();
//            } else {
//                mTouchX = -1;
//                mTouchY = -1;
//            }
//            super.onTouchEvent(event);
//        }

        /**
         * Draw one frame of the animation. This method gets called repeatedly
         * by posting a delayed Runnable. 
         */
        void drawFrame() {
            final SurfaceHolder holder = getSurfaceHolder();
             
            if (modq.needScreenDivide == true){
                modq.divideUpScreen(mWidth, mHeight);
            }
            Canvas c = null;
            try {
                c = holder.lockCanvas();
                if (c != null) {
                	modq.draw(c);
//             if (doTouchPoint == true) drawTouchPoint(c);
                }
            } finally {
                if (c != null) holder.unlockCanvasAndPost(c);
            }

            mHandler.removeCallbacks(mDrawmodq);
            if (mVisible) {
//            	if (holdem == false)
                mHandler.postDelayed(mDrawmodq, 1000 / desiredFps);
                // pause frame for 5 sec to take a screenshot
//            	else{
//            		holdem = false;
//                    mHandler.postDelayed(mDrawmodq, 1000 * 5);
//            	}
            }
        }



//        void drawTouchPoint(Canvas c) {
//           if (mTouchX >=0 && mTouchY >= 0){
//            }
//       }
    }
}

//TODO: thumbnail on API11 (tablet size) is stretched & blurred, but can't find proper specs yet

///TODO: find more places to add error and exception checking

//TODO: add name and useful/javadoc comments etc as is appropriate

//    why have separate reset and screen divide methods? When do I first know the screen dimensions?
// -> calling divideUpScreen() from onSurfaceChanged() works, but doing it this way
//    causes the values dependent on the parameters to reset() being ignored.
//    divideUpScreen() can't be called from onSharedPreferencesChanged() bc that would result in
//    it being run for the first time with unknown screen dimensions--
//    they aren't known until onSurfaceChanged() is called for the first time.
//    (and the shapes don't exist yet, for the same reason.)
//    After that, onSurfaceChanged() won't be called when onSharedPreferencesChanged()
//    is called again, and calling it from there is too spaghetti-like.
//    Therefore, just use the flag and make the grid in the draw function,
//    where execution always ends up.

//TODO: density setting should either go from "sparse" to "packed", else make it "number"
//TODO: make monochromic palettes => leave for next release 
//TODO: add a preferences item to open market with my other apps (for next release)
//TODO: implement "centered" option => make this a different app

