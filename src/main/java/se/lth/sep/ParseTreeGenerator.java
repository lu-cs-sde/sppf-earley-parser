package se.lth.sep;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Collections;

import se.lth.sep.SPPFNode.FamilyNode;

public class ParseTreeGenerator implements SPPFNodeVisitor {
	private HashMap<SPPFNode, LinkedList<ParseTree>> nodeMap = new HashMap<>();
	private HashMap<SPPFNode.FamilyNode, LinkedList<LinkedList<ParseTree>>> familyMap = new HashMap<>();
	private Grammar grammar;
	private SPPFNode root;

	public ParseTreeGenerator(Grammar grammar, SPPFNode root) {
		this.grammar = grammar;
		this.root = root;
	}

	public List<ParseTree> getParseTrees() {
		root.accept(this);
		return nodeMap.get(root);
	}


	@Override
	public void visit(FamilyNode f) {

		if (familyMap.containsKey(f))
			return;

		for (int i = 0; i < f.getNumChildren(); ++i) {
			f.getChild(i).accept(this);
		}

		LinkedList<LinkedList<ParseTree>> childrenTrees = new LinkedList<>();

		if (f.getNumChildren() != 0 ) {
			for (int i = 0; i < f.getNumChildren(); ++i) {
				childrenTrees.add(nodeMap.get(f.getChild(i)));
			}
		} else {
			// epsilon
			childrenTrees.add(new LinkedList<>(Collections.singletonList(new ParseTree(grammar.t_EPSILON, 0, 0))));
		}

		LinkedList<LinkedList<ParseTree>> childrenCombinations = Util.<ParseTree>product(childrenTrees.iterator());
		familyMap.put(f, childrenCombinations);
	}

	@Override
	public void visit(SPPFNode n) {
		if (nodeMap.containsKey(n)) {
			return;
		}

		int start = n.getLabel().getStart();
		int end = n.getLabel().getEnd();
		Category c = ((SymbolLabel)n.getLabel()).getSymbol(grammar);


		LinkedList<ParseTree> result = new LinkedList<>();
		if (n.getChildren().isEmpty()) {
			// handle the empty case
			result.add(new ParseTree(c, start, end));
		}

		for (FamilyNode f : n.getChildren()) {
			f.accept(this);

			LinkedList<LinkedList<ParseTree>> childAlternatives = familyMap.get(f);

			for (LinkedList<ParseTree> childAlternative : childAlternatives) {
				ParseTree pt;
				if (f.getInfo() != null) {
					pt = new ParseTree( (Rule)f.getInfo(), start, end);
				} else {
					pt = new ParseTree(c, start, end);
				}
				pt.addChildren(childAlternative);
				result.add(pt);
			}
		}
		nodeMap.put(n, result);
	}


}
