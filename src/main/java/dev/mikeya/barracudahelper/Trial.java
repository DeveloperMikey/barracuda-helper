package dev.mikeya.barracudahelper;

import java.util.ArrayList;
import java.util.List;

public class Trial {
    public String routeName;
    public final String trialName;
    public ArrayList<PathMarker> markers;

    public Trial(String routeName, String trialName)
    {
        this.routeName = routeName;
        this.trialName = trialName;
        this.markers = new ArrayList<>();
    }

    public List<PathMarker> getMarkersBetweenObjectives(int objectiveIndex) {
        List<Integer> objectiveIndices = new ArrayList<>();
        for (int i = 0; i < this.markers.size(); i++) {
            if (this.markers.get(i).getType() != PathMarker.Type.PATH) {
                objectiveIndices.add(i);
            }
        }

        if (objectiveIndices.isEmpty()) {
            return List.of();
        }

        if (objectiveIndex == 0) {
            int end = objectiveIndices.get(0);
            return this.markers.subList(0, end + 1);
        }

        if (objectiveIndex > 0 && objectiveIndex < objectiveIndices.size()) {
            int start = objectiveIndices.get(objectiveIndex - 1);
            int end = objectiveIndices.get(objectiveIndex);
            return this.markers.subList(start, end + 1);
        }

        return List.of();
    }

}
