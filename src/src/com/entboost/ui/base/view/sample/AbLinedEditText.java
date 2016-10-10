/*
 * Copyright (C) 2015 www.amsoft.cn
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.entboost.ui.base.view.sample;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.EditText;

//TODO: Auto-generated Javadoc
/**
* 
* Copyright (c) 2012 All rights reserved
* AbLinedEditText.java
* 带下横线的EditText
* @author amsoft.cn
* @date 2013-11-12 10:09:55
* @version v1.0
*/

public  class AbLinedEditText extends EditText {
  private Rect mRect;
  private Paint mPaint;

  public AbLinedEditText(Context context, AttributeSet attrs) {
      super(context, attrs);
      mRect = new Rect();
      mPaint = new Paint();
      mPaint.setStyle(Paint.Style.STROKE);
      mPaint.setColor(0x800000FF);
  }
  
  @Override
  protected void onDraw(Canvas canvas) {
      int count = getLineCount();
      Rect r = mRect;
      Paint paint = mPaint;

      for (int i = 0; i < count; i++) {
          int baseline = getLineBounds(i, r);
          canvas.drawLine(r.left, baseline+10, r.right, baseline + 10, paint);
      }

      super.onDraw(canvas);
  }
}