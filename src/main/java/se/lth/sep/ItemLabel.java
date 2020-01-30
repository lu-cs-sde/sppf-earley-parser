package se.lth.sep;

public class ItemLabel extends NodeLabel {
	final DottedRule item;
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

	public DottedRule getDottedRule() {
		return item;
	}
}
