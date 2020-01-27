public class EarleyItem {
	final int start; // 0 means beginning of input
	final DottedRule rule;
	SPPFNode sppf;

	public EarleyItem(EarleyRule rule, int start) {
		this.start = start;
		this.rule = new DottedRule(rule, 0);
		this.sppf = null;
	}

	public EarleyItem(DottedRule rule, int start) {
		this.start = start;
		this.rule = rule;
	}

	public void setSPPF(SPPFNode n) {
		sppf = n;
	}

	public DottedRule getDottedRule() {
		return rule;
	}

	public SPPFNode getSPPF() {
		return sppf;
	}

	public int afterDot() {
		return rule.afterDot();
	}

	public boolean isComplete() {
		return rule.isComplete();
	}

	public EarleyItem advance() {
		return new EarleyItem(rule.advance(), start);
	}

	@Override public boolean equals(Object other) {
		if (!(other instanceof EarleyItem))
			return false;
		EarleyItem e = (EarleyItem) other;
		return start == e.start
			&& rule.equals(e.rule) && sppf == e.sppf; // reference equality here!
	}

	@Override public int hashCode() {
		return (rule.hashCode() * 31 + start) * 31 + (sppf == null ? 0 : sppf.hashCode());
	}

	public String prettyPrint(PrettyPrintingInfo info) {
		String s = info.getCategory(rule.r.head).toString() + " -> ";
		for (int j = 0; j < rule.r.body.length; ++j) {
			if (j == rule.dot) {
				s += "\u2022 ";
			}
			int symbol = rule.r.body[j];
			s += info.getCategory(symbol).toString() + " ";
		}

		if (rule.dot == rule.r.body.length) {
			s += "\u2022";
		}

		s += "(" + start + ")";
		return s;

	}
}
