import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

public class EarleyParser {
	private boolean DEBUG = true;

	private HashMap<Category, Integer> cat2int;
	private HashMap<Integer, Category> int2cat;

	private int nonTermIndex = 1;
	private int termIndex = -1;

	private ArrayList<TreeSet<EarleyRule>> rules;

	private HashMap<Category, ArrayList<Rule>> grammarRules;


	public EarleyParser() {
		cat2int = new HashMap<>();
		int2cat = new HashMap<>();
		grammarRules = new HashMap<>();
	}

	// Interface to the higher level gramar
	public void addCategory(Category t) {
		if (t.isTerminal()) {
			cat2int.put(t, termIndex);
			int2cat.put(termIndex, t);
			termIndex--;
		} else {
			cat2int.put(t, nonTermIndex);
			int2cat.put(nonTermIndex, t);
			nonTermIndex++;
		}
	}

	static boolean isTerminal(int cat) {
		return cat < 0;
	}

	public void addRule(Rule r) {
		ArrayList<Rule> list = grammarRules.get(r.getHead());
		if (list == null) {
			list = new ArrayList<Rule>();
			grammarRules.put(r.getHead(), list);
		}
		list.add(r);
	}

	public void done() {
		rules = new ArrayList<>(grammarRules.size() + 1);
		rules.add(new TreeSet<EarleyRule>());
		for (int i = 1; i < grammarRules.size() + 1; ++i) {
			Category head = int2cat.get(i);
			ArrayList<Rule> bodies = grammarRules.get(head);
			TreeSet<EarleyRule> eBodies = new TreeSet<>();
			for (Rule b : bodies) {
				int[] body = new int[b.getBody().size()];
				for (int j = 0; j < body.length; ++j) {
					body[j] = cat2int.get(b.getBody().get(j));
				}
				eBodies.add(new EarleyRule(i, body));
			}
			rules.add(eBodies);
		}
	}

	private String asString(EarleyRule rule) {
		String s = int2cat.get(rule.head).toString() + " -> ";
		for (int j : rule.body) {
			s += int2cat.get(j).toString() + " ";
		}
		return s;
	}

	private String asString(EarleyItem item) {
		String s = int2cat.get(item.rule.head).toString() + " -> ";
		for (int j = 0; j < item.rule.body.length; ++j) {
			if (j == item.dot) {
				s += "\u2022 ";
			}
			int symbol = item.rule.body[j];
			s += int2cat.get(symbol).toString() + " ";
		}

		if (item.dot == item.rule.body.length) {
			s += "\u2022";
		}

		s += "(" + item.start + ")";
		return s;
	}

	public String toString() {
		String s = "";
		for (int i = 1; i < rules.size(); ++i) {
			TreeSet<EarleyRule> rs = rules.get(i);
			for (EarleyRule r : rs) {
				assert r.head == i;
				s += asString(r);
				s += "\n";
			}
		}
		return s;
	}

