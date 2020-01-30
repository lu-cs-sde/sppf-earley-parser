package se.lth.sep;

public abstract class NodeLabel {
	// this node covers the symbols [start, end) in the token array, where
	// the array is indexed from 0.
	final int start, end;
	protected NodeLabel(int start, int end) {
		this.start = start;
		this.end = end;
	}

	@Override public boolean equals(Object other) {
		if (!(other instanceof NodeLabel))
			return false;
		NodeLabel o = (NodeLabel) other;
		return o.start == start && o.end == end;
	}

	@Override public int hashCode() {
		return 31 * start + end;
	}

	public abstract String prettyPrint(Grammar info);

	public int getStart() {
		return start;
	}

	public int getEnd() {
		return end;
	}
}
