package se.lth.sep;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Iterator;
import java.util.TreeSet;

/**
   This is an implementation of the parsing algorithm described by
   Elizabeth Scott in the paper "SPPF-Style Parsing From Earley Recognisers",
   https://doi.org/10.1016/j.entcs.2008.03.044 .
 */
public class EarleyParser {
	private boolean DEBUG = false;

	private ArrayList<TreeSet<EarleyRule>> rules;
	private Grammar grammar;

	public EarleyParser(Grammar g) {
		this.grammar = g;
		rules = g.getInternalRules();
	}

	static boolean isTerminal(int cat) {
		return cat < 0;
	}

	public String toString() {
		return grammar.toString();
	}

	static class StateSet extends LinkedHashSet<EarleyItem> {
		public StateSet(Collection<EarleyItem> c) {
			super(c);
		}

		public StateSet() {
			super();
		}

		public EarleyItem pickOne() {
			Iterator<EarleyItem> it = iterator();
			if (it.hasNext()) {
				EarleyItem item = it.next();
				it.remove();
				return item;
			}
			return null;
		}
	}

	public boolean recognize(Category s[], Category startSymbol) {
		int[] symbols = new int[s.length + 1];
		for (int i = 0; i < s.length; ++i)
			symbols[i] = grammar.getInternalSymbol(s[i]);
		symbols[s.length] = 0;
		int start = grammar.getInternalSymbol(startSymbol);

		StateSet[] state = internalParseScott(symbols, start);

		if (DEBUG) {
			for (int i = 0; i < s.length + 1; ++i) {
				System.out.println("=== Item set at position " + i + " ===");
				for (EarleyItem item : state[i]) {
					System.out.println(item.prettyPrint(grammar));
				}
			}
		}

		StateSet finalState = state[s.length];
		if (DEBUG)
			System.out.println("===========================");

		for (EarleyItem item : finalState) {
			if (item.isComplete() && item.start == 0 && item.rule.r.head == start) {
				return true;
			}
		}

		return false;
	}

	public SPPFNode parse(Category s[], Category startSymbol) {
		int[] symbols = new int[s.length + 1];
		for (int i = 0; i < s.length; ++i)
			symbols[i] = grammar.getInternalSymbol(s[i]);
		symbols[s.length] = 0;
		int start = grammar.getInternalSymbol(startSymbol);

		StateSet[] state = internalParseScott(symbols, start);

		if (DEBUG) {
			for (int i = 0; i < s.length + 1; ++i) {
				System.out.println("=== Item set at position " + i + " ===");
				for (EarleyItem item : state[i]) {
					System.out.println(item.prettyPrint(grammar));
				}
			}
			System.out.println("===========================");
		}

		StateSet finalState = state[s.length];

		for (EarleyItem item : finalState) {
			if (item.isComplete() && item.start == 0 && item.rule.r.head == start) {
				return item.getSPPF();
			}
		}

		return null;
	}

