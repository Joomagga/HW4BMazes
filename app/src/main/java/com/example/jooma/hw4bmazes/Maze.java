package com.example.jooma.hw4bmazes;

import java.util.ArrayList;
import android.annotation.SuppressLint;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewManager;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

/**
 * Created by jooma on 2016-06-03.
 */
public class Maze {

// Members for View
    private MazeView maze_view;
    private Activity activity;
    private Context context;
    private TableLayout table;
    private int block_width;    // width of each block. equals screen_width / number_column.
    private int block_height;   // height of each block. equals screen_height / number_row.
    private int screen_width;   // width of device screen.
    private int screen_height;  // height of device screen.
    private int number_column;  // number of blocks for a row.
    private int number_row;  // number of blocks for a column.

// Members for Maze
    private boolean[][] maze;   // true is road, false is block.

// Constructor Methods
    private void init()
    {
        this.context = activity.getApplicationContext();
        this.table = (TableLayout)activity.findViewById(R.id.table);
        this.maze_view = new MazeView(context);
        this.block_width = (int)(screen_width / number_column);
        this.block_height = (int)(screen_height / number_row);
        this.maze = new boolean[block_width][block_height];
        activity.setContentView(getView());
        maze[0][1] = true;
        maze[1][2] = true;
        maze[0][2] = true;
        maze[1][3] = true;
        maze[3][1] = true;
        setMaze();
    }

    public Maze(Activity activity, int screen_width, int screen_height, int number_column, int number_row)
    {
        this.activity = activity;
        this.screen_width = screen_width;
        this.screen_height = screen_height;
        this.number_column = number_column;
        this.number_row = number_row;
        this.init();
    }

// Getter Methods
    public MazeView getView()
    {
        return this.maze_view;
    }


// Generic Methods
    private void setMaze()
    {
        for (int i=0; i<number_column; i++)
            for (int j=0; j<number_row; j++)
            {
                TableRow tr = new TableRow(context);
                tr.setMinimumHeight(block_height);
                table.addView(tr);
                if (maze[i][j]) {
                    ImageView iv = new ImageView(context);
                    iv.setImageDrawable(activity.getResources().getDrawable(R.drawable.road));
                    iv.setMinimumWidth(block_width);
                    iv.setMaxWidth(block_width);
                    iv.setMaxHeight(block_height);
                    tr.addView(iv);
                }
                else
                {
                    ImageView iv = new ImageView(context);
                    iv.setImageDrawable(activity.getResources().getDrawable(R.drawable.block));
                    iv.setMinimumWidth(block_width);
                    iv.setMaxWidth(block_width);
                    iv.setMaxHeight(block_height);
                    tr.addView(iv);
                }
            }
    }

// Inner Class
    private class MazeView extends SurfaceView implements SurfaceHolder.Callback
    {
        Canvas cacheCanvas;
        Bitmap backBuffer;
        int width, height, clientHeight;
        Paint paint;
        Context context;
        SurfaceHolder mHolder;

        public MazeView(Context context) {
            super(context);
            this.context = context;
            init();
        }
        public MazeView(Context context, AttributeSet attrs)
        {
            super(context, attrs);
            this.context = context;
            init();
         }

        private void init()
        {
            mHolder = getHolder();
            mHolder.addCallback(this);
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
        {
        }

        public void surfaceCreated(SurfaceHolder holder)
        {
            width = getWidth();
            height = getHeight();
            cacheCanvas = new Canvas();
            backBuffer = Bitmap.createBitmap(width, height,Bitmap.Config.ARGB_8888);
            cacheCanvas.setBitmap(backBuffer);
            cacheCanvas.drawColor(Color.TRANSPARENT); //Because of bottom of table layout
            paint = new Paint(); //Set paint
            paint.setColor(Color.BLUE);
            paint.setStrokeWidth(7);
            // paint.setStrokeCap(Paint.Cap.ROUND);
            // paint.setStrokeJoin(Paint.Join.ROUND);
            draw();
        }

        public void surfaceDestroyed(SurfaceHolder holder)
        {
        }

        int lastX, lastY, currX, currY;
        boolean isDeleting;
        @Override
        public boolean onTouchEvent(MotionEvent event) {
            super.onTouchEvent(event);
            int action = event.getAction();
            switch(action & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    lastX = (int) event.getX();
                    lastY = (int) event.getY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    if(isDeleting) break;
                    currX = (int) event.getX();
                    currY = (int) event.getY();
                    cacheCanvas.drawLine(lastX, lastY, currX, currY, paint);
                    lastX = currX;
                    lastY = currY;
                    break;
                case MotionEvent.ACTION_UP:
                    if(isDeleting) isDeleting = false;
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    cacheCanvas.drawColor(Color.TRANSPARENT);
                    isDeleting = true;
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                    break;
            }
            draw(); // SurfaceView에 그리는 function을 직접 제작 및 호출
            return true;
        }
        protected void draw() {
            if(clientHeight==0)
            {
                clientHeight = getClientHeight();
                height = clientHeight;
                backBuffer = Bitmap.createBitmap( width, height, Bitmap.Config.ARGB_8888);
                cacheCanvas.setBitmap(backBuffer);
                cacheCanvas.drawColor(Color.TRANSPARENT);
            }

        // Double Buffering
            Canvas canvas = null;
            try{
                canvas = mHolder.lockCanvas(null);
                canvas.drawBitmap(backBuffer, 0,0, paint);
            }catch(Exception ex){
                ex.printStackTrace();
            }finally{
                if(mHolder!=null) mHolder.unlockCanvasAndPost(canvas);
            }
        }
        // get height of device screen.
        private int getClientHeight() {
            Rect rect= new Rect();
            Window window = activity.getWindow();
            window.getDecorView().getWindowVisibleDisplayFrame(rect);
            int statusBarHeight= rect.top;
            int contentViewTop= window.findViewById(Window.ID_ANDROID_CONTENT).getTop();
            int titleBarHeight= contentViewTop - statusBarHeight;
            return activity.getWindowManager().getDefaultDisplay().getHeight() - statusBarHeight - titleBarHeight;
        }
    } // class DrawingSurface
}
