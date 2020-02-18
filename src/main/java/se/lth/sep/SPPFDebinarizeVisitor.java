package se.lth.sep;

import java.util.HashSet;
import java.util.Set;

/**
   A visitor that removes the nodes that contain dotted rules.
*/
public class SPPFDebinarizeVisitor implements SPPFNodeVisitor {
	private HashSet<SPPFNode> visitedNodes = new HashSet<>();
	private HashSet<SPPFNode.FamilyNode> visitedFamNodes = new HashSet<>();

	public void visit(SPPFNode.FamilyNode familyNode) {
		if (!visitedFamNodes.add(familyNode)) {
			// node already visited
			return;
		}
		for (int i = 0; i < familyNode.getNumChildren(); ++i)
			familyNode.getChild(i).accept(this);
	}

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

			// only the first child can a be a node representing a dot rule
			SPPFNode firstChild = f.getChild(0);
			if (!(firstChild.getLabel() instanceof ItemLabel))
				continue;

			// now we know this is representing a dot rule
			for (SPPFNode.FamilyNode g : firstChild.getChildren()) {
				SPPFNode[] childArray = new SPPFNode[g.getNumChildren() + f.getNumChildren() - 1];
				for (int i = 0; i < g.getNumChildren(); ++i) {
					childArray[i] = g.getChild(i);
				}

				for (int i = 1; i < f.getNumChildren(); ++i) {
					childArray[g.getNumChildren() + i - 1] = f.getChild(i);
				}

				newChildren.add(new SPPFNode.FamilyNode(childArray));
				childrenToRemove.add(f);
			}
		}

		children.removeAll(childrenToRemove);
		children.addAll(newChildren);
	}
}
