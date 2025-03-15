package net.fabricmc.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sat4j.pb.IPBSolver;
import org.sat4j.pb.SolverFactory;
import org.sat4j.pb.tools.DependencyHelper;
import org.sat4j.pb.tools.INegator;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.TimeoutException;

import net.fabricmc.loader.impl.util.logic.CNFConverter;
import net.fabricmc.loader.impl.util.logic.LogicNode;
import net.fabricmc.loader.impl.util.logic.LogicPrinter;
import net.fabricmc.loader.impl.util.logic.LogicType;

interface Mod {
	String name = "";
}

class TmpModGetter {
	private static Map<String, TmpMod> tmpModMap = new HashMap<>();

	static TmpMod getTmpMod(int identifier, int innerId) {
		String name = "TMP @ " + identifier + " " + innerId;
		if (tmpModMap.containsKey(name)) {
			return tmpModMap.get(name);
		} else {
			TmpMod tmp = new TmpMod(identifier, innerId);
			tmpModMap.put(name, tmp);
			return tmp;
		}
	}
}

class TmpMod implements Mod {
	String name;

	TmpMod(int identifier, int innerId) {
		name = "TMP @ " + identifier + " " + innerId;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof TmpMod && ((TmpMod) obj).name.equals(name);
	}

	@Override
	public String toString() {
		return name;
	}
}

class DummyMod implements Mod {
	public String name;

	DummyMod(String name) {
		this.name = name;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof DummyMod && ((DummyMod) obj).name.equals(name);
	}

	@Override
	public String toString() {
		return name;
	}
}

class NegatedMod implements Mod {
	final Mod obj;

	NegatedMod(Mod mod) {
		this.obj = mod;
	}

	@Override
	public String toString() {
		return "!" + name;
	}
}

public class LogicTest {
	@Test
	@DisplayName("Logic Test")
	public void logicTest() throws ContradictionException, TimeoutException {
		Mod _modA = new DummyMod("modA");
		Mod _modB = new DummyMod("modB");
		Mod _modC = new DummyMod("modC");
		Mod _modE = new DummyMod("modE");

		// Create a logic tree: modA AND (!modB or modC)
		LogicNode<Mod> modA = new LogicNode<>(LogicType.LITERAL, _modA, null);
		LogicNode<Mod> modB = new LogicNode<>(LogicType.LITERAL, _modB, null);
		LogicNode<Mod> modC = new LogicNode<>(LogicType.LITERAL, _modC, null);
		LogicNode<Mod> root = new LogicNode<>(LogicType.AND, null, createList(modA, new LogicNode<>(LogicType.OR, null, createList(new LogicNode<>(LogicType.NOT, null, createList(modB)), modC))));

		// Print the logic tree and its CNF form
		System.out.println(LogicPrinter.print(root));

		CNFConverter<Mod> converter = new CNFConverter<>(root);
		List<List<Integer>> cnf = converter.getClauses();
		System.out.println(cnf);

		IPBSolver solver = SolverFactory.newDefaultOptimizer();
		solver.setTimeout(3600);
		DependencyHelper<Mod, String> helper = new DependencyHelper<>(solver);
		helper.setNegator(new INegator() {
			@Override
			public boolean isNegated(Object thing) {
				return thing instanceof NegatedMod;
			}

			@Override
			public Object unNegate(Object thing) {
				return ((NegatedMod) thing).obj;
			}
		});
		helper.implication(_modA).implies(_modB, _modC).named("HARD DEP!");
		helper.setTrue(_modA, "HARD LOAD!");
		helper.setTrue(_modE, "HARD LOAD!");

		for (List<Integer> clause : converter.getClauses()) {
			List<Mod> objs = new ArrayList<>();

			for (int literal : clause) {
				if (literal > 0) {
					if (converter.getReverseVariableMap().containsKey(literal)) {
						objs.add(converter.getReverseVariableMap().get(literal));
					} else {
						objs.add(TmpModGetter.getTmpMod(0, literal));
					}
				} else {
					if (converter.getReverseVariableMap().containsKey(literal)) {
						objs.add(new NegatedMod(converter.getReverseVariableMap().get(-literal)));
					} else {
						objs.add(new NegatedMod(TmpModGetter.getTmpMod(0, -literal)));
					}
				}
			}

			helper.clause("TMP_VAR", objs.toArray(new Mod[0]));
		}

		helper.iff("CONDITIONAL DEP", _modE, TmpModGetter.getTmpMod(0, 1));

		if (helper.hasASolution()) {
			System.out.println(helper.getASolution());
		} else {
			System.out.println(helper.why());
		}
	}

	@SafeVarargs
	private static <T> List<T> createList(T... elements) {
		return new ArrayList<>(Arrays.asList(elements));
	}
}
