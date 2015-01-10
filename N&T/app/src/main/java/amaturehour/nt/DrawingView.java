package amaturehour.nt;

import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.MotionEvent;
import android.graphics.Paint;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.content.Context;

/**
 * Created by Jericho on 1/5/15.
 */

public class DrawingView extends View{

    protected Paint mPaint;
    protected Bitmap mBitmap;
    protected Canvas mCanvas;

    private float mx;
    private float my;
    private float mStartX;
    private float mStartY;

    private int mCurrentShape;
    private boolean isDrawing;

    private static final int RECTANGLE = 1;
    private static final int TOUCH_STROKE_WIDTH = 2;
    private static final String TAG = "EDIT";



    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        //get the screen density, width, and height from the device display
        Context viewContext = getContext();
        int density = viewContext.getResources().getDisplayMetrics().densityDpi;
        Display display = getDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;
        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        if(mBitmap.isMutable())
            Log.e(TAG, "bitmap is mutable!!!!!!!!!!!!!");
        mCanvas = new Canvas(mBitmap);
    }

    public DrawingView(Context context) {
        super(context);
        init();
    }

    public DrawingView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    protected void init() {

        mPaint = new Paint(Paint.DITHER_FLAG);
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(getContext().getResources().getColor(android.R.color.holo_blue_dark));
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(TOUCH_STROKE_WIDTH);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mx = event.getX();
        my = event.getY();
        switch (mCurrentShape) {
            case RECTANGLE:
                onTouchEventRectangle(event);
                break;
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(mBitmap, 0, 0, mPaint);

        if (isDrawing){
            switch (mCurrentShape) {
                case RECTANGLE:
                    onDrawRectangle(canvas);
                    break;
            }
        }
    }

    //------------------------------------------------------------------
    // Rectangle
    //------------------------------------------------------------------

    private void onTouchEventRectangle(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isDrawing = true;
                mStartX = mx;
                mStartY = my;
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                isDrawing = false;
                drawRectangle(mCanvas,mPaint);
                invalidate();
                break;
        }
    }

    private void onDrawRectangle(Canvas canvas) {
        drawRectangle(canvas,mPaint);
    }

    private void drawRectangle(Canvas canvas,Paint paint){
        float right = mStartX > mx ? mStartX : mx;
        float left = mStartX > mx ? mx : mStartX;
        float bottom = mStartY > my ? mStartY : my;
        float top = mStartY > my ? my : mStartY;
        canvas.drawRect(left, top , right, bottom, paint);
    }

    public void setCurrentShape(int shape){
        mCurrentShape = shape;
    }

    public void setBitmapDimensions(Bitmap tgt){
       mBitmap = Bitmap.createBitmap(tgt.getWidth(), tgt.getHeight(), Bitmap.Config.ARGB_8888);
    }

}
