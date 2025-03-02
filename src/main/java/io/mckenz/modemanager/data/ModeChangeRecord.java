package io.mckenz.modemanager.data;

import org.bukkit.GameMode;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Class to store mode change records
 */
public class ModeChangeRecord {
    private final GameMode gameMode;
    private final Instant timestamp;
    private final String reason;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());
    
    /**
     * Constructor for ModeChangeRecord
     * 
     * @param gameMode The game mode
     * @param timestamp The timestamp of the change
     * @param reason The reason for the change
     */
    public ModeChangeRecord(GameMode gameMode, Instant timestamp, String reason) {
        this.gameMode = gameMode;
        this.timestamp = timestamp;
        this.reason = reason;
    }
    
    /**
     * Get the game mode
     * 
     * @return The game mode
     */
    public GameMode getGameMode() {
        return gameMode;
    }
    
    /**
     * Get the timestamp
     * 
     * @return The timestamp
     */
    public Instant getTimestamp() {
        return timestamp;
    }
    
    /**
     * Get the reason
     * 
     * @return The reason
     */
    public String getReason() {
        return reason;
    }
    
    /**
     * Get a formatted timestamp
     * 
     * @return The formatted timestamp
     */
    public String getFormattedTimestamp() {
        return FORMATTER.format(timestamp);
    }
    
    /**
     * Get a string representation of the record
     * 
     * @return The string representation
     */
    @Override
    public String toString() {
        return getFormattedTimestamp() + " - " + gameMode.name() + " - " + reason;
    }
} 