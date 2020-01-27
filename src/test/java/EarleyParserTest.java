import static org.junit.Assert.*;

import org.junit.Test;

public class EarleyParserTest {
	Category num = new Category("NUM", true);
	Category var = new Category("VAR", true);
	Category plus = new Category("+", true);
	Category times = new Category("*", true);
	Category metaNum = new Category("META_NUM", true);
	Category metaVar = new Category("META_VAR", true);

	Category s = new Category("s", false);
	Category p = new Category("p", false);
	Category t = new Category("t", false);


	EarleyParser makeParser() {

		EarleyParser parser = new EarleyParser();
		parser.addCategory(num);
		parser.addCategory(var);
		parser.addCategory(plus);
		parser.addCategory(times);
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

		return parser;
	}

	@Test public void testToString() {
		EarleyParser parser = makeParser();
		System.out.println(parser);
		assertEquals("s -> s <+> p \ns -> p \np -> p <*> t \np -> t \nt -> <VAR> \nt -> <NUM> \n",
				parser.toString());
	}


	@Test public void testParse1() {
		Category str[] = {num, plus, var};
		EarleyParser parser = makeParser();
		assertTrue(parser.recognize(str, s));
	}

	@Test public void testParse2() {
		Category str[] = {num, plus, plus};
		EarleyParser parser = makeParser();
		assertFalse(parser.recognize(str, s));
	}


	EarleyParser makeAmbiguousParser() {
		EarleyParser parser = new EarleyParser();
		parser.addCategory(num);
		parser.addCategory(var);
		parser.addCategory(plus);
		parser.addCategory(times);
		parser.addCategory(metaVar);
		parser.addCategory(s);
		parser.addCategory(p);
		parser.addCategory(t);

		parser.addRule(new Rule(s, s, plus, p));
		parser.addRule(new Rule(s, p));
		parser.addRule(new Rule(p, p, times, t));
		parser.addRule(new Rule(p, t));
		parser.addRule(new Rule(t, num));
		parser.addRule(new Rule(t, var));
		parser.addRule(new Rule(t, metaVar));
		parser.addRule(new Rule(p, metaVar));
		parser.addRule(new Rule(s, metaVar));

		parser.done();

		return parser;
	}

	@Test public void testParse3() {
		Category str[] = {metaVar, plus, metaVar};
		EarleyParser parser = makeAmbiguousParser();
		assertTrue(parser.recognize(str, s));
	}

	@Test public void testScottExample2() {
		EarleyParser parser = new EarleyParser();

		Category S = new Category("S", false);
		Category b = new Category("b", true);

		parser.addCategory(S);
		parser.addCategory(b);
		parser.addRule(new Rule(S, S, S));
		parser.addRule(new Rule(S, b));

		parser.done();

		Category str [] = {b, b, b};
		assertTrue(parser.recognize(str, S));
	}

	@Test public void testScottExample3() {
		EarleyParser parser = new EarleyParser();
		Category S = new Category("S", false);
		Category A = new Category("A", false);
		Category B = new Category("B", false);
		Category T = new Category("T", false);
		Category a = new Category("a", true);
		Category b = new Category("b", true);

		parser.addCategory(S);
		parser.addCategory(A);
		parser.addCategory(B);
		parser.addCategory(T);
		parser.addCategory(a);
		parser.addCategory(b);

		parser.addRule(new Rule(S, A, T));
		parser.addRule(new Rule(S, a, T));
		parser.addRule(new Rule(A, a));
		parser.addRule(new Rule(A, B, A));
		parser.addRule(new Rule(B)); // epsilon production
		parser.addRule(new Rule(T, b, b, b));

		parser.done();

		Category str[] = {a, b, b, b};
		assertTrue(parser.recognize(str, S));
	}
}
