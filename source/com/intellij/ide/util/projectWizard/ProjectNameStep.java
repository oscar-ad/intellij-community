package com.intellij.ide.util.projectWizard;

import com.intellij.CommonBundle;
import com.intellij.ide.IdeBundle;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NonNls;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.List;

/**
 * @author Eugene Zhuravlev
 *         Date: Jan 21, 2004
 */
public class ProjectNameStep extends ModuleWizardStep {
  private static final Icon NEW_PROJECT_ICON = IconLoader.getIcon("/newprojectwizard.png");
  @NonNls private static final String PROJECT_FILE_EXTENSION = ".ipr";
  private NamePathComponent myNamePathComponent;
  private JPanel myPanel;
  private final WizardContext myWizardContext;

  public ProjectNameStep(WizardContext wizardContext) {
    myWizardContext = wizardContext;
    myNamePathComponent = new NamePathComponent(IdeBundle.message("label.project.name"), IdeBundle.message("label.project.file.location"), 'a', 'l',
                                                IdeBundle.message("title.select.project.file.directory"), IdeBundle.message("description.select.project.file.directory"));
    myPanel = new JPanel(new GridBagLayout());
    myPanel.setBorder(BorderFactory.createEtchedBorder());

    ApplicationInfo info = ApplicationManager.getApplication().getComponent(ApplicationInfo.class);
    String appName = info.getVersionName();
    myPanel.add(new JLabel(IdeBundle.message("label.please.enter.project.name", appName)),
                new GridBagConstraints(0, GridBagConstraints.RELATIVE, 1, 1, 1.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(8, 10, 8, 10), 0, 0));

    myPanel.add(myNamePathComponent, new GridBagConstraints(0, GridBagConstraints.RELATIVE, 1, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(8, 10, 8, 10), 0, 0));
  }

  public JComponent getPreferredFocusedComponent() {
    return myNamePathComponent.getNameComponent();
  }

  public String getHelpId() {
    return "project.new.page1";
  }

  public JComponent getComponent() {
    return myPanel;
  }

  public void updateStep() {
    super.updateStep();
    myNamePathComponent.setPath(myWizardContext.getProjectFileDirectory());
    String name = myWizardContext.getProjectName();
    if (name == null) {
      List<String> components = StringUtil.split(FileUtil.toSystemIndependentName(myWizardContext.getProjectFileDirectory()), "/");
      if (components.size() > 0) {
        name = components.get(components.size()-1);
      }
    }
    myNamePathComponent.setNameValue(name);
  }

  public void updateDataModel() {
    myWizardContext.setProjectName(getProjectName());
    myWizardContext.setProjectFileDirectory(getProjectFileDirectory());
  }

  public Icon getIcon() {
    return NEW_PROJECT_ICON;
  }

  public boolean validate() {
    String name = myNamePathComponent.getNameValue();
    if (name.length() == 0 ) {
      final ApplicationInfo info = ApplicationManager.getApplication().getComponent(ApplicationInfo.class);
      final String message = IdeBundle.message("prompt.new.project.file.name", info.getVersionName());
      Messages.showMessageDialog(myPanel, message, CommonBundle.getErrorTitle(), Messages.getErrorIcon());
      return false;
    }

    final String projectFileDirectory = getProjectFileDirectory();
    if (projectFileDirectory.length() == 0) {
      Messages.showMessageDialog(myPanel, IdeBundle.message("prompt.enter.project.file.location"), CommonBundle.getErrorTitle(), Messages.getErrorIcon());
      return false;
    }

    final boolean shouldPromptCreation = myNamePathComponent.isPathChangedByUser();
    if (!ProjectWizardUtil.createDirectoryIfNotExists(IdeBundle.message("directory.project.file.directory"), projectFileDirectory, shouldPromptCreation)) {
      return false;
    }

    boolean shouldContinue = true;
    final File projectFile = new File(getProjectFilePath());
    if (projectFile.exists()) {
      int answer = Messages.showYesNoDialog(
        IdeBundle.message("prompt.overwrite.project.file", projectFile.getAbsolutePath()),
        IdeBundle.message("title.file.already.exists"),
        Messages.getQuestionIcon()
      );
      shouldContinue = (answer == 0);
    }

    return shouldContinue;
  }

  public String getProjectFilePath() {
    return getProjectFileDirectory() + "/" + myNamePathComponent.getNameValue()/*myTfProjectName.getText().trim()*/ + PROJECT_FILE_EXTENSION;
  }

  public String getProjectFileDirectory() {
    return myNamePathComponent.getPath();
  }

  public String getProjectName() {
    return myNamePathComponent.getNameValue();
  }

}
