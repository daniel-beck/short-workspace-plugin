package org.jenkinsci.plugins.shortworkspace;

import com.cloudbees.hudson.plugins.folder.Folder;
import hudson.Extension;
import hudson.FilePath;
import hudson.Util;
import hudson.model.ItemGroup;
import hudson.model.Node;
import hudson.model.Slave;
import hudson.model.TopLevelItem;
import jenkins.model.Jenkins;
import jenkins.slaves.WorkspaceLocator;

import java.util.logging.Level;
import java.util.logging.Logger;

@Extension
public class ShortWorkspaceLocator extends WorkspaceLocator {
    @Override
    public FilePath locate(TopLevelItem item, Node node) {
        WorkspaceFolderProperty p = findWorkspaceFolderPropertyInAncestors(item);
        if (p == null) {
            LOGGER.log(Level.FINE, "Could not find WorkspaceFolderProperty for " + item.getFullName());
            return null;
        }
        FilePath workspace = getWorkspaceRoot(node);
        if (p.isInFolder()) {
            workspace = workspace.child(p.getOwner().getFullName());
        }
        workspace = workspace.child(getShortWorkspaceNameFor(item));
        LOGGER.log(Level.CONFIG, "Choosing short workspace path for " + item.getFullName() + " on " + node.getDisplayName() + ": " + workspace.getRemote());
        return workspace;
    }

    /**
     * Finds the nearest WorkspaceFolderProperty in the parent ItemGroup hierarchy of the given TopLevelItem
     * @param item
     * @return the nearest WorkspaceFolderProperty in the parent ItemGroup hierarchy of the given TopLevelItem, or null if none was found
     */
    private WorkspaceFolderProperty findWorkspaceFolderPropertyInAncestors(TopLevelItem item) {
        ItemGroup group = item.getParent();
        while (group != Jenkins.getInstance()) {
            if (group instanceof Folder) {
                Folder f = (Folder) group;
                WorkspaceFolderProperty p = f.getProperties().get(WorkspaceFolderProperty.class);
                if (p != null) {
                    LOGGER.log(Level.CONFIG, "Found WorkspaceFolderProperty for " + item.getFullName() + " in " + f.getFullName());
                    return p;
                }
                group = f.getParent();
            } else {
                LOGGER.log(Level.FINE, item.getFullName() + " is in an ItemGroup that is not a folder: " + group.getFullName());
                return null;
            }
        }
        return null;
    }

    /**
     * Returns the root workspace folder the the given node.
     *
     * @param node
     * @return the root workspace folder the the given node.
     */
    private FilePath getWorkspaceRoot(Node node) {
        if (node instanceof Slave) {
            return ((Slave) node).getWorkspaceRoot();
        }
        // need to choose a sensible default
        // using the obsolete JENKINS_HOME/jobs/foo/bar/workspace hierarchy makes no sense
        return node.getRootPath().child(WORKSPACE_FOLDER_NAME);
    }

    /**
     * Computes the short workspace name to use for the given top level item.
     * @param item
     * @return the short workspace name to use for the given top level item.
     */
    private String getShortWorkspaceNameFor(TopLevelItem item) {
        return Util.getDigestOf(item.getFullName()).substring(0, 8);
    }

    public static String WORKSPACE_FOLDER_NAME = System.getProperty(ShortWorkspaceLocator.class.getName() + ".defaultWorkspaceFolder", "workspace");

    private static final Logger LOGGER = Logger.getLogger(ShortWorkspaceLocator.class.getName());
}
