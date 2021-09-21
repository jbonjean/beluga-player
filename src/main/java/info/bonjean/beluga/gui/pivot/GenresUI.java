/*
 * Copyright (C) 2012-2021 Julien Bonjean <julien@bonjean.info>
 *
 * This file is part of Beluga Player.
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package info.bonjean.beluga.gui.pivot;

import info.bonjean.beluga.client.PandoraClient;
import info.bonjean.beluga.exception.BelugaException;
import info.bonjean.beluga.gui.PivotUI;
import info.bonjean.beluga.response.Category;
import info.bonjean.beluga.response.Station;
import java.net.URL;
import java.util.List;
import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.Map;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.collections.Sequence.Tree.Path;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.TablePane;
import org.apache.pivot.wtk.TreeView;
import org.apache.pivot.wtk.TreeViewSelectionListener;
import org.apache.pivot.wtk.content.TreeBranch;
import org.apache.pivot.wtk.content.TreeNode;
import org.apache.pivot.wtk.content.TreeViewNodeRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GenresUI extends TablePane implements Bindable {
	private static Logger log = LoggerFactory.getLogger(GenresUI.class);

	@BXML
	protected TreeView genresTree;

	@BXML
	protected PushButton submitButton;

	@Override
	public void initialize(Map<String, Object> namespace, URL location, Resources resources) {
		((TreeViewNodeRenderer) genresTree.getNodeRenderer()).setShowIcon(false);

		// change default tree view behavior (for branches).
		genresTree.getTreeViewSelectionListeners().add(new TreeViewSelectionListener.Adapter() {
			@Override
			public void selectedPathsChanged(TreeView treeView, Sequence<Path> previousSelectedPaths) {
				Object node = genresTree.getSelectedNode();

				if (node == null || !node.getClass().equals(TreeBranch.class)) {
					// not a branch, no processing to do, just update the submit button status.
					PivotUI.enableComponent(submitButton, canSubmit());
					return;
				}

				// it's a branch.
				Path currentSelectedPath = genresTree.getSelectedPath();

				// prevent the branch from being selected, try to re-select previous entry.
				if (previousSelectedPaths == null || previousSelectedPaths.getLength() < 1)
					genresTree.clearSelection();
				else
					genresTree.setSelectedPath(previousSelectedPaths.get(0));

				// toggle expand on the branch.
				genresTree.setBranchExpanded(currentSelectedPath, !genresTree.isBranchExpanded(currentSelectedPath));
			}
		});

		genresTree.setTreeData(generateTree());
		// genresTree.expandBranch(new Path(0));
	}

	private TreeBranch generateTree() {
		TreeBranch root = new TreeBranch();

		List<Category> categories;
		try {
			categories = PandoraClient.getInstance().getGenreStationList();
		} catch (BelugaException e) {
			log.error(e.getMessage());
			return root;
		}

		for (Category category : categories) {
			TreeBranch categoryBranch = new TreeBranch(category.getCategoryName());

			for (Station station : category.getStations()) {
				TreeNode stationNode = new TreeNode(station.getStationName());
				stationNode.setUserData(station);
				categoryBranch.add(stationNode);
			}

			root.add(categoryBranch);
		}

		return root;
	}

	private boolean canSubmit() {
		Object node = genresTree.getSelectedNode();
		return node != null && node.getClass().equals(TreeNode.class);
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		PivotUI.enableComponent(submitButton, canSubmit());
	}
}
