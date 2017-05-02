package com.entboost.im.comparator;

import java.util.Comparator;

import net.yunim.service.entity.ContactGroup;

/**
 * 联系人分组比较器(用于排序)
 */
public class ContactGroupComparator implements Comparator<ContactGroup> {

	@Override
	public int compare(ContactGroup lhs, ContactGroup rhs) {
		if (lhs.getGroupname() != null && rhs.getGroupname() != null) {
			return ComparatorUtils.strcmpUsingGBK(lhs.getGroupname(), rhs.getGroupname());
		}
		
		if (lhs.getGroupname()!=null)
			return -1;
		return 1;
	}

}
