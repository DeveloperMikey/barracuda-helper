package dev.mikeya.barracudahelper.ui;

import dev.mikeya.barracudahelper.PathMarker;
import dev.mikeya.barracudahelper.Routes;
import dev.mikeya.barracudahelper.Trial;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.ColorScheme;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EditorPanel extends JPanel
{
    private final Trial trial;
    private final BarracudaHelperPluginPanel parent;

    private final DefaultListModel<PathMarker> markerListModel = new DefaultListModel<>();
    private final JList<PathMarker> markerList = new JList<>(markerListModel);
    private final JLabel nameLabel = new JLabel();

    public EditorPanel(Trial trial, BarracudaHelperPluginPanel parent)
    {
        this.trial = trial;
        this.parent = parent;

        setLayout(new BorderLayout());

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Editing: " + trial.trialName);
        header.add(title);

        JPanel nameRow = new JPanel(new BorderLayout());
        this.nameLabel.setText(trial.routeName);
        nameRow.add(this.nameLabel, BorderLayout.WEST);

        JButton editBtn = new JButton("Edit");
        editBtn.setMargin(new Insets(2, 6, 2, 6));
        nameRow.add(editBtn, BorderLayout.EAST);

        header.add(nameRow);

        editBtn.addActionListener(e -> {
            String newName = JOptionPane.showInputDialog(
                    this,
                    "Enter a new name for this route:",
                    trial.routeName
            );

            if (newName != null && !newName.trim().isEmpty())
            {
                trial.routeName = newName;
                nameLabel.setText(newName);
                Routes.saveTrial(trial);
            }
        });

        add(header, BorderLayout.NORTH);

        markerList.setCellRenderer(new MarkerCellRenderer());
        markerList.setDragEnabled(true);
        markerList.setDropMode(DropMode.INSERT);
        markerList.setTransferHandler(new MarkerListTransferHandler());
        markerList.setVisibleRowCount(20);
        markerList.setBackground(ColorScheme.DARK_GRAY_COLOR);
        JScrollPane scrollPane = new JScrollPane(markerList);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));

        JButton clearBtn = new JButton("Clear All");
        clearBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                    this, "Are you sure you want to clear all markers?",
                    "Confirm Clear", JOptionPane.YES_NO_OPTION
            );
            if (confirm == JOptionPane.YES_OPTION) {
                markerListModel.clear();
                trial.markers.clear();
            }
        });

        JPanel importExportPanel = new JPanel(new GridLayout(1, 2, 5, 0));
        JButton importBtn = new JButton("Import");
        JButton exportBtn = new JButton("Export");
        importBtn.addActionListener(e -> importMarkers());
        exportBtn.addActionListener(e -> exportMarkers());
        importExportPanel.add(importBtn);
        importExportPanel.add(exportBtn);

        JButton finishBtn = new JButton("Finish");
        finishBtn.addActionListener(e -> {
            parent.showMainPanel();
            parent.editing = null;
        });


        bottomPanel.add(clearBtn);
        clearBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        clearBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, clearBtn.getPreferredSize().height));
        bottomPanel.add(Box.createVerticalStrut(5));
        bottomPanel.add(importExportPanel);
        bottomPanel.add(Box.createVerticalStrut(5));
        bottomPanel.add(finishBtn);
        finishBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        finishBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, finishBtn.getPreferredSize().height));
        bottomPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, bottomPanel.getPreferredSize().height));
        add(bottomPanel, BorderLayout.SOUTH);

        rebuildMarkerList();
        setupRightClickMenu();
    }

    public void rebuildMarkerList()
    {
        markerListModel.clear();
        for (PathMarker m : trial.markers)
            markerListModel.addElement(m);
        Routes.saveTrial(trial);
    }

    private class MarkerCellRenderer implements ListCellRenderer<PathMarker>
    {
        @Override
        public Component getListCellRendererComponent(JList<? extends PathMarker> list,
                                                      PathMarker marker,
                                                      int index,
                                                      boolean isSelected,
                                                      boolean cellHasFocus)
        {
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
            panel.setOpaque(true);
            panel.setBackground(isSelected ? ColorScheme.DARKER_GRAY_COLOR : ColorScheme.DARK_GRAY_COLOR);

            Color typeColor;
            switch (marker.getType()) {
                case CRATE:
                    typeColor = new Color(255, 220, 60);
                    break;
                case RUM:
                    typeColor = new Color(80, 255, 80);
                    break;
                default:
                    typeColor = Color.LIGHT_GRAY;
                    break;
            }

            JLabel label = new JLabel(marker.getType() + " | X: " +
                    marker.getLocation().getX() + ", Y: " +
                    marker.getLocation().getY());
            label.setForeground(typeColor);

            panel.add(label);
            return panel;
        }
    }

    private class MarkerListTransferHandler extends TransferHandler
    {
        private int fromIndex = -1;

        @Override
        public int getSourceActions(JComponent c) { return MOVE; }

        @Override
        protected Transferable createTransferable(JComponent c)
        {
            fromIndex = markerList.getSelectedIndex();
            return new StringSelection("");
        }

        @Override
        public boolean canImport(TransferSupport support) { return support.isDrop(); }

        @Override
        public boolean importData(TransferSupport support)
        {
            if (!support.isDrop()) return false;
            JList.DropLocation dl = (JList.DropLocation) support.getDropLocation();
            int toIndex = dl.getIndex();
            if (fromIndex < 0 || toIndex == fromIndex) return false;

            PathMarker marker = markerListModel.get(fromIndex);
            markerListModel.remove(fromIndex);
            if (toIndex > fromIndex) toIndex--;
            markerListModel.add(toIndex, marker);

            trial.markers.clear();
            for (int i = 0; i < markerListModel.size(); i++)
                trial.markers.add(markerListModel.get(i));
            Routes.saveTrial(trial);

            return true;
        }
    }

    private void setupRightClickMenu()
    {
        JPopupMenu menu = new JPopupMenu();

        JMenu changeTypeMenu = new JMenu("Change Type");
        for (PathMarker.Type type : PathMarker.Type.values())
        {
            JMenuItem typeItem = new JMenuItem(type.name());
            typeItem.addActionListener(e -> {
                PathMarker selected = markerList.getSelectedValue();
                if (selected != null) {
                    selected.setType(type);
                    Routes.saveTrial(trial);
                    markerList.repaint();
                }
            });
            changeTypeMenu.add(typeItem);
        }

        JMenuItem deleteItem = new JMenuItem("Delete Marker");
        deleteItem.addActionListener(e -> {
            int index = markerList.getSelectedIndex();
            if (index != -1) {
                markerListModel.remove(index);
                trial.markers.remove(index);
                Routes.saveTrial(trial);
            }
        });

        menu.add(changeTypeMenu);
        menu.addSeparator();
        menu.add(deleteItem);

        markerList.addMouseListener(new java.awt.event.MouseAdapter()
        {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) { maybeShowPopup(e); }
            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) { maybeShowPopup(e); }

            private void maybeShowPopup(java.awt.event.MouseEvent e)
            {
                if (SwingUtilities.isRightMouseButton(e))
                {
                    int index = markerList.locationToIndex(e.getPoint());
                    if (index != -1) {
                        markerList.setSelectedIndex(index);
                        menu.show(markerList, e.getX(), e.getY());
                    }
                }
            }
        });
    }

    private void exportMarkers()
    {
        String json = Routes.GSON.toJson(trial);

        StringSelection selection = new StringSelection(json);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);

        JOptionPane.showMessageDialog(this, "Markers exported to clipboard.", "Export Complete", JOptionPane.INFORMATION_MESSAGE);
    }

    private void importMarkers()
    {
        try {
            String clipboard = (String) Toolkit.getDefaultToolkit()
                    .getSystemClipboard()
                    .getData(java.awt.datatransfer.DataFlavor.stringFlavor);

            Trial importedTrial = Routes.GSON.fromJson(clipboard, Trial.class);
            if (importedTrial == null) {
                Toolkit.getDefaultToolkit().beep();
                JOptionPane.showMessageDialog(this, "No valid markers found in clipboard.", "Import Failed", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Confirm before replacing existing markers
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "This will replace all existing markers. Continue?",
                    "Confirm Import",
                    JOptionPane.YES_NO_OPTION
            );
            if (confirm != JOptionPane.YES_OPTION) return;

            // Replace markers in current trial
            trial.markers.clear();
            markerListModel.clear();
            for (PathMarker pm : importedTrial.markers) {
                trial.markers.add(pm);
                markerListModel.addElement(pm);
            }
            trial.routeName = importedTrial.routeName;
            this.nameLabel.setText(importedTrial.routeName);
            Routes.saveTrial(trial);

            JOptionPane.showMessageDialog(this, "Markers imported successfully!", "Import Complete", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            ex.printStackTrace();
            Toolkit.getDefaultToolkit().beep();
            JOptionPane.showMessageDialog(this, "An error occurred while importing markers.", "Import Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

}
