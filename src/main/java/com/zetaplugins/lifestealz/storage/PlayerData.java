package com.zetaplugins.lifestealz.storage;

import com.zetaplugins.lifestealz.afterlife.LifeState;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PlayerData {
    private final String name;
    private final String uuid;
    private double maxHealth = 20;
    private int craftedHearts;
    private int craftedRevives;
    private int hasBeenRevived;
    private int killedOtherPlayers;
    private long firstJoin;
    private LifeState lifeState = LifeState.ALIVE;
    private long afterlifeReleaseTime = 0L;
    private int prestigeCount = 0;

    private final Set<String> modifiedFields = new HashSet<>(); // Track modified fields

    public PlayerData(String name, UUID uuid) {
        this.name = name;
        this.uuid = uuid.toString();
    }

    public String getName() {
        return name;
    }

    public String getUuid() {
        return uuid;
    }

    public double getMaxHealth() {
        return maxHealth;
    }

    public void setMaxHealth(double maxHealth) {
        if (this.maxHealth != maxHealth) {
            this.maxHealth = maxHealth;
            modifiedFields.add("maxhp");
        }
    }

    public int getCraftedHearts() {
        return craftedHearts;
    }

    public void setCraftedHearts(int craftedHearts) {
        if (this.craftedHearts != craftedHearts) {
            this.craftedHearts = craftedHearts;
            modifiedFields.add("craftedHearts");
        }
    }

    public int getCraftedRevives() {
        return craftedRevives;
    }

    public void setCraftedRevives(int craftedRevives) {
        if (this.craftedRevives != craftedRevives) {
            this.craftedRevives = craftedRevives;
            modifiedFields.add("craftedRevives");
        }
    }

    public int getHasBeenRevived() {
        return hasBeenRevived;
    }

    public void setHasBeenRevived(int hasBeenRevived) {
        if (this.hasBeenRevived != hasBeenRevived) {
            this.hasBeenRevived = hasBeenRevived;
            modifiedFields.add("hasbeenRevived");
        }
    }

    public int getKilledOtherPlayers() {
        return killedOtherPlayers;
    }

    public void setKilledOtherPlayers(int killedOtherPlayers) {
        if (this.killedOtherPlayers != killedOtherPlayers) {
            this.killedOtherPlayers = killedOtherPlayers;
            modifiedFields.add("killedOtherPlayers");
        }
    }

    public long getFirstJoin() {
        return firstJoin;
    }

    public void setFirstJoin(long firstJoin) {
        if (this.firstJoin != firstJoin) {
            this.firstJoin = firstJoin;
            modifiedFields.add("firstJoin");
        }
    }

    public LifeState getLifeState() {
        return lifeState;
    }

    public void setLifeState(LifeState lifeState) {
        if (this.lifeState != lifeState) {
            this.lifeState = lifeState;
            modifiedFields.add("lifeState");
        }
    }

    public long getAfterlifeReleaseTime() {
        return afterlifeReleaseTime;
    }

    public void setAfterlifeReleaseTime(long afterlifeReleaseTime) {
        if (this.afterlifeReleaseTime != afterlifeReleaseTime) {
            this.afterlifeReleaseTime = afterlifeReleaseTime;
            modifiedFields.add("afterlifeReleaseTime");
        }
    }

    public boolean isAfterlife() {
        return lifeState == LifeState.AFTERLIFE;
    }

    public boolean isEliminated() {
        return lifeState == LifeState.ELIMINATED;
    }

    public boolean isAlive() {
        return lifeState == LifeState.ALIVE;
    }

    public int getPrestigeCount() {
        return prestigeCount;
    }

    public void setPrestigeCount(int prestigeCount) {
        if (this.prestigeCount != prestigeCount) {
            this.prestigeCount = prestigeCount;
            modifiedFields.add("prestigeCount");
        }
    }

    public boolean hasChanges() {
        return !modifiedFields.isEmpty();
    }

    public Set<String> getModifiedFields() {
        return new HashSet<>(modifiedFields);
    }

    public void clearModifiedFields() {
        modifiedFields.clear();
    }
}