package com.example.pencil;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.text.method.Touch;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

public class PaintView extends View {
    private Bitmap btmView,btmBackground;
    private Paint paint = new Paint();
    private Path mPath = new Path();
    private Float mX,mY;
    private Canvas mCanvas;
    private int sizeEraser,sizeBrush,colorBackground;
    private static final  float Touch_TOERANCE = 4;
    private ArrayList<Bitmap> listAction = new ArrayList<>();


    public PaintView(Context context, AttributeSet attrs) {
        super(context,attrs);


        init();


    }

    private void init(){
        sizeEraser =sizeBrush = 12;
        colorBackground = Color.WHITE;

        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(toPx(sizeBrush));


    }

    private float toPx(int sizeBrush){
        return sizeBrush*(getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        btmBackground = Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);
        btmView = Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(btmView);


    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawColor(colorBackground);
        canvas.drawBitmap(btmBackground,0,0,null);
        canvas.drawBitmap(btmView,0,0,null);

    }
    public void setColourBackground(int color){
        colorBackground = color;
        invalidate();
    }
    public void setSizeBrush(int s){
        sizeBrush = s;
        paint.setStrokeWidth(toPx(sizeBrush));
    }
    public void setBrushColour(int color){
        paint.setColor(color);
    }
    public void setSizeEraser(int s){
        sizeEraser = s;
        paint.setStrokeWidth(toPx(sizeEraser));
    }
    public void enableEraser(){
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
    }
    public void disableEraser(){
        paint.setXfermode(null);
        paint.setShader(null);
        paint.setMaskFilter(null);
    }
    public void addLastAction(Bitmap bitmap){
        listAction.add(bitmap);
    }
    public void returnLastAction() {
        if (listAction.size()>0){
            listAction.remove(listAction.size()-1);
            if (listAction.size()>0){
                btmView = listAction.get(listAction.size()-1);

            }else {
                btmView = Bitmap.createBitmap(getWidth(),getHeight(),Bitmap.Config.ARGB_8888);

            }
            mCanvas = new Canvas(btmView);
            invalidate();
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                touchStart(x,y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touchMove(x,y);
                invalidate();
                break;

            case MotionEvent.ACTION_UP:
                touchUp();
                addLastAction(getBitmap());
                break;
        }
        return true;
    }

    private void touchUp(){
        mPath.reset();
    }


    private void touchMove(float x, float y) {
        float dx = Math.abs(x- mX);
        float dy = Math.abs(y- mY);

        if (dx>= Touch_TOERANCE ||  dy>Touch_TOERANCE){
            mPath.quadTo(mX,mY,(x+mX)/2,(y+mY)/2);
            mX = x;
            mY = y;
            mCanvas.drawPath(mPath,paint);
            invalidate();
        }
    }



    private void touchStart( float x, float y){

        mPath.moveTo(x,y);
        mX = x;
        mY = y;


    }


    public Bitmap getBitmap(){
        this.setDrawingCacheEnabled(true);
        this.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(this.getDrawingCache());
        this.setDrawingCacheEnabled(false);
        return bitmap;
    }

    public void clear(){
       // mPath.clear();
        invalidate();
    }
}
