package dev.mikeya.barracudahelper;

import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.config.Config;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

import javax.inject.Inject;
import java.awt.*;
import java.util.List;

public class BarracudaHelperWorldOverlay extends Overlay {
    public static final int IMAGE_Z_OFFSET = 30;

    private final BarracudaHelperPlugin plugin;
    private final Client client;
    private final BarracudaHelperConfig config;

    @Inject
    public BarracudaHelperWorldOverlay(BarracudaHelperPlugin plugin, Client client, BarracudaHelperConfig config)
    {
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
        this.plugin = plugin;
        this.client = client;
        this.config = config;
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if (plugin.pluginPanel.editing != null)
        {
            drawLines(Color.RED, plugin.pluginPanel.editing.markers, graphics);

            for (PathMarker marker : plugin.pluginPanel.editing.markers)
            {
                WorldPoint wp = marker.getLocation();
                Color color;

                switch (marker.getType())
                {
                    case CRATE:
                        color = Color.YELLOW;
                        break;
                    case RUM:
                        color = Color.GREEN;
                        break;
                    default:
                        color = Color.LIGHT_GRAY;
                }

                drawTile(graphics, wp, color);
            }

            /*PathMarker selected = plugin.pluginPanel.getSelectedMarker();
            if (selected != null) {
                WorldPoint wp = selected.getLocation();
                drawTile(graphics, wp, Color.RED);
            }*/
        }
        else
        {
            drawLines(Color.YELLOW, plugin.beforeMarkers, graphics);
            drawLines(Color.GREEN, plugin.currentMarkers, graphics);
            drawLines(Color.BLUE, plugin.afterMarkers, graphics);
        }

        return null;
    }

    private void drawLines(Color color, List<PathMarker> markers, Graphics2D graphics)
    {
        graphics.setColor(color);
        graphics.setStroke(new BasicStroke(2));

        for (int i = 1; i < markers.size(); i++)
        {
            WorldPoint point1 = markers.get(i - 1).getLocation();
            WorldPoint point2 = markers.get(i).getLocation();

            Point start = worldToCanvas(point1);
            Point end = worldToCanvas(point2);

            if (start != null && end != null)
            {
                graphics.drawLine(start.getX(), start.getY(), end.getX(), end.getY());
            }
        }
    }

    private void drawTile(Graphics2D graphics, WorldPoint wp, Color color)
    {
        LocalPoint lp = LocalPoint.fromWorld(client, wp.getX(), wp.getY());
        if (lp == null)
        {
            return;
        }

        Polygon tilePoly = Perspective.getCanvasTilePoly(client, lp);
        if (tilePoly == null)
        {
            return;
        }

        graphics.setColor(color);
        graphics.setStroke(new BasicStroke(2));
        graphics.draw(tilePoly);

        Color fill = new Color(color.getRed(), color.getGreen(), color.getBlue(), 50);
        graphics.setColor(fill);
        graphics.fill(tilePoly);
    }

    private Point worldToCanvas(WorldPoint wp)
    {
        LocalPoint lp = LocalPoint.fromWorld(client, wp.getX(), wp.getY());
        if (lp == null)
        {
            return null;
        }

        return Perspective.localToCanvas(
                client,
                lp,
                wp.getPlane()
        );
    }
}
