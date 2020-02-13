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

		Set<SPPFNode.FamilyNode> children = n.getChildren();

		HashSet<SPPFNode.FamilyNode> newChildren = new HashSet<>();
		HashSet<SPPFNode.FamilyNode> childrenToRemove = new HashSet<>();

		for (SPPFNode.FamilyNode f : children) {
			f.accept(this);

			if (f.getNumChildren() == 0)
				continue;

			for (int i = 0; i < f.getNumChildren(); ++i) {
				SPPFNode ruleNode = f.getChild(i);
				assert ruleNode.getLabel() instanceof SymbolLabel;
				Category head = ((SymbolLabel)ruleNode.getLabel()).getSymbol(grammar);

				boolean allProductionAreTrivial = true;
				for (SPPFNode.FamilyNode g : ruleNode.getChildren()) {
					if (g.getNumChildren() != 1) {
						allProductionAreTrivial = false;
						break;
					}

					Category body = ((SymbolLabel)g.getChild(0).getLabel()).getSymbol(grammar);
					if (!isTrivialProduction(head, body)) {
						allProductionAreTrivial = false;
						break;
					}
				}

				if (!allProductionAreTrivial)
					continue;

				for (SPPFNode.FamilyNode g : ruleNode.getChildren()) {
					SPPFNode[] childArray = new SPPFNode[f.getNumChildren()];
					for (int j = 0; j < f.getNumChildren(); ++j) {
						if (i == j) {
							childArray[j] = g.getChild(0);
						} else {
							childArray[j] = f.getChild(j);
						}
					}

					newChildren.add(new SPPFNode.FamilyNode(childArray));
					childrenToRemove.add(f);
				}
			}
		}

		children.removeAll(childrenToRemove);
		children.addAll(newChildren);
	}

	public boolean isTrivialProduction(Category head, Category body) {
		return false;
	}
}
