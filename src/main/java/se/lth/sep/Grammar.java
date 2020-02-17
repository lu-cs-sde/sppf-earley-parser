package se.lth.sep;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

public class Grammar {
	private HashMap<Category, Integer> cat2int = new HashMap<>();
	private HashMap<Integer, Category> int2cat = new HashMap<>();
	private HashMap<Category, ArrayList<Rule>> grammarRules = new HashMap<>();
	private HashMap<EarleyRule, Rule> earleyRule2GrammarRule = new HashMap<>();
	private HashMap<String, Category> name2cat = new HashMap<>();

	private int nonTermIndex = 1;
	private int termIndex = -1;

	ArrayList<TreeSet<EarleyRule>> rules;

	public ArrayList<TreeSet<EarleyRule>> getInternalRules() {
		// lazily initialize the rule table
		if (rules == null)
			computeInternalRules();
		return rules;
	}

	public Grammar() {

	}

	private void addCategory(Category t) {
		if (cat2int.containsKey(t))
			return;

		Category existingCat = name2cat.get(t.getName());
		if (existingCat == null) {
			name2cat.put(t.getName(), t);
		} else if (existingCat != t) {
			throw new EarleyException("Two categories with the same name in the grammar.");
		}

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

	public void addRule(Rule r) {
		addCategory(r.getHead());
		for (Category c : r.getBody())
			addCategory(c);

		ArrayList<Rule> list = grammarRules.get(r.getHead());
		if (list == null) {
			list = new ArrayList<Rule>();
			grammarRules.put(r.getHead(), list);
		}
		list.add(r);
	}

	private void computeInternalRules() {
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
				EarleyRule eRule = new EarleyRule(i, body);
				earleyRule2GrammarRule.put(eRule, b);
				eBodies.add(eRule);
			}
			rules.add(eBodies);
		}
	}

	public String toString() {
		String s = "";
		for (int i = 1; i < rules.size(); ++i) {
			TreeSet<EarleyRule> rs = rules.get(i);
			for (EarleyRule r : rs) {
				assert r.head == i;
				s += r.prettyPrint(this);
				s += "\n";
			}
		}
		return s;
	}

	public Category getCategory(int i) {
		Category c = int2cat.get(i);
		if (c == null)
			throw new EarleyException("Symbol index not present in the grammar");
		return c;
	}

	public Category getCategory(String name) {
		Category c = name2cat.get(name);
		if (c == null)
			throw new EarleyException("Category '" + name + "' not present in the grammar");
		return c;
	}

	public int getInternalSymbol(Category c) {
		Integer i = cat2int.get(c);
		if (i == null)
			throw new EarleyException("Category not present in the gramamr");
		return i;
	}

	public Rule getRule(EarleyRule rule) {
		Rule r = earleyRule2GrammarRule.get(rule);
		if (r == null)
			throw new EarleyException("Rule not preset in the grammar");
		return r;
	}

	public Rule getRule(Category head, List<Category> body) {
		List<Rule> ruleList = grammarRules.get(head);
		if (ruleList == null)
			return null;
		for (Rule r : ruleList) {
			if (r.getBody().equals(body))
				return r;
		}
		return null;
	}
}
