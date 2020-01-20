public class Category {
	private boolean isTerminal;
	private String name;

	public Category(String name, boolean isTerminal) {
		this.isTerminal = isTerminal;
		this.name = name;
	}

	public String toString() {
		if (isTerminal) {
			return "<" + name + ">";
		} else {
			return name;
		}
	}

	public boolean isTerminal() {
		return isTerminal;
	}
}
