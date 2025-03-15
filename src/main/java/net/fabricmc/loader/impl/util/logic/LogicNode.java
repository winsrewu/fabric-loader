package net.fabricmc.loader.impl.util.logic;

import java.util.ArrayList;
import java.util.List;

public class LogicNode<T> {
	private final LogicType type;
	private final T value;
	private final List<LogicNode<T>> childrens;

	public LogicNode(LogicType type, T value, List<LogicNode<T>> childrens) {
		this.type = type;
		this.value = value;
		this.childrens = childrens == null ? new ArrayList<>() : childrens;
	}

	public LogicType getType() {
		return type;
	}

	public T getValue() {
		return value;
	}

	public List<LogicNode<T>> getChildrens() {
		return childrens;
	}
}
