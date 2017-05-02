package com.entboost.im.comparator;

import java.util.Comparator;

import net.yunim.service.constants.EB_MANAGER_LEVEL;
import net.yunim.service.entity.MemberInfo;

/**
 * 部门(群组)成员比较器(用于排序)
 *
 */
public class MemberInfoComparator implements Comparator<MemberInfo> {

	@Override
	public int compare(MemberInfo lhs, MemberInfo rhs) {
		//按排序编号比较
		if (lhs.getDisplay_index()==rhs.getDisplay_index()) {
			//按创建者比较
			if (lhs.isCreator()==rhs.isCreator()) {
				//按管理员比较
				boolean lHasAdmin = (lhs.getManager_level() & EB_MANAGER_LEVEL.EB_LEVEL_DEP_ADMIN.getValue()) == EB_MANAGER_LEVEL.EB_LEVEL_DEP_ADMIN.getValue(); 
				boolean rHasAdmin = (rhs.getManager_level() & EB_MANAGER_LEVEL.EB_LEVEL_DEP_ADMIN.getValue()) == EB_MANAGER_LEVEL.EB_LEVEL_DEP_ADMIN.getValue(); 
				if (lHasAdmin==rHasAdmin) {
					//按在线状态比较
					if (Integer.valueOf(lhs.getState()).compareTo(Integer.valueOf(rhs.getState())) == 0) {
						if(lhs.getUsername()!=null && rhs.getUsername()!=null){
							return ComparatorUtils.strcmpUsingGBK(lhs.getUsername(), rhs.getUsername());
						}
						if (lhs.getUsername()!=null)
							return -1;
						return 1;
					}
					
					return Integer.valueOf(rhs.getState()).compareTo(Integer.valueOf(lhs.getState()));
				}
				
				return lHasAdmin?-1:1;
			}
			
			return lhs.isCreator()?-1:1;
		}
		
		return rhs.getDisplay_index() - lhs.getDisplay_index();
	}

}
