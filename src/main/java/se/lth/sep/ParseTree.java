package se.lth.sep;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ParseTree {
	private int start;
	private int end;
	private Category cat;
	private Rule rule;
	private ArrayList<ParseTree> children;

	private ParseTree(Category c, Rule rule, int start, int end) {
		this.cat = c;
		this.children = new ArrayList<ParseTree>();
		this.start = start;
		this.end = end;
		this.rule = rule;
		assert rule == null || rule.getHead() == c;
	}

	public ParseTree(Rule r, int start, int end) {
		this(r.getHead(), r, start, end);
	}

	public ParseTree(Category c, int start, int end) {
		this(c, null, start, end);
	}

	public void addChild(ParseTree t) {
		children.add(t);
	}

	public void addChildren(Collection<ParseTree> ts) {
		children.addAll(ts);
	}

	public List<ParseTree> getChildren() {
		return children;
	}

	public Category getCategory() {
		return cat;
	}

	public int getStart() {
		return start;
	}

	public int getEnd() {
		return end;
	}

	public Rule getRule() {
		return rule;
	}
}
