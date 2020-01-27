import java.util.Arrays;
import java.util.List;

public class Rule {
	private Category head;
	private Category[] body;

	public Rule(Category head, Category...  body) {
		this.head = head;
		// assert body.length > 0;
		this.body = body;
	}

	public Category getHead() {
		return head;
	}

	public List<Category> getBody() {
		return Arrays.asList(body);
	}
}
