package com.entboost.im.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RadioGroup;

import com.entboost.im.R;
  
public class SegmentedRadioGroup extends RadioGroup {  
  
    public SegmentedRadioGroup(Context context) {  
        super(context);  
    }  
  
    public SegmentedRadioGroup(Context context, AttributeSet attrs) {  
        super(context, attrs);  
    }  
  
    @Override  
    protected void onFinishInflate() {  
        super.onFinishInflate();  
        changeButtonsImages();  
    }  
  
    private void changeButtonsImages(){  
        int count = super.getChildCount();  
        if(count > 1){  
            super.getChildAt(0).setBackgroundResource(R.drawable.segment_radio_left);  
            for(int i=1; i < count-1; i++){  
                super.getChildAt(i).setBackgroundResource(R.drawable.segment_radio_mid);  
            }  
            super.getChildAt(count-1).setBackgroundResource(R.drawable.segment_radio_right);  
        } 
    }  
}  