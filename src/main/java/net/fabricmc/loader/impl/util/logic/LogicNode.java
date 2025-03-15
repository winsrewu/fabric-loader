/*
 * Copyright 2016 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
