package com.entboost.im.comparator;

import java.util.Comparator;

import net.yunim.service.entity.FuncInfo;

/**
 * 应用功能列表比较器(用于排序)
 */
public class FuncInfoComparator implements Comparator<FuncInfo> {

	@Override
	public int compare(FuncInfo lhs, FuncInfo rhs) {
		if (lhs.getDisplay_index()==rhs.getDisplay_index()) {
			return ComparatorUtils.strcmpUsingGBK(lhs.getFunc_name(), rhs.getFunc_name());
		}
		
		return Integer.valueOf(lhs.getDisplay_index()).compareTo(Integer.valueOf(rhs.getDisplay_index()));
	}

}
