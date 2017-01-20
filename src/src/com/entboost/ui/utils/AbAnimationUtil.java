package com.entboost.ui.utils;

import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;

/**
 * ����������
 * 
 * @author 12120153 wujj
 * 
 */
public class AbAnimationUtil
{
    /**
     * ���嶯����ʱ��
     */
    public final static long aniDurationMillis = 1L;

    /**
     * �����ı䵱ǰѡ������ķŴ󶯻�Ч��
     * 
     * @param obj
     * @param listener
     * @author 12120153 wujj
     */
    public static void changBigView(View obj, float saclePix)
    {
        if (obj == null)
            return;

        // ��1.0f�Ŵ�1.2f����
        obj.bringToFront();// ��������view���ϲ�
        int width = obj.getWidth();
        float animationSize = 1 + saclePix / width;
        changeView(obj, animationSize);
    }

    /**
     * ������ԭ��ǰѡ������Ļ�ԭ����Ч��
     * 
     * @param obj
     * @param listener
     * @author 12120153 wujj
     */
    public static void changOldView(View obj, float saclePix)
    {
        if (obj == null)
            return;
        int width = obj.getWidth();
        float animationSize = 1 + saclePix / width;
        // ��1.2f��С1.0f����
        changeView(obj, -1 * animationSize);
    }

    /**
     * ����View����ʾ
     * 
     * @param view
     *            ��Ҫ�ı��View
     * @param aniSize
     *            ���ŵĴ�С��������ֵ���Ŵ󣬸�ֵ�����С����ֵ������ŵı���
     * @param listener
     * @author 12120153 wujj
     */
    private static void changeView(final View view, float aniSize)
    {
        ScaleAnimation scale = null;
        if (aniSize == 0)
        {
            return;
        }
        else if (aniSize > 0)
        {
            scale = new ScaleAnimation(1.0f, aniSize, 1.0f, aniSize,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f);
        }
        else
        {
            scale = new ScaleAnimation(aniSize * (-1), 1.0f, aniSize * (-1),
                    1.0f, Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f);
        }
        scale.setDuration(aniDurationMillis);
        scale.setInterpolator(new AccelerateDecelerateInterpolator());
        scale.setFillAfter(true);
        view.startAnimation(scale);
    }

}