	/**
	   @param symbols - a zero terminated array of symbols
	   @param startSymbol - the start symbol for the grammar
	   @return the state set computed by the Earley parsing algorithm
	*/
	private StateSet[] internalParseScott(int[] symbols, int startSymbol) {
		StateSet[] state = new StateSet[symbols.length + 1];
		for (int i = 0; i < state.length; ++i)
			state[i] = new StateSet();

		StateSet Q_next = new StateSet();
		HashMap<NodeLabel, SPPFNode> V = new HashMap<>();

		for (EarleyRule r : rules.get(startSymbol)) {
			if (r.startsWithNonTerminal())
				state[0].add(new EarleyItem(r, 0));
			if (!r.isEmpty() && r.body[0] == symbols[0]) {
				Q_next.add(new EarleyItem(r, 0));
			}
		}

		for (int i = 0; i < symbols.length; ++i) {
			HashMap<Integer, SPPFNode> H = new HashMap<>();
			StateSet R = new StateSet(state[i]); // worklist
			StateSet Q = Q_next;
			Q_next = new StateSet();

			while (!R.isEmpty()) {
				// for hash sets the iteration order is not deterministic, this may be a problem...
				EarleyItem Lambda = R.pickOne();
				if (!Lambda.isComplete() && !isTerminal(Lambda.afterDot())) { // 1
					for (EarleyRule r : rules.get(Lambda.afterDot())) { // 1.1
						EarleyItem C = new EarleyItem(r, i);
						if (r.startsWithNonTerminal()) { // 1.1.1
							if (state[i].add(C)) {
								R.add(C);
							}
						} else if (r.body[0] == symbols[i]) { // 1.1.2
							assert !r.startsWithNonTerminal();
							Q.add(C);
						}
					}

					SPPFNode v = H.get(Lambda.afterDot());
					if (v != null) { // 1.2
						EarleyItem LambdaNext = Lambda.advance();
						SPPFNode y = makeNode(LambdaNext.getDottedRule(), LambdaNext.start, i, Lambda.getSPPF(), v, V);
						LambdaNext.setSPPF(y);
						if (LambdaNext.isComplete() || !isTerminal(LambdaNext.afterDot())) { // 1.2.1
							if (state[i].add(LambdaNext)) { // 1.2.1
								R.add(LambdaNext);
							}
						} else if (LambdaNext.afterDot() == symbols[i]) { // 1.2.2
							Q.add(LambdaNext);
						}
					}
				}

				if (Lambda.isComplete()) { // 2
					if (Lambda.getSPPF() == null) { // 2.1
						NodeLabel vLabel = new SymbolLabel(Lambda.rule.r.head, i, i);
						SPPFNode v;
						if (V.containsKey(vLabel)) { // 2.1.1
							v = V.get(vLabel);
						} else {
							v = new SPPFNode(vLabel);
							V.put(vLabel, v);
						}
						Lambda.setSPPF(v);
						v.addEpsilon(); // 2.1.2
					}
					if (Lambda.start == i) { // 2.2
						assert Lambda.getSPPF() != null;
						H.put(Lambda.rule.r.head, Lambda.getSPPF());
					}

					// this set is needed to avoid concurrent modification which occurs when Lambda.start == i
					LinkedHashSet<EarleyItem> RTemp = new LinkedHashSet<>();
					for (EarleyItem item : state[Lambda.start]) { // 2.3
						if (!item.isComplete() && item.afterDot() == Lambda.rule.r.head) {
							EarleyItem itemNext = item.advance();
							SPPFNode y = makeNode(itemNext.getDottedRule(), itemNext.start, i, item.getSPPF(), Lambda.getSPPF(), V);
							itemNext.setSPPF(y);
							if (itemNext.isComplete() || !isTerminal(itemNext.afterDot())) { // 2.3.1
								if (!state[i].contains(itemNext)) { // 2.3.1
									RTemp.add(itemNext);
								}
							} else if (itemNext.afterDot() == symbols[i]) { // 2.3.2
								Q.add(itemNext);
							}
						}
					}
					R.addAll(RTemp);
					state[i].addAll(RTemp);
				}
			}

			V.clear();

			SPPFNode v = new SPPFNode(new SymbolLabel(symbols[i], i, i + 1));
			while (!Q.isEmpty()) { // 3
				EarleyItem Lambda = Q.pickOne();
				assert Lambda.afterDot() == symbols[i];
				EarleyItem LambdaNext = Lambda.advance();
				SPPFNode y = makeNode(LambdaNext.getDottedRule(), LambdaNext.start, i + 1, Lambda.getSPPF(), v, V);
				// EarleyItem newItem = new EarleyItem(LambdaNext.rule, LambdaNext.start);
				LambdaNext.setSPPF(y);
				if (LambdaNext.isComplete() || !isTerminal(LambdaNext.afterDot())) { // 3.1
					state[i + 1].add(LambdaNext);
				} else if (LambdaNext.afterDot() == symbols[i + 1]) { // 3.2
					Q_next.add(LambdaNext);
				}
			}
		}

		return state;
	}

	private SPPFNode makeNode(DottedRule dottedRule, int j, int i, SPPFNode w, SPPFNode v,
							  HashMap<NodeLabel, SPPFNode> V) {
		NodeLabel s;
		if (dottedRule.isComplete()) {
			s = new SymbolLabel(dottedRule.r.head, j, i);
		} else {
			s = new ItemLabel(dottedRule, j, i);
		}

		if (!dottedRule.isComplete() && dottedRule.dot == 1) {
			return v;
		} else {
			SPPFNode y = V.get(s);
			if (y == null) {
				y = new SPPFNode(s);
				V.put(s, y);
			}

			if (w == null) {
				y.addChild(v);
			} else {
				y.addChildren(w, v);
			}

			return y;
		}
	}
}
