package com.entboost.im.comparator;

import java.util.Comparator;

import net.yunim.service.entity.DynamicNews;

/**
 * 动态消息比较器(用于排序)
 */
public class DynamicNewsCompare implements Comparator<DynamicNews> {

	@Override
	public int compare(DynamicNews lhs, DynamicNews rhs) {
		if (lhs.getTime() > rhs.getTime()) {
			return -1;
		} else if (lhs.getTime() < rhs.getTime()) {
			return 1;
		} else {
			return 0;
		}
	}

}
