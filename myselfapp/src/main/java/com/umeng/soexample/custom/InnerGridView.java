package com.umeng.soexample.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.GridView;

public class InnerGridView extends GridView {
	public InnerGridView(Context context) {
		super(context);
	}
	public InnerGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	public InnerGridView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
				MeasureSpec.AT_MOST);
		super.onMeasure(widthMeasureSpec, expandSpec);
	}

	//通过重新dispatchTouchEvent方法来禁止滑动
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
// TODO Auto-generated method stub
		if(ev.getAction() == MotionEvent.ACTION_MOVE){
			return true;//禁止Gridview进行滑动
		}
		return super.dispatchTouchEvent(ev);
	}

}