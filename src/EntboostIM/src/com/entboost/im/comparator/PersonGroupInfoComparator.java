package com.entboost.im.comparator;

import java.util.Comparator;

import net.yunim.service.entity.PersonGroupInfo;

/**
 * 群组比较器(用于排序)
 *
 */
public class PersonGroupInfoComparator implements Comparator<PersonGroupInfo> {

//	private static String LONG_TAG = PersonGroupInfoComparator.class.getName();
	
	@Override
	public int compare(PersonGroupInfo lhs, PersonGroupInfo rhs) {
		//按类型
		if (lhs.getType()==rhs.getType()) {
			//按排序编号
			if (lhs.getDisplay_index()==rhs.getDisplay_index()) {
				//按群组名称
				return ComparatorUtils.strcmpUsingGBK(lhs.getDep_name(), rhs.getDep_name());
			}
			
			return rhs.getDisplay_index() - lhs.getDisplay_index();
		}
		
		return lhs.getType() - rhs.getType();
	}
}
