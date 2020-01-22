import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
import java.util.HashSet;
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
}
