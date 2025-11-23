package dev.mikeya.barracudahelper.ui;

import dev.mikeya.barracudahelper.BarracudaHelperPlugin;
import dev.mikeya.barracudahelper.Trial;
import net.runelite.client.ui.PluginPanel;

import javax.swing.*;
import java.awt.*;

public class BarracudaHelperPluginPanel extends PluginPanel
{
    public JPanel currentPanel;
    public BarracudaHelperPlugin plugin= null;
    public Trial editing = null;

    public BarracudaHelperPluginPanel(BarracudaHelperPlugin plugin)
    {
        this.plugin = plugin;
        setLayout(new BorderLayout());
        showMainPanel();
    }

    public void showMainPanel()
    {
        switchPanel(new MainPanel(this));
    }

    public void showEditorPanel(Trial trial)
    {
        editing = trial;
        switchPanel(new EditorPanel(editing, this));
    }

    private void switchPanel(JPanel newPanel)
    {
        if (currentPanel != null)
        {
            remove(currentPanel);
        }

        currentPanel = newPanel;
        add(currentPanel, BorderLayout.CENTER);
        revalidate();
        repaint();
    }
}
