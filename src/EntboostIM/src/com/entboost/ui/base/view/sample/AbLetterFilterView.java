package com.entboost.ui.base.view.sample;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.HeaderViewListAdapter;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.entboost.ui.utils.AbViewUtil;

/**
 * 字母索引条
 */

public class AbLetterFilterView extends View {
    private char[] l;
    private SectionIndexer sectionIndexter = null;
    private ListView list;
    private TextView mDialogText;
    private Paint paint;
    private int backgroundResource;
    //private int bg_touch = R.drawable.im_contact_sidebar_bg_trans;
    //private int bg_leave = R.color.im_white_side;
    private float widthCenter;

    /**
     * 字母之间的间距
     */
    private float singleHeight;

    public AbLetterFilterView(Context context) {
	super(context);
	init();
    }

    public AbLetterFilterView(Context context, AttributeSet attrs) {
	super(context, attrs);
	init();
    }

    private void init() {
	l = new char[] { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K',
		'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W',
		'X', 'Y', 'Z', '#' };
	paint = new Paint();
	paint.setColor(Color.parseColor("#333333"));
	paint.setTypeface(Typeface.DEFAULT);
	paint.setTextSize(AbViewUtil.getDipSize(12));
	paint.setAntiAlias(true);
	paint.setTextAlign(Paint.Align.CENTER);

    }

    public AbLetterFilterView(Context context, AttributeSet attrs, int defStyle) {
	super(context, attrs, defStyle);
	init();
    }

    public void setListView(ListView _list) {
	list = _list;
	sectionIndexter = (SectionIndexer) _list.getAdapter();
    }

    public void setTextView(TextView mDialogText) {
	this.mDialogText = mDialogText;
    }

    public boolean onTouchEvent(MotionEvent event) {
	super.onTouchEvent(event);
	int i = (int) event.getY();
	int div = (int) singleHeight;
	int idx = 0;
	if (div != 0) {
	    idx = i / div;
	}
	if (idx >= l.length) {
	    idx = l.length - 1;
	} else if (idx < 0) {
	    idx = 0;
	}
	if (event.getAction() == MotionEvent.ACTION_DOWN
		|| event.getAction() == MotionEvent.ACTION_MOVE) {
	    
	    mDialogText.setVisibility(View.VISIBLE);
	    mDialogText.setText("" + l[idx]);
	    // 首先先将listView强制转换为HeaderViewListAdapter
	    if (list.getAdapter() != null) {
		HeaderViewListAdapter listAdapter = (HeaderViewListAdapter) list
			.getAdapter();
		if (sectionIndexter == null) {
		    sectionIndexter = (SectionIndexer) listAdapter
			    .getWrappedAdapter();
		}
		int position = sectionIndexter.getPositionForSection(l[idx]);
		if (position == -1) {

		    return true;
		}
		list.setSelection(position);
	    }
	} else {
	    mDialogText.setVisibility(View.INVISIBLE);
	}
	return true;
    }

    protected void onDraw(Canvas canvas) {
	float height = getHeight();
	singleHeight = height / l.length;
	widthCenter = getMeasuredWidth() / (float) 2;
	for (int i = 0; i < l.length; i++) {
	    canvas.drawText(String.valueOf(l[i]), widthCenter, singleHeight
		    + (i * singleHeight), paint);
	}
	super.onDraw(canvas);
    }

    public int getBackgroundResource() {
        return backgroundResource;
    }

    public void setBackgroundResource(int backgroundResource) {
        this.backgroundResource = backgroundResource;
        this.setBackgroundResource(backgroundResource);
    }
    
}
