package se.lth.sep;

import java.util.List;

public abstract class SemanticAction {
	public abstract Object act(List<Object> children);

	public static SemanticAction NULL = new SemanticAction() {
			@Override public Object act(List<Object> children) { return null; }
		};

	public static SemanticAction PASSTROUGH = new SemanticAction() {
			@Override public Object act(List<Object> children) { return children.get(0); }
		};
}
