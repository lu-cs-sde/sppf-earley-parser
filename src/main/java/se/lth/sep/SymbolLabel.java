package se.lth.sep;

public class SymbolLabel extends NodeLabel {
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

	public Category  getSymbol(Grammar info) {
		return info.getCategory(symbol);
	}
}
