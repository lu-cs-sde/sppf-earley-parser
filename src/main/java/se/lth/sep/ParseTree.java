package se.lth.sep;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ParseTree {
	private int start;
	private int end;
	private Category cat;
	private ArrayList<ParseTree> children;

	public ParseTree(Category c, int start, int end) {
		this.cat = c;
		this.children = new ArrayList<ParseTree>();
		this.start = start;
		this.end = end;
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
}
