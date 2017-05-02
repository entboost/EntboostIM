package com.entboost.im.comparator;

import java.util.Comparator;

import net.yunim.service.entity.ContactInfo;

/**
 * 联系人比较器(用于排序)
 */
public class ContactInfoComparator implements Comparator<ContactInfo> {

	@Override
	public int compare(ContactInfo lhs, ContactInfo rhs) {
		if (lhs.getName() != null && rhs.getName() != null) {
			if (Integer.valueOf(lhs.getState()).compareTo(rhs.getState()) == 0) {
				return ComparatorUtils.strcmpUsingGBK(lhs.getName(), rhs.getName());
//				if(Integer.valueOf(lhs.getType()).compareTo(rhs.getType()) == 0){
//					return ComparatorUtils.strcmpUsingGBK(lhs.getName(), rhs.getName());
//				} else {
//					return Integer.valueOf(lhs.getType()).compareTo(rhs.getType());
//				}
			} else {
				return Integer.valueOf(rhs.getState()).compareTo(lhs.getState());
			}
		}
		
		if (lhs.getName() != null)
			return -1;
		return 1;
	}

}
