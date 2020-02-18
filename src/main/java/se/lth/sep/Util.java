package se.lth.sep;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.mutable.MutableInt;

public class Util {
	public static void dumpParseResult(String dotFileName, SPPFNode node, Grammar grammar) {
		try {
			PrintStream out = new PrintStream(new File(dotFileName));
			DotVisitor visitor = new DotVisitor(out, grammar);
			visitor.prologue();
			node.accept(visitor);
			visitor.epilogue();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	private static int dumpParseTree(PrintStream ps, ParseTree pt, MutableInt count) {
		int currentID = count.getAndIncrement();
		String label = pt.getRule() != null ?
			pt.getRule().prettyPrint() : pt.getCategory().getName();
		ps.println(currentID + " [shape=box,label=\"" + label + "\"" +
				   (pt.getRule() == null ? ",color=red" : "") + "];");
		for (ParseTree c : pt.getChildren()) {
			int childID = dumpParseTree(ps, c, count);
			ps.println(currentID + " -> " + childID + ";");
		}
		return currentID;
	}

	public static void dumpParseTree(String dotFileName, ParseTree pt) {
		try {
			PrintStream out = new PrintStream(new File(dotFileName));
			out.println("digraph G {");
			dumpParseTree(out, pt, new MutableInt(0));
			out.println("}");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public static void dumpParseTrees(String dotFilePrefix, Collection<ParseTree> pts) {
		int index = 0;
		for (ParseTree pt : pts) {
			index++;
			dumpParseTree(dotFilePrefix + "_" + index + ".dot", pt);
		}
	}

	public static<T> LinkedList<LinkedList<T>> product(Iterator<LinkedList<T>> children) {
		if (!children.hasNext()) {
			return new LinkedList<>();
		}

		LinkedList<T> head = children.next();
		LinkedList<LinkedList<T>> tail = product(children);

		LinkedList<LinkedList<T>> result = new LinkedList<>();
		if (tail.isEmpty()) {
			for (T t : head) {
				LinkedList<T> singleton = new LinkedList<T>();
				singleton.add(t);
				result.add(singleton);
			}
			return result;
		}

		for (T t : head) {
			for (LinkedList<T> tt : tail) {
				LinkedList<T> childList = new LinkedList<>();
				childList.add(t);
				childList.addAll(tt);
				result.add(childList);
			}
		}

		return result;
	}

	static public List<ParseTree> enumerateParseTrees(SPPFNode root,
													  Grammar grammar,
													  SPPFTrivialProductionRemover tpr) {
		SPPFDebinarizeVisitor dbv = new SPPFDebinarizeVisitor();
		dbv.visit(root);

		// decorate nodes with the grammar rules
		SPPFNodeDecorator dec = new SPPFNodeDecorator(grammar);
		dec.visit(root);

		// remove trivial productions
		tpr.visit(root);

		// generate all the possible parse trees
		ParseTreeGenerator ptg = new ParseTreeGenerator(grammar, root);
		List<ParseTree> pts = ptg.getParseTrees();

		return pts;
	}
}

class DotVisitor implements SPPFNodeVisitor {
	private PrintStream ps;
	private int nodeID = 0;
	private HashMap<SPPFNode, Integer> visitedNodes = new HashMap<>();
	private HashMap<SPPFNode.FamilyNode, Integer> visitedFamilies = new HashMap<>();
	private Grammar info;

	public DotVisitor(PrintStream ps, Grammar info) {
		this.info = info;
		this.ps = ps;
	}

	public void prologue() {
		ps.println("digraph G {");
	}

	public void epilogue() {
		ps.println("}");
	}

	@Override
	public void visit(SPPFNode.FamilyNode f) {
		if (visitedFamilies.containsKey(f))
			return;

		int currentID = nodeID++;
		visitedFamilies.put(f, currentID);

		if (f.getNumChildren() == 0) {
			// epsilon
			ps.print(currentID + " [label=\u03b5];\n");
		} else {
			ps.print(currentID + " [shape=circle];\n");
			for (int i = 0; i < f.getNumChildren(); ++i)
				f.getChild(i).accept(this);
		}

		for (int i = 0; i < f.getNumChildren(); ++i)
			ps.print(currentID + " -> " + visitedNodes.get(f.getChild(i)) + ";\n");
	}

	@Override
	public void visit(SPPFNode n) {
		if (visitedNodes.containsKey(n))
			return;

		int currentID = nodeID++;
		visitedNodes.put(n, currentID);

		if (n.getLabel() instanceof ItemLabel) {
			ps.print(currentID + " [shape=box,color=red,label=\"" + n.prettyPrint(info) + "\"];\n");
		} else if (n.getChildren().size() > 1) {
			ps.print(currentID + " [shape=box,color=red,label=\"" + n.prettyPrint(info) + "\"];\n");
		} else {
			ps.print(currentID + " [shape=box,label=\"" + n.prettyPrint(info) + "\"];\n");
		}

		for (SPPFNode.FamilyNode f : n.children) {
			f.accept(this);

			ps.print(currentID + " -> " + visitedFamilies.get(f) + ";\n");
		}
	}
}
