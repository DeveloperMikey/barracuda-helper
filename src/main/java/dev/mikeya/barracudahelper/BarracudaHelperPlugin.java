package dev.mikeya.barracudahelper;

import com.google.inject.Provides;
import javax.inject.Inject;

import dev.mikeya.barracudahelper.enums.Paths;
import dev.mikeya.barracudahelper.ui.BarracudaHelperPluginPanel;
import dev.mikeya.barracudahelper.ui.EditorPanel;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.Menu;
import net.runelite.api.Point;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.util.ImageUtil;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@PluginDescriptor(
	name = "Barracuda Helper"
)
public class BarracudaHelperPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private BarracudaHelperConfig config;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	public ClientThread clientThread;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private ConfigManager configManager;

	@Inject
	private BarracudaHelperWorldOverlay barracudaHelperWorldOverlay;

	@Inject
	public Routes routes;

	public BarracudaHelperPluginPanel pluginPanel;
	private NavigationButton navigationButton;

	public List<PathMarker> beforeMarkers = new ArrayList<PathMarker>();
	public List<PathMarker> currentMarkers = new ArrayList<PathMarker>();
	public List<PathMarker> afterMarkers = new ArrayList<PathMarker>();

	public List<PathMarker> recordingMarkers = new ArrayList<PathMarker>();

	public int currentCrates = 0;
	public int currentRums = 0;

	@Override
	protected void startUp() throws Exception
	{
		overlayManager.add(barracudaHelperWorldOverlay);
		clientThread.invoke(() -> {
			recalculateVisibleMarkers();
		});

		pluginPanel = new BarracudaHelperPluginPanel(this);
		final BufferedImage icon = ImageUtil.loadImageResource(getClass(), "icon.png");
		navigationButton = NavigationButton.builder().tooltip("Barracuda Helper mapper").icon(icon).priority(5).panel(pluginPanel).build();
		clientToolbar.addNavigation(navigationButton);
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(barracudaHelperWorldOverlay);
		clientToolbar.removeNavigation(navigationButton);
		pluginPanel = null;
		navigationButton = null;
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged varbitChanged)
	{
		if (varbitChanged.getVarbitId() == VarbitID.SAILING_BT_IN_TRIAL) {
			recalculateVisibleMarkers();
		}
		if (varbitChanged.getVarbitId() >= VarbitID.SAILING_BT_OBJECTIVE0 && varbitChanged.getVarbitId() <= VarbitID.SAILING_BT_OBJECTIVE95)
		{
			log.debug("barracuda {} changed: {}", varbitChanged.getVarbitId(), varbitChanged.getValue());
			if (varbitChanged.getValue() == 0) {
				currentCrates += 1;
				recalculateVisibleMarkers();

			}
		}
		if (varbitChanged.getVarbitId() == VarbitID.CURRENT_HINT_ARROW && varbitChanged.getValue() == 0)
		{
			currentRums += 1;
			recalculateVisibleMarkers();
		}
	}

	@Subscribe
	public void onMenuEntryAdded(final MenuEntryAdded event)
	{
		if (pluginPanel.editing == null) {
			return;
		}

		final MenuEntry menuEntry = event.getMenuEntry();
		if (!Objects.equals(menuEntry.getOption(), "Set heading")) {return;}
		final Menu menu = client.getMenu();

		Point mousePos = client.getMouseCanvasPosition();
		Scene scene = client.getScene();
		Tile[][][] tiles = scene.getTiles();

		int z = client.getPlane();

		WorldPoint wp = null;
		for (int x = 0; x < Constants.SCENE_SIZE; ++x)
		{
			for (int y = 0; y < Constants.SCENE_SIZE; ++y)
			{
				Tile tile = tiles[z][x][y];

				if (tile == null)
				{
					continue;
				}

				Polygon poly = Perspective.getCanvasTilePoly(client, tile.getLocalLocation());
				if (poly != null &&
						poly.contains(client.getMouseCanvasPosition().getX(), client.getMouseCanvasPosition().getY()))
				{
					wp = tile.getWorldLocation();
					break;
				}
			}
		}

		if (wp != null)
		{
			WorldPoint finalWp = wp;
			menu.createMenuEntry(-1).setOption("Add Path").setTarget(menuEntry.getTarget()).setType(MenuAction.RUNELITE).onClick(e -> {
				log.debug("Position is {}", finalWp);

				pluginPanel.editing.markers.add(new PathMarker(finalWp, PathMarker.Type.PATH));
				if (pluginPanel.currentPanel instanceof EditorPanel) {
					((EditorPanel) pluginPanel.currentPanel).rebuildMarkerList();
				}
				Routes.saveTrial(pluginPanel.editing);
				log.debug(recordingMarkers.toString());
			});
		}

	}

	public void recalculateVisibleMarkers()
	{
		beforeMarkers = new ArrayList<>();
		currentMarkers = new ArrayList<>();
		afterMarkers = new ArrayList<>();

		int trial_num = client.getVarbitValue(VarbitID.SAILING_BT_IN_TRIAL);
		if (trial_num == 0) {
			currentCrates = 0;
			currentRums = 0;
			return;
		}

		Trial trial = Routes.TRIALS.get(trial_num - 1);

		if (trial != null) {
			if (currentCrates + currentRums > 0)
			{
				beforeMarkers = trial.getMarkersBetweenObjectives(currentRums + currentCrates - 1);
			}
			currentMarkers = trial.getMarkersBetweenObjectives(currentRums + currentCrates);
			afterMarkers = trial.getMarkersBetweenObjectives(currentRums + currentCrates + 1);
		}
	}

	@Provides
	BarracudaHelperConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(BarracudaHelperConfig.class);
	}
}
