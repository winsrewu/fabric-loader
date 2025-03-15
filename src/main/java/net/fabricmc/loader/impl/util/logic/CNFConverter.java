package net.fabricmc.loader.impl.util.logic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CNFConverter<T> {
	private int nextVar = 1;
	private final Map<T, Integer> variableMap = new HashMap<>();
	private final Map<Integer, T> reverseVariableMap = new HashMap<>();
	private final List<List<Integer>> clauses = new ArrayList<>();
	private final int rootVar;

	public CNFConverter(LogicNode<T> root) {
		rootVar = convertNode(root);
		variableMap.forEach((k, v) -> reverseVariableMap.put(v, k));
	}

	private int convertNode(LogicNode<T> node) {
		if (node.getChildrens().isEmpty()) {
			return getOrCreateVariable(node.getValue());
		}

		int auxVar = nextVar++;
		LogicType type = node.getType();
		List<Integer> childVars = new ArrayList<>();

		for (LogicNode<T> child : node.getChildrens()) {
			childVars.add(convertNode(child));
		}

		switch (type) {
		case AND:
			// v → (c1 ∧ c2 ∧ ...)
			for (int c : childVars) {
				addClause(-auxVar, c);
			}

			// (c1 ∧ c2 ∧ ...) → v
			List<Integer> andClause = new ArrayList<>();
			for (int c : childVars) andClause.add(-c);
			andClause.add(auxVar);
			addClause(andClause);
			break;
		case OR:
			// v → (c1 ∨ c2 ∨ ...)
			List<Integer> orClause = new ArrayList<>();
			orClause.add(-auxVar);
			orClause.addAll(childVars);
			addClause(orClause);

			// (c1 ∨ c2 ∨ ...) → v
			for (int c : childVars) {
				addClause(-c, auxVar);
			}

			break;
		case NOT:
			if (childVars.size() != 1) {
				throw new IllegalArgumentException("NOT node must have exactly one child");
			}

			int c = childVars.get(0);
			// v ↔ ¬c
			addClause(-auxVar, -c);
			addClause(auxVar, c);
			break;
		default:
			throw new UnsupportedOperationException("Unsupported logic type: " + type);
		}

		return auxVar;
	}

	private int getOrCreateVariable(T value) {
		return variableMap.computeIfAbsent(value, k -> nextVar++);
	}

	private void addClause(Integer... literals) {
		clauses.add(Arrays.asList(literals));
	}

	private void addClause(List<Integer> clause) {
		clauses.add(new ArrayList<>(clause));
	}

	public Map<T, Integer> getVariableMap() {
		return variableMap;
	}

	public Map<Integer, T> getReverseVariableMap() {
		return reverseVariableMap;
	}

	public List<List<Integer>> getClauses() {
		return clauses;
	}

	public int getRootVar() {
		return rootVar;
	}
}
