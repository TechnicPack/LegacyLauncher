package org.spoutcraft.launcher;

import java.awt.Component;
import java.awt.Container;
import java.awt.FocusTraversalPolicy;
import java.util.ArrayList;
import java.util.List;

public class SpoutFocusTraversalPolicy extends FocusTraversalPolicy {
	List<Component>	order;

	public SpoutFocusTraversalPolicy(List<Component> order) {
		this.order = new ArrayList<Component>(order.size());
		this.order.addAll(order);
	}

	@Override
	public Component getComponentAfter(Container focusCycleRoot, Component aComponent) {
		int idx = (order.indexOf(aComponent) + 1) % order.size();
		return order.get(idx);
	}

	@Override
	public Component getComponentBefore(Container focusCycleRoot, Component aComponent) {
		int idx = order.indexOf(aComponent) - 1;
		if (idx < 0) {
			idx = order.size() - 1;
		}
		return order.get(idx);
	}

	@Override
	public Component getDefaultComponent(Container focusCycleRoot) {
		return order.get(0);
	}

	@Override
	public Component getLastComponent(Container focusCycleRoot) {
		return order.get(order.size() - 1);
	}

	@Override
	public Component getFirstComponent(Container focusCycleRoot) {
		return order.get(0);
	}
}