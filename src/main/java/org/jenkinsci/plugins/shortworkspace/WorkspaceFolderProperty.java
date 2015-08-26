package org.jenkinsci.plugins.shortworkspace;

import com.cloudbees.hudson.plugins.folder.Folder;
import com.cloudbees.hudson.plugins.folder.FolderProperty;
import com.cloudbees.hudson.plugins.folder.FolderPropertyDescriptor;
import hudson.Extension;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

public class WorkspaceFolderProperty extends FolderProperty<Folder> {

    private boolean inFolder;

    @DataBoundConstructor
    public WorkspaceFolderProperty(boolean inFolder) {
        this.inFolder = inFolder;
    }

    public boolean isInFolder() {
        return inFolder;
    }

    Folder getOwner() {
        return this.owner;
    }

    @Extension
    public static class DescriptorImpl extends FolderPropertyDescriptor {
        @Override
        public String getDisplayName() {
            return Messages.DisplayName();
        }

        @Override
        public FolderProperty<?> newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            if (formData.isNullObject()) {
                return null;
            }

            JSONObject shortWorkspace = formData.getJSONObject("enableShortWorkspace");
            if (shortWorkspace.isNullObject()) {
                return null;
            }

            WorkspaceFolderProperty p = req.bindJSON(WorkspaceFolderProperty.class, shortWorkspace);

            return p;
        }
    }
}
