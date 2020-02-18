package se.lth.sep;

import java.util.Arrays;
import java.util.List;

public class Rule {
	private final Category head;
	private final Category[] body;
	private final SemanticAction action;

	public Rule(Category head, Category...  body) {
		this.head = head;
		this.body = body;
		this.action = SemanticAction.NULL;
	}

	public Rule(SemanticAction act, Category head, Category... body) {
		this.head = head;
		this.body = body;
		this.action = act;
	}

	public String prettyPrint() {
		String s = head.getName() + " -> ";
		if (body.length == 0) {
			s += "\u03b5";
			return s;
		}

		for (Category c : body) {
			s += c.getName() + " ";
		}
		return s;
	}

	public SemanticAction getAction() {
		return action;
	}

	public Category getHead() {
		return head;
	}

	public List<Category> getBody() {
		return Arrays.asList(body);
	}

	@Override
	public int hashCode() {
		final int prime = 47;
		int result = 1;
		result = prime * result + Arrays.hashCode(body);
		result = prime * result + ((head == null) ? 0 : head.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Rule other = (Rule) obj;
		if (!Arrays.equals(body, other.body))
			return false;
		if (head == null) {
			if (other.head != null)
				return false;
		} else if (!head.equals(other.head))
			return false;
		return true;
	}

}
