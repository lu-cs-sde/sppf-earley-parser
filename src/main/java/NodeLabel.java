public abstract class NodeLabel {
	int start, end;
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
}

class ItemLabel extends NodeLabel {
	DottedRule item;
	public ItemLabel(DottedRule item, int start, int end) {
		super(start, end);
		this.item = item;
	}

	@Override public boolean equals(Object other) {
		if (!super.equals(other))
			return false;
		return  (other instanceof ItemLabel) &&
			item.equals(((ItemLabel)other).item);
	}

	@Override public int hashCode() {
		return super.hashCode() * 31 + item.hashCode();
	}

	public String prettyPrint(Grammar info) {
		return item.prettyPrint(info) + ", " + super.start + ", " + super.end;
	}
}

class SymbolLabel extends NodeLabel {
	int symbol;
	public SymbolLabel(int symbol, int start, int end) {
		super(start, end);
		this.symbol = symbol;
	}

	@Override public boolean equals(Object other) {
		if (!super.equals(other))
			return false;
		return (other instanceof SymbolLabel) &&
			((SymbolLabel)other).symbol == symbol;
	}

	@Override public int hashCode() {
		return super.hashCode() * 31 + symbol;
	}

	public String prettyPrint(Grammar info) {
		return info.getCategory(symbol).toString() + ", " + super.start + ", " + super.end;
	}
}
