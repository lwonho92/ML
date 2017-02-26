package com.example.android.BluetoothChat;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;


import java.util.List;

/**
 * Created by pusik on 2016-07-05.
 */
public class GraphView extends View {
    PointList list1;
    PointList list2;
    int pre1;
    int pre2;
    public int windowWidth;
    public int windowHeight;
    public int desiredWidth;
    public int desiredHeight;
    public int level = 300;
    int textSize;
    private Paint[] paint = null;
    private Path mPath;
    private String tag = "";
    
    boolean isClicked = false;


    public GraphView(Context context, String id) {
        super(context);

        list1 = new PointList();
        list2 = new PointList();
        tag = id;
        
        Display display = ((WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        windowWidth = display.getWidth();
        windowHeight = display.getHeight();
        
        desiredWidth = windowWidth / 2;
        desiredHeight = (windowHeight / 7) * 2;

        textSize = desiredWidth / 40;
        pre1 = 0;
        pre2 = 0;

        paint = new Paint[4];
        for(int i = 0; i < 4; i++) { paint[i] = new Paint(); }

        paint[0].setStrokeWidth(2);
        paint[0].setColor(Color.BLACK);
        paint[0].setAlpha(128);
        paint[0].setTextSize(textSize);
        paint[1].setARGB(128, 128, 128, 128);
        paint[1].setStyle(Paint.Style.STROKE);
        paint[1].setPathEffect(new DashPathEffect(new float[]{100, 100}, 0));
        paint[2].setStrokeWidth(2);
        paint[2].setColor(Color.BLUE);
        paint[2].setAlpha(128);
        paint[3].setStrokeWidth(2);
        paint[3].setColor(Color.GREEN);
        paint[3].setAlpha(128);
    }
    public void setTextSize(int coefficient) {
    	textSize *= coefficient;
    	paint[0].setTextSize(textSize);
    }
   
    public void addPoint(int a, int b) {
        list1.addItem(a);
        list2.addItem(b);

        this.invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawLine(0, 0, 0, desiredHeight, paint[0]);
        canvas.drawLine(0, desiredHeight, desiredWidth, desiredHeight, paint[0]);
        canvas.drawLine(0, (float)desiredHeight / 2, desiredWidth, (float)desiredHeight / 2, paint[1]);

        for(int i = 0; i < list1.lists.size(); i++) {
            if(i != 0) {
                canvas.drawLine((list1.maxItem - list1.lists.size() + i - 1) * ((float)desiredWidth / list1.maxItem), (level - pre1) * ((float)desiredHeight / (2 * level)), (list1.maxItem - list1.lists.size() + i) * (desiredWidth / list1.maxItem), (level - list1.lists.get(i)) * ((float)desiredHeight / (2 * level)), paint[2]);
                canvas.drawLine((list2.maxItem - list2.lists.size() + i - 1) * ((float)desiredWidth / list2.maxItem), (level - pre2) * ((float)desiredHeight / (2 * level)), (list2.maxItem - list2.lists.size() + i) * (desiredWidth / list2.maxItem), (level - list2.lists.get(i)) * ((float)desiredHeight / (2 * level)), paint[3]);
            }

            canvas.drawCircle((list1.maxItem - list1.lists.size() + i) * ((float)desiredWidth / list1.maxItem), (level - list1.lists.get(i)) * ((float)desiredHeight / (2 * level)), 6, paint[2]);
            canvas.drawCircle((list2.maxItem - list2.lists.size() + i) * ((float)desiredWidth / list1.maxItem), (level - list2.lists.get(i)) * ((float)desiredHeight / (2 * level)), 6, paint[3]);
            canvas.drawText(String.valueOf(list1.lists.get(i)), (list1.maxItem - list1.lists.size() + i) * ((float)desiredWidth / list1.maxItem), (level - list1.lists.get(i)) * ((float)desiredHeight / (2 * level)) - textSize / 2, paint[0]);
            canvas.drawText(String.valueOf(list2.lists.get(i)), (list2.maxItem - list2.lists.size() + i) * ((float)desiredWidth / list1.maxItem), (level - list2.lists.get(i)) * ((float)desiredHeight / (2 * level)) - textSize / 2, paint[0]);
            pre1 = list1.lists.get(i);
            pre2 = list2.lists.get(i);
        }
        
        canvas.drawText(tag, textSize / 6, textSize, paint[0]);
    }
    
    @Override
	public boolean onTouchEvent(MotionEvent event) {
    	int action = event.getAction();
    	switch(action) {
    	case MotionEvent.ACTION_DOWN:
    		Toast.makeText(getContext(), tag, Toast.LENGTH_SHORT).show();
    		isClicked = !isClicked;
    		break;
    	}
    	
    	return true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;

        //Measure Width
        if (widthMode == MeasureSpec.EXACTLY) {
            //Must be this size
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            //Can't be bigger than...
            width = Math.min(desiredWidth, widthSize);
        } else {
            //Be whatever you want
            width = desiredWidth;
        }

        //Measure Height
        if (heightMode == MeasureSpec.EXACTLY) {
            //Must be this size
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            //Can't be bigger than...
            height = Math.min(desiredHeight, heightSize);
        } else {
            //Be whatever you want
            height = desiredHeight;
        }

        //MUST CALL THIS
        setMeasuredDimension(width, height);
    }
}
