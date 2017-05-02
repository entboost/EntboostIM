package com.entboost.im.comparator;

import java.util.Comparator;

import net.yunim.service.entity.DepartmentInfo;

/**
 * 部门比较器(用于排序)
 *
 */
public class DepartmentInfoComparator implements Comparator<DepartmentInfo> {
	
//	private static String LONG_TAG = DepartmentInfoComparator.class.getName();

	@Override
	public int compare(DepartmentInfo lhs, DepartmentInfo rhs) {
		//按类型
		if (lhs.getType()==rhs.getType()) {
			//按排序编号
			if (lhs.getDisplay_index()==rhs.getDisplay_index()) {
				//按部门名称
				return ComparatorUtils.strcmpUsingGBK(lhs.getDep_name(), rhs.getDep_name());
			}
			
			return rhs.getDisplay_index() - lhs.getDisplay_index();
		}
		
		return lhs.getType() - rhs.getType();
	}

}
