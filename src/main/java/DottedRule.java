public class DottedRule {
	final EarleyRule r;
	final int dot;

	public DottedRule(EarleyRule r, int dot) {
		assert r != null;
		this.r = r;
		this.dot = dot;
	}

	public DottedRule advance() {
		assert !isComplete();
		return new DottedRule(r, dot + 1);
	}

	public boolean isComplete() {
		return r.body.length == dot;
	}

	public int afterDot() {
		assert !isComplete();
		return r.body[dot];
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + dot;
		result = prime * result + ((r == null) ? 0 : r.hashCode());
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
		DottedRule other = (DottedRule) obj;
		if (dot != other.dot)
			return false;
		if (!r.equals(other.r))
			return false;
		return true;
	}
}
