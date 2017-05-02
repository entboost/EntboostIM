package com.entboost.im.comparator;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

import net.yunim.service.entity.Dict;

/**
 * 地区字典比较器(用于排序)
 */
public class DictComparator implements Comparator<Dict> {

	private Collator instance = Collator.getInstance(Locale.CHINA);
	
	@Override
	public int compare(Dict lhs, Dict rhs) {
		return instance.compare(lhs.getDict_name(), rhs.getDict_name());
	}

}
