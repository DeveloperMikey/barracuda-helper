package dev.mikeya.barracudahelper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.runelite.client.config.ConfigManager;

import javax.inject.Inject;
import java.util.ArrayList;

public class Routes {
    public static final ArrayList<Trial> TRIALS = new ArrayList<>();
    private static ConfigManager configManager = null;
    private static BarracudaHelperPlugin plugin = null;

    @Inject
    public Routes(ConfigManager cm, BarracudaHelperPlugin plugin)
    {
        this.configManager = cm;
        this.plugin = plugin;

        String[] names = {
                "Tempor unranked",
                "Tempor swordfish",
                "Tempor shark",
                "Tempor marlin",

                "Jubbly unranked",
                "Jubbly swordfish",
                "Jubbly shark",
                "Jubbly marlin",

                "Gwenith unranked",
                "Gwenith swordfish",
                "Gwenith shark",
                "Gwenith marlin"
        };

        for (String name : names)
        {
            Trial trial;

            String json = cm.getConfiguration("barracudahelper", name);
            if (json != null && !json.isEmpty())
            {
                try
                {
                    trial = plugin.gson.fromJson(json, Trial.class);
                }
                catch (Exception e)
                {
                    trial = new Trial("Default", name);
                }
            }
            else
            {
                trial = loadDefaultTrial(name);
            }

            TRIALS.add(trial);
        }
    }

    public static void saveTrial(Trial trial)
    {
        String json = plugin.gson.toJson(trial);
        configManager.setConfiguration("barracudahelper", trial.trialName, json);
    }

    private Trial loadDefaultTrial(String name)
    {
        try (var stream = getClass().getResourceAsStream("/dev/mikeya/barracudahelper/defaults/" + name.replace(" ", "_") + ".json"))
        {
            if (stream != null)
            {
                String json = new String(stream.readAllBytes());
                return plugin.gson.fromJson(json, Trial.class);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return new Trial("Default", name);
    }
}