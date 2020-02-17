package se.lth.sep;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import se.lth.sep.SPPFNode.FamilyNode;

public class SPPFTrivialProductionRemover implements SPPFNodeVisitor {
	private HashSet<SPPFNode> visitedNodes = new HashSet<>();
	private HashSet<SPPFNode.FamilyNode> visitedFamNodes = new HashSet<>();
	private Grammar grammar;

	@Override
	public void visit(FamilyNode familyNode) {
		if (!visitedFamNodes.add(familyNode)) {
			// node already visited
			return;
		}
		for (int i = 0; i < familyNode.getNumChildren(); ++i)
			familyNode.getChild(i).accept(this);
	}

	public SPPFTrivialProductionRemover(Grammar grammar) {
		this.grammar = grammar;
	}

	@Override
	public void visit(SPPFNode n) {
		if (!visitedNodes.add(n)) {
			// node already visited
			return;
		}


		for (SPPFNode.FamilyNode f : n.getChildren()) {
			f.accept(this);
		}

		HashSet<SPPFNode.FamilyNode> newChildren = new HashSet<>();
		HashSet<SPPFNode.FamilyNode> childrenToRemove = new HashSet<>();

		for (SPPFNode.FamilyNode f : n.getChildren()) {

			if (f.getNumChildren() != 1)
				continue;
			SPPFNode[] childArray = new SPPFNode[f.getNumChildren()];
			boolean updatedChildren = false;
			for (int i = 0; i < f.getNumChildren(); ++i) {
				childArray[i] = f.getChild(i);
			}

			for (int i = 0; i < f.getNumChildren(); ++i) {
				SPPFNode bubbleUpChild = null;
				SPPFNode nn = f.getChild(i);

				for (SPPFNode.FamilyNode ff : nn.getChildren()) {
					if (ff.getNumChildren() != 1)
						continue;

					SymbolLabel label = (SymbolLabel)ff.getChild(0).getLabel();
					if (isBubleUpChild(label.getSymbol(grammar))) {
						bubbleUpChild = ff.getChild(0);
						break;
					}
				}

				if (bubbleUpChild != null) {
					childArray[i] = bubbleUpChild;
					updatedChildren = true;
				}
			}
			if (updatedChildren) {
				childrenToRemove.add(f);
				newChildren.add(new FamilyNode(childArray));
			}
		}

		n.getChildren().removeAll(childrenToRemove);
		n.getChildren().addAll(newChildren);
	}

	public boolean isBubleUpChild(Category c) {
		return false;
	}
}
