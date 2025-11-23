package dev.mikeya.barracudahelper.ui;

import dev.mikeya.barracudahelper.BarracudaHelperPlugin;
import dev.mikeya.barracudahelper.Routes;
import dev.mikeya.barracudahelper.Trial;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MainPanel extends JPanel
{
    private final BarracudaHelperPluginPanel parent;

    public MainPanel(BarracudaHelperPluginPanel parent)
    {
        this.parent = parent;

        setLayout(new GridLayout(0, 1, 4, 4));

        for (Trial trial : Routes.TRIALS) {
            add(createTrialRow(trial));
        }
    }

    private JPanel createTrialRow(Trial trial)
    {
        JPanel row = new JPanel(new BorderLayout());
        row.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.GRAY), BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        JPanel namePanel = new JPanel(new BorderLayout());
        JLabel nameLabel = new JLabel(trial.trialName);
        JLabel routeLabel = new JLabel(trial.routeName);

        namePanel.add(nameLabel, BorderLayout.WEST);
        namePanel.add(routeLabel, BorderLayout.EAST);

        JButton menuBtn = new JButton("...");

        JPopupMenu menu = new JPopupMenu();
        JMenuItem edit = new JMenuItem("Edit");

        edit.addActionListener(e -> parent.showEditorPanel(trial));

        menu.add(edit);

        JMenuItem reset = new JMenuItem("Reset");
        reset.addActionListener(e -> {
            Trial newTrial = null;
            try (var stream = getClass().getResourceAsStream("/dev/mikeya/barracudahelper/defaults/" + trial.trialName.replace(" ", "_") + ".json"))
            {
                if (stream != null)
                {
                    String json = new String(stream.readAllBytes());
                    newTrial = parent.plugin.gson.fromJson(json, Trial.class);
                }
            }
            catch (Exception ee)
            {
                ee.printStackTrace();
            }

            if (newTrial == null) {
                newTrial = new Trial("Default", trial.trialName);
            }

            trial.markers.clear();
            trial.markers.addAll(newTrial.markers);
            trial.routeName = newTrial.routeName;
            parent.plugin.clientThread.invokeLater(() -> {
                parent.plugin.recalculateVisibleMarkers();
            });
        });
        menu.add(reset);

        menuBtn.addActionListener(e -> menu.show(menuBtn, 0, menuBtn.getHeight()));

        attachPopup(row, menu);
        attachPopup(namePanel, menu);
        attachPopup(menuBtn, menu);

        row.add(namePanel, BorderLayout.CENTER);
        row.add(menuBtn, BorderLayout.EAST);

        return row;
    }

    private void attachPopup(JComponent c, JPopupMenu menu)
    {
        c.addMouseListener(new MouseAdapter()
        {
            private void show(MouseEvent e)
            {
                if (e.isPopupTrigger())
                {
                    menu.show(e.getComponent(), e.getX(), e.getY());
                }
            }

            @Override
            public void mousePressed(MouseEvent e) { show(e); }

            @Override
            public void mouseReleased(MouseEvent e) { show(e); }
        });
    }

}
