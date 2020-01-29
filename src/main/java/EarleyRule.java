class EarleyRule implements Comparable<EarleyRule> {
	final int body[];
	final int head;

	public EarleyRule(int head, int body[]) {
		this.head = head;
		this.body = body;
	}

	public boolean isEmpty() {
		return body.length == 0;
	}

	public boolean startsWithNonTerminal() {
		return isEmpty() || !EarleyParser.isTerminal(body[0]);
	}

	@Override
	public int compareTo(EarleyRule other) {
		for (int i = 0; i < Math.min(this.body.length, other.body.length); ++i) {
			if (this.body[i] < other.body[i])
				return -1;
			else if (this.body[i] > other.body[i])
				return 1;
		}
		if (this.body.length == other.body.length) {
			return 0;
		} else if (this.body.length < other.body.length) {
			return -1;
		} else {
			return 0;
		}
	}

	public String prettyPrint(Grammar info) {
		String s = info.getCategory(head).toString() + " -> ";
		for (int j : body) {
			s += info.getCategory(j).toString() + " ";
		}
		return s;
	}
}
