import static org.junit.Assert.*;

import org.junit.Test;

public class EarleyParserTest {
	@Test public void testToString() {
		Category num = new Category("NUM", true);
		Category var = new Category("VAR", true);
		Category plus = new Category("+", true);
		Category times = new Category("*", true);

		Category s = new Category("s", false);
		Category p = new Category("p", false);
		Category t = new Category("t", false);

		EarleyParser parser = new EarleyParser();
		parser.addCategory(num);
		parser.addCategory(var);
		parser.addCategory(s);
		parser.addCategory(p);
		parser.addCategory(t);

		parser.addRule(new Rule(s, s, plus, p));
		parser.addRule(new Rule(s, p));
		parser.addRule(new Rule(p, p, times, t));
		parser.addRule(new Rule(p, t));
		parser.addRule(new Rule(t, num));
		parser.addRule(new Rule(t, var));

		parser.done();

		System.out.println(parser);

		assertEquals("Abc", parser.toString());
	}
}
