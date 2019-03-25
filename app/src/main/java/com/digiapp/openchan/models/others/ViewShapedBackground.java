package com.digiapp.openchan.models.others;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.digiapp.openchan.AppObj;
import com.digiapp.openchan.R;

public class ViewShapedBackground extends RelativeLayout {
    public ViewShapedBackground(Context context) {
        super(context);
    }

    public ViewShapedBackground(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ViewShapedBackground(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @SuppressLint("NewApi")
    public ViewShapedBackground(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int width = this.getWidth();
        int height = this.getHeight();
        int center = -100;

        Resources resources = getResources();
        // drawing circles
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(resources.getColor(R.color.color1));
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        canvas.drawCircle(width/2, center,width,paint);

        Paint paint2 = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint2.setColor(resources.getColor(R.color.color2));
        paint2.setStyle(Paint.Style.FILL);
        paint2.setAntiAlias(true);
        canvas.drawCircle(width/2, center,width/2+350,paint2);

        Paint paint3 = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint3.setColor(resources.getColor(R.color.color3));
        paint3.setStyle(Paint.Style.FILL);
        paint3.setAntiAlias(true);
        canvas.drawCircle(width/2, center,width/2+200,paint3);

        Paint paint4 = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint4.setColor(resources.getColor(R.color.color4));
        paint4.setStyle(Paint.Style.FILL);
        paint4.setAntiAlias(true);
        canvas.drawCircle(width/2, center,width/2+50,paint4);

        Paint paint5 = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint5.setColor(resources.getColor(R.color.color5));
        paint5.setStyle(Paint.Style.FILL);
        paint5.setAntiAlias(true);
        canvas.drawCircle(width/2, center,width/2-100,paint5);

        // drawing clouds
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            Bitmap bitmap = BitmapFactory.decodeResource(resources,R.drawable.cloud_1);
            canvas.drawBitmap(bitmap,0,200,new Paint());

            Bitmap bitmap2 = BitmapFactory.decodeResource(resources,R.drawable.cloud_2);
            canvas.drawBitmap(bitmap2,width-bitmap2.getWidth(),250,new Paint());
        }

        super.onDraw(canvas);
    }
}
