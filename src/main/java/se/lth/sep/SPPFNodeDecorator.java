package se.lth.sep;

import java.util.ArrayList;
import java.util.HashSet;

public class SPPFNodeDecorator implements SPPFNodeVisitor {
	private HashSet<SPPFNode> visitedNodes = new HashSet<>();
	private HashSet<SPPFNode.FamilyNode> visitedFamNodes = new HashSet<>();
	Grammar grammar;

	public void visit(SPPFNode.FamilyNode familyNode) {
		if (!visitedFamNodes.add(familyNode)) {
			// node already visited
			return;
		}
		for (int i = 0; i < familyNode.getNumChildren(); ++i)
			familyNode.getChild(i).accept(this);
	}

	public SPPFNodeDecorator(Grammar grammar) {
		this.grammar = grammar;
	}

	public void visit(SPPFNode n) {
		if (!visitedNodes.add(n)) {
			// node already visited
			return;
		}

		Category head = ((SymbolLabel)n.getLabel()).getSymbol(grammar);

		for (SPPFNode.FamilyNode f : n.getChildren()) {
			f.accept(this);
			ArrayList<Category> body = new ArrayList<>(f.getNumChildren());
			for (int i = 0; i < f.getNumChildren(); ++i) {
				Category c = ((SymbolLabel)f.getChild(i).getLabel()).getSymbol(grammar);
				body.add(c);
			}
			Rule r = grammar.getRule(head, body);
			assert f.getInfo() == null;
			assert head.isTerminal() || r != null;
			f.setInfo(r);
		}
	}
}
