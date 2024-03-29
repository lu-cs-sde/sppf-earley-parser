import static org.junit.Assert.*;

import java.util.List;

import se.lth.sep.*;

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

		Grammar g = new Grammar();

		g.addRule(new Rule(s, s, plus, p));
		g.addRule(new Rule(s, p));
		g.addRule(new Rule(p, p, times, t));
		g.addRule(new Rule(p, t));
		g.addRule(new Rule(t, num));
		g.addRule(new Rule(t, var));

		return new EarleyParser(g);
	}

	@Test public void testToString() {
		EarleyParser parser = makeParser();
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
		Grammar g = new Grammar();

		g.addRule(new Rule(s, s, plus, p));
		g.addRule(new Rule(s, p));
		g.addRule(new Rule(p, p, times, t));
		g.addRule(new Rule(p, t));
		g.addRule(new Rule(t, num));
		g.addRule(new Rule(t, var));
		g.addRule(new Rule(t, metaVar));
		g.addRule(new Rule(p, metaVar));
		g.addRule(new Rule(s, metaVar));

		return new EarleyParser(g);
	}

	@Test public void testParse3() {
		Category str[] = {metaVar, plus, metaVar};
		EarleyParser parser = makeAmbiguousParser();
		assertTrue(parser.recognize(str, s));
	}

	@Test public void testParse4() {
		Grammar g = new Grammar();
		Category a = new Category("a", true);
		Category b = new Category("b", true);
		Category c = new Category("c", true);

		Category A = new Category("A", false);
		Category B = new Category("B", false);
		Category C = new Category("C", false);
		Category S = new Category("S", false);

		g.addRule(new Rule(A, a, b));
		g.addRule(new Rule(A, a));
		g.addRule(new Rule(A));

		g.addRule(new Rule(B, a, b));
		g.addRule(new Rule(B, b, c));
		g.addRule(new Rule(B, b));
		g.addRule(new Rule(B, c));
		g.addRule(new Rule(B));

		g.addRule(new Rule(C, b, c));
		g.addRule(new Rule(C, c));
		g.addRule(new Rule(C));

		g.addRule(new Rule(S, A, B, C));

		Category[] str = {a, b, c};
		EarleyParser parser = new EarleyParser(g);
		SPPFNode root = parser.parse(str, S);
		assertNotNull(root);
		Util.dumpParseResult("testParse4-bt.dot", root, g);

		SPPFDebinarizeVisitor dbv = new SPPFDebinarizeVisitor();
		dbv.visit(root);

		Util.dumpParseResult("testParse4.dot", root, g);
	}

	@Test public void testScottExample2() {


		Category S = new Category("S", false);
		Category b = new Category("b", true);

		Grammar g = new Grammar();

		g.addRule(new Rule(S, S, S));
		g.addRule(new Rule(S, b));

		EarleyParser parser = new EarleyParser(g);

		Category str [] = {b, b, b};
		assertTrue(parser.recognize(str, S));
	}

	@Test public void testScottExample3() {
		Category S = new Category("S", false);
		Category A = new Category("A", false);
		Category B = new Category("B", false);
		Category T = new Category("T", false);
		Category a = new Category("a", true);
		Category b = new Category("b", true);

		Grammar g = new Grammar();

		g.addRule(new Rule(S, A, T));
		g.addRule(new Rule(S, a, T));
		g.addRule(new Rule(A, a));
		g.addRule(new Rule(A, B, A));
		g.addRule(new Rule(B)); // epsilon production
		g.addRule(new Rule(T, b, b, b));

		EarleyParser parser = new EarleyParser(g);

		Category str[] = {a, b, b, b};
		assertTrue(parser.recognize(str, S));
	}

	@Test public void testJava1() {
		Grammar g = new Grammar();
		Java14Grammar.addRules(g);

		EarleyParser parser = new EarleyParser(g);

		Category str[] = {Java14Grammar.t_METAVARID,
						  Java14Grammar.t_EQ,
						  Java14Grammar.t_METAVARID,
						  Java14Grammar.t_PLUS,
						  Java14Grammar.t_METAVARID,
						  Java14Grammar.t_PLUS,
						  Java14Grammar.t_METAVARID,
						  Java14Grammar.t_PLUS,
						  Java14Grammar.t_METAVARID,
						  Java14Grammar.t_PLUS,
						  Java14Grammar.t_METAVARID,
						  Java14Grammar.t_PLUS,
						  Java14Grammar.t_METAVARID,
						  Java14Grammar.t_PLUS,
						  Java14Grammar.t_METAVARID,
						  Java14Grammar.t_SEMICOLON};

		assertTrue(parser.recognize(str, Java14Grammar.n_statement));

		SPPFNode root = parser.parse(str, Java14Grammar.n_statement);
		assertNotNull(root);
		Util.dumpParseResult("testJava1-bt.dot", root, g);
		SPPFDebinarizeVisitor dbv = new SPPFDebinarizeVisitor();
		dbv.visit(root);
		Util.dumpParseResult("testJava1.dot", root, g);
		// decorate nodes with the grammar rules
		SPPFNodeDecorator dec = new SPPFNodeDecorator(g);
		dec.visit(root);
		// remove trivial productions
		SPPFTrivialProductionRemover tpr = new SPPFTrivialProductionRemover(g) {
				@Override public boolean isBubleUpChild(Category p, Category c) {
					if (c.getName().equals("METAVARID"))
						return true;
					if (c.getName().equals("GAP"))
						return true;
					return false;
				}
			};
		tpr.visit(root);
		Util.dumpParseResult("testJava1-notr.dot", root, g);
		// dump the parse trees
		ParseTreeGenerator ptg = new ParseTreeGenerator(g, root);
		List<ParseTree> pts = ptg.getParseTrees();
		Util.dumpParseTrees("testJava1-parse-tree", pts);
	}

	@Test public void testJava2() {
		Grammar g = new Grammar();
		Java14Grammar.addRules(g);

		EarleyParser parser = new EarleyParser(g);

		Category str[] = {
			Java14Grammar.t_CLASS,
			Java14Grammar.t_IDENTIFIER,
			Java14Grammar.t_LBRACE,
			Java14Grammar.t_METAVARID,
			Java14Grammar.t_METAVARID,
			Java14Grammar.t_LPAREN,
			Java14Grammar.t_RPAREN,
			Java14Grammar.t_LBRACE,
			Java14Grammar.t_GAP,
			Java14Grammar.t_RBRACE,
			Java14Grammar.t_RBRACE};

		SPPFNode root = parser.parse(str, Java14Grammar.n_class_declaration);
		assertNotNull(root);
		Util.dumpParseResult("testJava2-bt.dot", root, g);
		// debinarize
		SPPFDebinarizeVisitor dbv = new SPPFDebinarizeVisitor();
		dbv.visit(root);
		Util.dumpParseResult("testJava2.dot", root, g);
		// decorate nodes with the grammar rules
		SPPFNodeDecorator dec = new SPPFNodeDecorator(g);
		dec.visit(root);
		// remove trivial productions
		SPPFTrivialProductionRemover tpr = new SPPFTrivialProductionRemover(g) {
				@Override public boolean isBubleUpChild(Category p, Category c) {
					if (c.getName().equals("METAVARID"))
						return true;
					if (c.getName().equals("GAP"))
						return true;
					return false;
				}
			};
		tpr.visit(root);
		Util.dumpParseResult("testJava2-notr.dot", root, g);

		// dump the parse trees
		ParseTreeGenerator ptg = new ParseTreeGenerator(g, root);
		List<ParseTree> pts = ptg.getParseTrees();
		Util.dumpParseTrees("testJava2-parse-tree", pts);
	}

	@Test public void testJava3() {
		Grammar g = new Grammar();
		Java14Grammar.addRules(g);

		EarleyParser parser = new EarleyParser(g);
		/* `a = `b.`c + `e.`f.`g() ; . */
		Category str[] = {Java14Grammar.t_METAVARID,
						  Java14Grammar.t_EQ,
						  Java14Grammar.t_METAVARID,
						  Java14Grammar.t_DOT,
						  Java14Grammar.t_METAVARID,
						  Java14Grammar.t_PLUS,
						  Java14Grammar.t_METAVARID,
						  Java14Grammar.t_DOT,
						  Java14Grammar.t_METAVARID,
						  Java14Grammar.t_DOT,
						  Java14Grammar.t_METAVARID,
						  Java14Grammar.t_LPAREN,
						  Java14Grammar.t_RPAREN,
						  Java14Grammar.t_SEMICOLON};

		assertTrue(parser.recognize(str, Java14Grammar.n_statement));

		SPPFNode root = parser.parse(str, Java14Grammar.n_statement);
		assertNotNull(root);
		Util.dumpParseResult("testJava3-bt.dot", root, g);
		SPPFDebinarizeVisitor dbv = new SPPFDebinarizeVisitor();
		dbv.visit(root);
		Util.dumpParseResult("testJava3.dot", root, g);
		// remove trivial productions
		SPPFTrivialProductionRemover tpr = new SPPFTrivialProductionRemover(g) {
				@Override public boolean isBubleUpChild(Category p, Category c) {
					if (c.getName().equals("METAVARID"))
						return true;
					if (c.getName().equals("GAP"))
						return true;
					return false;
				}
			};
		tpr.visit(root);
		Util.dumpParseResult("testJava3-notr.dot", root, g);
	}

	@Test public void testJava4() {
		Grammar g = new Grammar();
		Java14Grammar.addRules(g);

		EarleyParser parser = new EarleyParser(g);

		Category str[] = {
			Java14Grammar.t_CLASS,
			Java14Grammar.t_IDENTIFIER,
			Java14Grammar.t_LBRACE,
			Java14Grammar.t_METAVARID,
			Java14Grammar.t_METAVARID,
			Java14Grammar.t_LPAREN,
			Java14Grammar.t_RPAREN,
			Java14Grammar.t_LBRACE,
			Java14Grammar.t_METAVARID,
			Java14Grammar.t_GAP,
			Java14Grammar.t_METAVARID,
			Java14Grammar.t_GAP,
			Java14Grammar.t_METAVARID,
			Java14Grammar.t_RBRACE,
			Java14Grammar.t_RBRACE};

		SPPFNode root = parser.parse(str, Java14Grammar.n_class_declaration);
		assertNotNull(root);
		Util.dumpParseResult("testJava4-bt.dot", root, g);
		// debinarize
		SPPFDebinarizeVisitor dbv = new SPPFDebinarizeVisitor();
		dbv.visit(root);
		Util.dumpParseResult("testJava4.dot", root, g);

		// remove trivial productions
		SPPFTrivialProductionRemover tpr = new SPPFTrivialProductionRemover(g) {
				@Override public boolean isBubleUpChild(Category p, Category c) {
					if (c.getName().equals("METAVARID"))
						return true;
					if (c.getName().equals("GAP"))
						return true;
					return false;
				}
			};
		tpr.visit(root);
		Util.dumpParseResult("testJava4-notr.dot", root, g);
	}


	@Test public void testJava5() {
		// This test is expected should finish in a reasonable ammount of time (~10s).
		Grammar g = new Grammar();
		Java14Grammar.addRules(g);

		EarleyParser parser = new EarleyParser(g);

		Category str[] = {
			Java14Grammar.t_CLASS,
			Java14Grammar.t_IDENTIFIER,
			Java14Grammar.t_LBRACE,
			// start ambigous constructor/method declaration
			Java14Grammar.t_METAVARID,
			Java14Grammar.t_METAVARID,
			Java14Grammar.t_LPAREN,
			Java14Grammar.t_RPAREN,
			Java14Grammar.t_LBRACE,
			Java14Grammar.t_GAP,
			Java14Grammar.t_RBRACE,
			// end ambigous constructor/method declaration

			// start ambigous constructor/method declaration
			Java14Grammar.t_METAVARID,
			Java14Grammar.t_METAVARID,
			Java14Grammar.t_LPAREN,
			Java14Grammar.t_RPAREN,
			Java14Grammar.t_LBRACE,
			Java14Grammar.t_GAP,
			Java14Grammar.t_RBRACE,
			// end ambigous constructor/method declaration

			// start ambigous constructor/method declaration
			Java14Grammar.t_METAVARID,
			Java14Grammar.t_METAVARID,
			Java14Grammar.t_LPAREN,
			Java14Grammar.t_RPAREN,
			Java14Grammar.t_LBRACE,
			Java14Grammar.t_GAP,
			Java14Grammar.t_RBRACE,
			// end ambigous constructor/method declaration


			// start ambigous constructor/method declaration
			Java14Grammar.t_METAVARID,
			Java14Grammar.t_METAVARID,
			Java14Grammar.t_LPAREN,
			Java14Grammar.t_RPAREN,
			Java14Grammar.t_LBRACE,
			Java14Grammar.t_GAP,
			Java14Grammar.t_RBRACE,
			// end ambigous constructor/method declaration

			// start ambigous constructor/method declaration
			Java14Grammar.t_METAVARID,
			Java14Grammar.t_METAVARID,
			Java14Grammar.t_LPAREN,
			Java14Grammar.t_RPAREN,
			Java14Grammar.t_LBRACE,
			Java14Grammar.t_GAP,
			Java14Grammar.t_RBRACE,
			// end ambigous constructor/method declaration


			// start ambigous constructor/method declaration
			Java14Grammar.t_METAVARID,
			Java14Grammar.t_METAVARID,
			Java14Grammar.t_LPAREN,
			Java14Grammar.t_RPAREN,
			Java14Grammar.t_LBRACE,
			Java14Grammar.t_GAP,
			Java14Grammar.t_RBRACE,
			// end ambigous constructor/method declaration

			Java14Grammar.t_RBRACE};

		SPPFNode root = parser.parse(str, Java14Grammar.n_class_declaration);
		assertNotNull(root);
		// remove trivial productions
		SPPFTrivialProductionRemover tpr = new SPPFTrivialProductionRemover(g) {
				@Override public boolean isBubleUpChild(Category p, Category c) {
					if (c.getName().equals("METAVARID"))
						return true;
					if (c.getName().equals("GAP"))
						return true;
					return false;
				}
			};
		List<ParseTree> pts = Util.enumerateParseTrees(root, g, tpr);
		// the parsing should produce 3^6 parse trees
		assertEquals(3*3*3*3*3*3, pts.size());
		Util.dumpParseTrees("testJava5-parse-tree", pts);
	}

	@Test public void testJava6() {
		Grammar g = new Grammar();
		Java8Grammar.addRules1(g);
		Java8Grammar.addRules2(g);

		EarleyParser parser = new EarleyParser(g);

		/* enum E { ID } */
		Category str[] = {
			Java8Grammar.t_ENUM,
			Java8Grammar.t_IDENTIFIER,
			Java8Grammar.t_LBRACE,
			Java8Grammar.t_IDENTIFIER,
			Java8Grammar.t_RBRACE
		};

		SPPFNode root = parser.parse(str, Java8Grammar.n_enum_declaration);
		assertNotNull(root);
		// remove trivial productions
		SPPFTrivialProductionRemover tpr = new SPPFTrivialProductionRemover(g) {
				@Override public boolean isBubleUpChild(Category p, Category c) {
					if (c.getName().equals("METAVARID"))
						return true;
					if (c.getName().equals("GAP"))
						return true;
					return false;
				}
			};
		List<ParseTree> pts = Util.enumerateParseTrees(root, g, tpr);
		Util.dumpParseTrees("testJava6-parse-tree", pts);
		assertEquals(1, pts.size());
	}

	@Test public void testJava7() {
		Grammar g = new Grammar();
		Java8Grammar.addRules1(g);
		Java8Grammar.addRules2(g);
		EarleyParser parser = new EarleyParser(g);

		/* enum E { .., MetaVarID,..; } */
		Category str[] = {
			Java8Grammar.t_ENUM,
			Java8Grammar.t_IDENTIFIER,
			Java8Grammar.t_LBRACE,
			Java8Grammar.t_GAP,
			Java8Grammar.t_COMMA,
			Java8Grammar.t_IDENTIFIER,
			Java8Grammar.t_COMMA,
			Java8Grammar.t_GAP,
			Java8Grammar.t_SEMICOLON,
			Java8Grammar.t_INT,
			Java8Grammar.t_IDENTIFIER,
			Java8Grammar.t_SEMICOLON,
			Java8Grammar.t_RBRACE
		};

		SPPFNode root = parser.parse(str, Java8Grammar.n_enum_declaration);
		assertNotNull(root);
		// remove trivial productions
		SPPFTrivialProductionRemover tpr = new SPPFTrivialProductionRemover(g) {
				@Override public boolean isBubleUpChild(Category p, Category c) {
					if (c.getName().equals("METAVARID"))
						return true;
					if (c.getName().equals("GAP"))
						return true;
					return false;
				}
			};


		List<ParseTree> pts = Util.enumerateParseTrees(root, g, tpr);
		Util.dumpParseTrees("testJava7-parse-tree", pts);
		assertEquals(1, pts.size());
	}

	@Test public void testParseList() {
		Grammar g = new Grammar();
		Category a = new Category("a", true);
		Category b = new Category("b", true);

		Category E = new Category("LE", false);
		g.addRule(new Rule(E, a));
		g.addRule(new Rule(E, b));

		Category L = new Category("L", false);
		g.addRule(new Rule(L, L, E));
		g.addRule(new Rule(L));

		Category[] str = {};
		EarleyParser parser = new EarleyParser(g);
		SPPFNode root = parser.parse(str, L);
		assertNotNull(root);
		Util.dumpParseResult("testParseList-bt.dot", root, g);

		SPPFDebinarizeVisitor dbv = new SPPFDebinarizeVisitor();
		dbv.visit(root);

		Util.dumpParseResult("testParseList.dot", root, g);
	}

	@Test public void testParseEmptyList() {
		Grammar g = new Grammar();
		Category a = new Category("a", true);
		Category b = new Category("b", true);

		Category E = new Category("LE", false);
		g.addRule(new Rule(E, a));
		g.addRule(new Rule(E, b));

		Category L = new Category("L", false);
		g.addRule(new Rule(L, E));
		g.addRule(new Rule(L, L, E));

		Category LO = new Category("LO", false);
		g.addRule(new Rule(LO));
		g.addRule(new Rule(LO, L));


		Category[] str = {};
		EarleyParser parser = new EarleyParser(g);
		SPPFNode root = parser.parse(str, LO);
		assertNotNull(root);
		Util.dumpParseResult("testParseEmptyList-bt.dot", root, g);

		SPPFDebinarizeVisitor dbv = new SPPFDebinarizeVisitor();
		dbv.visit(root);

		Util.dumpParseResult("testParseEmptyList.dot", root, g);
	}


	@Test public void testParseEmptyListPrefix() {
		Grammar g = new Grammar();
		Category a = new Category("a", true);
		Category b = new Category("b", true);

		Category E = new Category("LE", false);
		g.addRule(new Rule(E, a));
		g.addRule(new Rule(E, b));

		Category L = new Category("L", false);
		g.addRule(new Rule(L, E));
		g.addRule(new Rule(L, L, E));

		Category LO = new Category("LO", false);
		g.addRule(new Rule(LO));
		g.addRule(new Rule(LO, L));

		Category P = new Category("P", false);
		Category p = new Category("p", true);
		g.addRule(new Rule(P, p));

		Category PLO = new Category("PLO", false);
		g.addRule(new Rule(PLO, P, LO));


		Category[] str = {p};
		EarleyParser parser = new EarleyParser(g);
		SPPFNode root = parser.parse(str, PLO);

		assertNotNull(root);
		Util.dumpParseResult("testParseEmptyListPrefix-bt.dot", root, g);

		SPPFDebinarizeVisitor dbv = new SPPFDebinarizeVisitor();
		dbv.visit(root);

		Util.dumpParseResult("testParseEmptyListPrefix.dot", root, g);
	}

}
