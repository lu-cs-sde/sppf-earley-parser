import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

public class EarleyParser {
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

	public String toString() {
		String s = "";
		for (int i = 1; i < rules.size(); ++i) {
			TreeSet<EarleyRule> rs = rules.get(i);
			for (EarleyRule r : rs) {
				s += int2cat.get(i).toString() + " -> ";
				assert r.head == i;
				for (int j : r.body) {
					s += int2cat.get(j).toString() + " ";
				}
				s += "\n";
			}
		}
		return s;
	}

	public void parse(Category s[]) {

	}
}
