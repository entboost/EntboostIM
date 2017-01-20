package com.entboost.im.group;

public interface SelectedMemberListener {

	/**
	 * 多选模式下，选中某个(某些)成员
	 */
	public void onSelectedMembersChange();
	
	/**
	 * 单选模式下，选中某个成员
	 */
	public void onClickOneMember();
}
