import java.util.List;
import org.junit.Test;

import se.lth.sep.*;
import static org.junit.Assert.*;

public class ParseTreeGeneratorTest {
	@Test public void test1() {
		Grammar g = new Grammar();
		Java14Grammar.addRules(g);

		EarleyParser parser = new EarleyParser(g);

		Category str[] = {Java14Grammar.t_LPAREN,
						  Java14Grammar.t_METAVARID,
						  Java14Grammar.t_RPAREN};

		SPPFNode root = parser.parse(str, Java14Grammar.n_expression);

		SPPFTrivialProductionRemover tpr = new SPPFTrivialProductionRemover(g) {
				@Override public boolean isBubleUpChild(Category p, Category c) {
					if (c.getName().equals("METAVARID"))
						return true;
					if (c.getName().equals("GAP"))
						return true;
					return false;
				}
			};

		SPPFDebinarizeVisitor dbv = new SPPFDebinarizeVisitor();
		dbv.visit(root);

		// decorate nodes with the grammar rules
		SPPFNodeDecorator dec = new SPPFNodeDecorator(g);
		dec.visit(root);

		// remove trivial productions
		tpr.visit(root);

		// generate all the possible parse trees
		ParseTreeGenerator ptg = new ParseTreeGenerator(g, root);
		List<ParseTree> pts1 = ptg.getParseTrees();

		List<ParseTree> pts = pts1;

		assertEquals(1, pts.size());
	}
}
