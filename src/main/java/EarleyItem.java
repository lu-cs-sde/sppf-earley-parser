public class EarleyItem {
	int dot; // 0 means before the first element in the rule
	final int start; // 0 means beginning of input
	final EarleyRule rule;
	SPPFNode sppf;

	public EarleyItem(EarleyRule rule, int start) {
		this.dot = 0;
		this.start = start;
		this.rule = rule;
		this.sppf = null;
	}

	public void setSPPF(SPPFNode n) {
		sppf = n;
	}

	public DottedRule getDottedRule() {
		return new DottedRule(rule, dot);
	}

	public SPPFNode getSPPF() {
		return sppf;
	}

	public int afterDot() {
		assert !isComplete();
		return rule.body[dot];
	}

	public boolean isComplete() {
		return this.rule.body.length == this.dot;
	}

	public EarleyItem advance() {
		assert !isComplete();
		EarleyItem ret = new EarleyItem(rule, start);
		ret.dot = dot + 1;
		return ret;
	}

	@Override public boolean equals(Object other) {
		if (!(other instanceof EarleyItem))
			return false;
		EarleyItem e = (EarleyItem) other;
		return dot == e.dot && start == e.start
			&& rule == e.rule && sppf == e.sppf; // reference equality here!
	}

	@Override public int hashCode() {
		return ((rule.hashCode() + (dot * 31)) * 31 + start) * 31; // + (sppf == null ? 0 : sppf.hashCode());
	}
}
