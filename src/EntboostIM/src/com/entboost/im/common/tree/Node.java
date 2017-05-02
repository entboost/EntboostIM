package com.entboost.im.common.tree;

import java.util.Collections;
import java.util.Vector;

public class Node implements Comparable<Node>{
	private Node parent = null;// 父节点
	private Vector<Node> children = null;
	private String oid = null;// 该节点的oid
	private String name = null;// 该节点的名称
	private Object value = null;// 该节点的值
	private String desc;//节点附加描述信息
	private boolean isLeaf = false;// 是否为叶节点
	private boolean isExpanded = false;// 该节点是否展开
	private int icon = -1;// 该节点的图标对应的id
	private int iconForExpanding = -1;
	private int iconForFolding = -1;
	private boolean tableItemOrNot = false;// 表示是否为表结构的一列
	
	public Node(Node parent, String oid, String name, boolean isLeaf,
			int icon, int exIcon, int foIcon) {
		this.parent = parent;
		this.oid = oid;
		this.name = name;
		this.isLeaf = isLeaf;
		this.icon = icon;
		this.iconForExpanding = exIcon;
		this.iconForFolding = foIcon;
	}

	public Node(Node parent, String oid, String name, boolean isLeaf,
			int exIcon, int foIcon) {
		this.parent = parent;
		this.oid = oid;
		this.name = name;
		this.isLeaf = isLeaf;
		this.iconForExpanding = exIcon;
		this.iconForFolding = foIcon;
	}
	
	public Node(Node parent, String oid, String name, boolean isLeaf,
			int icon) {
		this.parent = parent;
		this.oid = oid;
		this.name = name;
		this.isLeaf = isLeaf;
		this.icon = icon;
	}
	
	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public void setParent(Node parent) {
		this.parent = parent;
	}

	public void setTableItemOrNot(boolean tableItemOrNot) {
		this.tableItemOrNot = tableItemOrNot;
	}

	public boolean getTableItemOrNot() {
		return this.tableItemOrNot;
	}

	// 设置value
	public void setValue(Object value) {
		this.value = value;
	}

	// 得到value
	public Object getValue() {
		return this.value;
	}

	// 设置图标
	public void setIcon(int icon) {
		this.icon = icon;
	}

	public int getIcon() {
		return this.icon;
	}

	// 得到description
	public String getDescription() {
		return this.name;
	}

	// 得到oid
	public String getOid() {
		return this.oid;
	}

	// 得到是否为叶节点
	public boolean isLeafOrNot() {
		return this.isLeaf;
	}
	
	public void setLeaf(boolean isLeaf) {
		this.isLeaf = isLeaf;
	}

	// 得到当前节点所在的层数，根为0层
	public int getLevel() {
		return parent == null ? 0 : parent.getLevel() + 1;
	}

	// 设置是否展开
	public void setExpanded(boolean isExpanded) {
		this.isExpanded = isExpanded;
	}

	public boolean getExpanded() {
		return this.isExpanded;
	}

	// 添加子节点
	public void addChildNode(Node child) {
		if (this.children == null) {
			this.children = new Vector<Node>();
		}
		this.children.add(child);
	}

	// 清空子节点
	public void clearChildren() {
		if (!this.children.equals(null)) {
			this.children.clear();
		}
	}

	// 是否为根节点
	public boolean isRoot() {
		return this.parent.equals(null) ? true : false;
	}

	// 设置展开图标
	public void setExpandIcon(int expand) {
		this.iconForExpanding = expand;
	}

	// 设置折叠图标
	public void setFoldIcon(int fold) {
		this.iconForFolding = fold;
	}

	// 得到展开或折叠图标
	public int getExpandOrFoldIcon() {
		if (this.isExpanded == true)
			return this.iconForExpanding;
		else
			return this.iconForFolding;
	}

	// 得到子树
	public Vector<Node> getChildren() {
		if(children!=null){
			Collections.sort(this.children);
		}
		return this.children;
	}

	@Override
	public int compareTo(Node arg0) {
		return name.compareTo(arg0.getDescription());
	}
}
