package salticid.modquads;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

    public class Modq {
    	static Random modqRandomizer = new Random();
    	int density= 8;
    	float sizePref = 3;
    	float size = 100;
    	float jitterMax = 70;
        boolean needScreenDivide = false;
    	ArrayList<ModqSingle> modqs;
        private final Paint mPaint;

    	RectF visibleScreen;
    	float xShift = 0;
    	float yShift = 0;
        int[] colorList;
        
    	
    	Modq(){
    		mPaint = new Paint();
            mPaint.setAntiAlias(true);
            mPaint.setStrokeJoin(Paint.Join.ROUND);
            mPaint.setStyle(Paint.Style.STROKE);
    		modqs = null;
    		visibleScreen = new RectF();
     	}
    	
    	/**
    	 * method to divide up the screen and
    	 * allocate the array of evenly distributed (by the grid) ModqSingles,
    	 * to be called at startup and whenever the screen changes
    	 */
        public void divideUpScreen(float mWidth, float mHeight)
        {
        	visibleScreen.set(0, 0, mWidth, mHeight);
        	size = (mHeight < mWidth ? mHeight / 32 : mWidth / 32) * sizePref;
            final int regionWidth = (int)(mWidth * 2 / density);
            final int regionHeight = (int)(mHeight / density);
            jitterMax = (mHeight > mWidth ? regionHeight : regionWidth / 2);
//        	synchronized(modqs){
        	modqs = null;
        	modqs = new ArrayList<ModqSingle>(density * density);
  		for (int j = 0; j < density; j++){
				int y = j * regionHeight + (regionHeight / 2);
  			for (int i = 0; i < density; i++){
 				int x = i * regionWidth + (regionWidth / 2);
 				createModq(x, y);
	  			}
  		}
//        	}
  		needScreenDivide = false;
        }      
    	
        /** method to handle the changes
         * resulting from a shared preferences change
         * that can be best handled here.        
         */
        void reset(int size, int density){
            sizePref = size;  
    		this.density = density;
    		needScreenDivide = true;
        }
        
        /** Moves each ModqSingle in the collection in order to follow scrolling 
         * across the various screens of the home screen.
         * Should be called from ModqEngine.onOffsetsChanged().
         * 
         * @param xamount  number of pixels to shift horizontally.
         * 
         * @param yamount  number of pixels to shift vertically.
         */
        void shift(float xamount, float yamount){
               for (Iterator<ModqSingle> itor = modqs.iterator(); itor.hasNext();){
                	ModqSingle current = itor.next();
        		current.modq.offset(-xShift, -yShift);
        		current.modq.offset(xamount, yamount);
        	}
        	xShift = xamount;
        	yShift = yamount;
        }
    	

        /**
         * Draws one frame. Blanks the screen,
         * then iterates over the collection of ModqSingles,
         * updating and then drawing each one.
         * @param c (android.graphics.Canvas)
         */
        void draw(Canvas c) {
            c.save();
            c.drawColor(0xff000000);
          c.restore();
//            synchronized(modqs){
            for (Iterator<ModqSingle> itor = modqs.iterator(); itor.hasNext();){
            	ModqSingle current = itor.next();
            	current.advance();
           	if(RectF.intersects(current.modq, visibleScreen)){
            	mPaint.setColor(current.color);
            	mPaint.setStrokeWidth(current.strokeWidth);
             c.save();
            current.draw(c, mPaint);
            c.restore();
            }
            }
            }
//            }
        
        /** Instantiates one ModqSingle object
         * and adds it to the list.
         * 
         * @param x  the horizontal location where the shape's range will be centered
         * 
         * @param y the vertical location where the shape's range will be centered
         */
        void createModq(int x, int y){
        	ModqSingle single;
				int shapeChooser = modqRandomizer.nextInt(8);
  				if (shapeChooser < 3)
  				single = new ModqSingleRectangle(x, y);
  				else if (shapeChooser < 6)
  	  				single = new ModqSingleRoundRectangle(x, y);
  				else
  	  				single = new ModqSingleOval(x, y);
  				
        	modqs.add(single);
        }
        
// local classes    	
    	
    	private abstract class ModqSingle{
    	float centerX;
    	float centerY;
    	int color = -1;
    	int lifeCounter = 0;
    	int strokeWidth = 1;
    	float cornerRadius;
    	RectF modq;
    	
    	private static final int lifeSpan = 64;
    	private static final int strokeWidthSpread = 15;
    	private static final int strokeWidthMin = 5;
    	
    	private ModqSingle(int x, int y){    		
    		this.centerX = x;
    		this.centerY = y;
    		modq = new RectF();
    		
    		this.refresh(); 
    		//make it look like the shape has been alive for awhile, 
    		//so that their refresh times are staggered,
    		// and so that the first frame looks like any other frame
    		int degradeFactor = modqRandomizer.nextInt(lifeSpan);
    		lifeCounter -= degradeFactor;
    		color = Color.argb(Color.alpha(color) - degradeFactor * 4,
    				Color.red(color), Color.green(color), Color.blue(color));
    	}
    	
    	/** updates the color of this ModqSingle
    	 * and refreshes it, if necessary.
    	 */
    	public void advance(){
    		lifeCounter--;
    			color = Color.argb(Color.alpha(color) > 4 ? Color.alpha(color) - 4 : 0, 
    					Color.red(color), Color.green(color), Color.blue(color));
   		if (lifeCounter < 0){
    			this.refresh();
    		}
     	}
    	/** Creates a new appearance for this ModqSingle
    	 * When it is at the end of its life span.
    	 * Gives it a new color, line thickness and apparent size,
    	 * and moves it around within its range.
    	 * changes it around rather than killing it and creating a new one
    	 * to save garbage collection, and to keep the grid intact.
    	 */
    	private void refresh(){
    		int tempColor = modqRandomizer.nextInt(colorList.length);
    		color = colorList[tempColor];
    		modq.setEmpty();
    		modq.inset(-(modqRandomizer.nextFloat() * size + size),-(modqRandomizer.nextFloat() * size + size));
    		cornerRadius = (modq.width() < modq.height() ? modq.width() / 5 : modq.height() / 5);
    		modq.offsetTo(centerX + xShift-modq.width()/2, centerY+yShift-modq.width()/2);
    		modq.offset(modqRandomizer.nextFloat() * jitterMax * 2 - jitterMax, modqRandomizer.nextFloat() * jitterMax * 2 - jitterMax);
    		strokeWidth = modqRandomizer.nextInt(strokeWidthSpread) + strokeWidthMin * (int)(sizePref < 5 ? sizePref : 5);
    		lifeCounter = lifeSpan;
    	}
    	
    	public abstract void draw(Canvas cc, Paint mmPaint);
    	
    	} // class ModqSingle
    	
    	private class ModqSingleOval extends ModqSingle{
       		private ModqSingleOval(int x, int y){
       			super(x, y);
       		}
    		
    		public void draw(Canvas cc, Paint mmPaint){
                	cc.drawOval(this.modq, mmPaint);
    		}
    	}
    	private class ModqSingleRoundRectangle extends ModqSingle{
       		private ModqSingleRoundRectangle(int x, int y){
       			super(x, y);
       		}
    		
    		public void draw(Canvas cc, Paint mmPaint){
                cc.drawRoundRect(this.modq, cornerRadius, cornerRadius, mmPaint);
    	}
    	}
    	private class ModqSingleRectangle extends ModqSingle{
       		private ModqSingleRectangle(int x, int y){
       			super(x, y);
       		}
    		
    		public void draw(Canvas cc, Paint mmPaint){
                cc.drawRect(this.modq, mmPaint);
    	}
    	}
    }
