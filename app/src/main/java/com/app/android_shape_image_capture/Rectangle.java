package com.app.android_shape_image_capture;

/**
 * @author Kumar Shubham
 * 29/1/20
 */
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;

public class Rectangle extends View {
    Paint paint = new Paint();

    Rect rect= new Rect(220, 500, 820, 1300);

    public Rectangle(Context context) {
        super(context);
    }


    @Override
    public void onDraw(Canvas canvas) {
        paint.setColor(Color.GREEN);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(6);
        Log.d("canvas",""+rect.height()+" "+ rect.width()+" Left"+rect.left);
        canvas.drawRect(rect, paint );
    }

    public Rect getRect(){
       return rect;
    }
}