	class StateSet extends HashSet<EarleyItem> {
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
				// the following is optional, let's see if it works
				it.remove();
				return item;
			}
			return null;
		}
	}

	/**
	   @param symbols - a zero terminated array of symbols
	 */
	private StateSet[] internalParse(int[] symbols, int startSymbol) {
		StateSet[] state = new StateSet[symbols.length + 1];
		state[0] = new StateSet();
		for (EarleyRule r : rules.get(startSymbol)) {
			state[0].add(new EarleyItem(r, 0));
		}

		for (int i = 0; i < symbols.length; ++i) {
			StateSet currentSet = state[i];
			state[i + 1] = new StateSet();
			StateSet nextSet = state[i + 1];

			LinkedList<EarleyItem> worklist = new LinkedList<>(currentSet);

			while (!worklist.isEmpty()) {
				EarleyItem item = worklist.removeFirst();
				if (item.isComplete()) {
					// COMPLETION
					// TODO: we're iterating over items in a parent set here. This is O(n_items).
					// We can improve this by storing the set as a tree set, which would give
					// a complexity of O(log(n_items)) for this iteration and also for insertion.
					for (EarleyItem jtem : state[item.start]) {
						if (!jtem.isComplete() && jtem.afterDot() == item.rule.head) {
							EarleyItem newItem = jtem.advance();
							if (currentSet.add(newItem)) {
								worklist.addLast(newItem);
							}
						}
					}
				} else if (isTerminal(item.afterDot())) {
					// SCAN
					if (item.afterDot() == symbols[i]) {
						// we have a match, advance
						EarleyItem newItem = item.advance();
						nextSet.add(newItem);
					} else {
						// do nothing
					}
				} else {
					// PREDICTION:
					// non-terminal after dot
					for (EarleyRule r : rules.get(item.afterDot())) {
						EarleyItem newItem = new EarleyItem(r, i);
						if (currentSet.add(newItem)) {
							// the item was not existing in the set, add it to the worklist
							worklist.addLast(newItem);
						}
					}
				}
			}
		}
		return state;
	}


	public boolean recognize(Category s[], Category startSymbol) {
		int[] symbols = new int[s.length + 1];
		for (int i = 0; i < s.length; ++i)
			symbols[i] = cat2int.get(s[i]);
		symbols[s.length] = 0;
		int start = cat2int.get(startSymbol);

		StateSet[] state = internalParse(symbols, start);

		if (DEBUG) {
			for (int i = 0; i < s.length + 1; ++i) {
				System.out.println("=== Item set at position " + i + " ===");
				for (EarleyItem item : state[i]) {
					if (item.isComplete())
						System.out.println(asString(item));
				}
			}
		}

		StateSet finalState = state[s.length];
		System.out.println("===========================");
		for (EarleyItem item : finalState) {
			if (item.isComplete() && item.start == 0 && item.rule.head == start) {
				return true;
			}
		}

		return false;
	}

	public void parse(Category s[], Category startSymbol) {
		int[] symbols = new int[s.length + 1];
		for (int i = 0; i < s.length; ++i)
			symbols[i] = cat2int.get(s[i]);
		symbols[s.length] = 0;
		int start = cat2int.get(startSymbol);

		StateSet[] state = internalParse(symbols, start);
	}

	private boolean internalParseScott(int[] symbols, int startSymbol) {
		StateSet[] state = new StateSet[symbols.length + 1];
		state[0] = new StateSet();

		StateSet Q_next = new StateSet();
		HashMap<NodeLabel, SPPFNode> V = new HashMap<>();

		for (EarleyRule r : rules.get(startSymbol)) {
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
				// for hash sets this is not deterministic, this may be a problem...
				EarleyItem Lambda = R.pickOne();
				if (!Lambda.isComplete() && !isTerminal(Lambda.afterDot())) { // 1
					for (EarleyRule r : rules.get(Lambda.afterDot())) { // 1.1
						EarleyItem C = new EarleyItem(r, i);
						if (r.startsWithNonTerminal()) { // 1.1.1
							if (state[i].add(C)) {
								R.add(C);
							}
						}
						if (r.body[0] == symbols[i]) { // 1.1.2
							assert !r.startsWithNonTerminal();
							Q.add(C);
						}
					}

					SPPFNode v = H.get(Lambda.afterDot()); // TODO: check that we never insert null
					if (v != null) { // 1.2
						EarleyItem LambdaNext = Lambda.advance();
						SPPFNode y = makeNode(LambdaNext.getDottedRule(), LambdaNext.start, i, Lambda.getSPPF(), v, V);
						LambdaNext.setSPPF(y);
						if (LambdaNext.isComplete() || !isTerminal(LambdaNext.afterDot())) { // 1.2.1
							if (state[i].add(LambdaNext)) { // 1.2.1
								R.add(LambdaNext);
							}
						} else if (LambdaNext.afterDot() == symbols[i + 1]) { // 1.2.2
							Q.add(LambdaNext);
						}
					}
				}

				if (Lambda.isComplete()) { // 2
					if (Lambda.getSPPF() == null) { // 2.1
						NodeLabel vLabel = new SymbolLabel(Lambda.rule.head, i, i);
						SPPFNode v;
						if (V.containsKey(vLabel)) { // 2.1.1
							v = V.get(vLabel);
						} else {
							v = new SPPFNode(vLabel);
							V.put(vLabel, v);
						}
						Lambda.setSPPF(v);
						// TODO: if w does not have family (eps) add one? 2.1.2
					}
					if (Lambda.start == i) { // 2.2
						H.put(Lambda.rule.head, Lambda.getSPPF());
					}

					for (EarleyItem item : state[Lambda.start]) { // 2.3
						EarleyItem itemNext = item.advance();
						SPPFNode y = makeNode(itemNext.getDottedRule(), itemNext.start, i, item.getSPPF(), Lambda.getSPPF(), V);
						EarleyItem newItem = new EarleyItem(itemNext.rule, itemNext.start);
						newItem.setSPPF(y);
						if (itemNext.isComplete() || !isTerminal(itemNext.afterDot())) { // 2.3.1
							if (state[i].add(newItem)) { // 2.3.1
								R.add(newItem);
							}
						} else if (itemNext.afterDot() == symbols[i + 1]) { // 2.3.2
							Q.add(newItem);
						}
					}
				}
			}

			V.clear();

			SPPFNode v = new SPPFNode(new SymbolLabel(symbols[i + 1], i, i + 1));
			while (!Q.isEmpty()) { // 3
				EarleyItem Lambda = Q.pickOne();
				assert Lambda.afterDot() == symbols[i + 1];
				EarleyItem LambdaNext = Lambda.advance();
				SPPFNode y = makeNode(LambdaNext.getDottedRule(), LambdaNext.start, i + 1, Lambda.getSPPF(), v, V);
				EarleyItem newItem = new EarleyItem(LambdaNext.rule, LambdaNext.start);
				newItem.setSPPF(y);
				if (LambdaNext.isComplete() || !isTerminal(LambdaNext.afterDot())) { // 3.1
					state[i + 1].add(newItem);
				} else if (i + 2 < symbols.length && LambdaNext.afterDot() == symbols[i + 2]) { // 3.2
					Q_next.add(newItem);
				}
			}
		}

		StateSet finalState = state[symbols.length];
		for (EarleyItem item : finalState) {
			if (item.isComplete() && item.start == 0 && item.rule.head == startSymbol) {
				return true;
			}
		}

		return false;
	}

	private SPPFNode makeNode(DottedRule dottedRule, int start, int i, SPPFNode sppf, SPPFNode sppf2,
			HashMap<NodeLabel, SPPFNode> v) {
		return null;
	}


}
