package se.lth.sep;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class SPPFNode {
	static public class FamilyNode {
		private SPPFNode[] child;

		FamilyNode(SPPFNode child0) {
			assert child0 != null;
			child = new SPPFNode[1];
			child[0] = child0;
		}

		FamilyNode() {
			child = new SPPFNode[0];
		}

		FamilyNode(SPPFNode child0, SPPFNode child1) {
			assert child0 != null || child1 != null;
			child = new SPPFNode[2];
			child[0] = child0;
			child[1] = child1;
		}

		FamilyNode(SPPFNode children[]) {
			child = Arrays.copyOf(children, children.length);
		}

		public int getNumChildren() {
			return child.length;
		}

		public SPPFNode getChild(int i) {
			return child[i];
		}

		@Override
		public int hashCode() {
			final int prime = 47;
			int result = 1;
			for (int i = 0; i < child.length; ++i) {
				result = prime * result + ((child[0] == null) ? 0 : child[0].hashCode());
			}
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			FamilyNode other = (FamilyNode) obj;
			if (other.child.length != child.length)
				return false;
			for (int i = 0; i < child.length; ++i) {
				if (child[i] == null) {
					if (other.child[i] != null)
						return false;
				} else if (!child[i].equals(other.child[i])) {
					return false;
				}
			}
			return true;
		}

		public void accept(SPPFNodeVisitor visitor) {
			visitor.visit(this);
		}
	}

	HashSet<FamilyNode> children = new HashSet<>();
	private NodeLabel label;

	public SPPFNode() {
		label = null;
	}

	public SPPFNode(NodeLabel label) {
		this.label = label;
	}

	public void addChild(SPPFNode child) {
		children.add(new FamilyNode(child));
	}

	public void addChildren(SPPFNode child0, SPPFNode child1) {
		children.add(new FamilyNode(child0, child1));
	}

	public void addEpsilon() {
		children.add(new FamilyNode());
	}

	public String prettyPrint(Grammar info) {
		return label.prettyPrint(info);
	}

	public void accept(SPPFNodeVisitor visitor) {
		visitor.visit(this);
	}

	public Set<FamilyNode> getChildren() {
		return children;
	}

	public NodeLabel getLabel() {
		return label;
	}
}
