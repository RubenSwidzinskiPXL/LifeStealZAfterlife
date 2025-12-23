package com.zetaplugins.lifestealz.afterlife;

/**
 * Represents the different life states a player can be in.
 */
public enum LifeState {
    /**
     * Player is alive and playing normally in the SMP world.
     */
    ALIVE,
    
    /**
     * Player has reached 0 hearts and is currently in the afterlife world.
     */
    AFTERLIFE,
    
    /**
     * Player has been permanently eliminated (banned).
     */
    ELIMINATED
}
