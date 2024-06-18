package me.kenvera.chronocore.Object;

import net.luckperms.api.model.user.User;

import java.util.UUID;

public class PlayerData {
    private UUID uuid;
    private String playerName;
    private String address;
    private String inheritedGroup;

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getInheritedGroup() {
        return inheritedGroup;
    }

    public void setInheritedGroup(String inheritedGroup) {
        this.inheritedGroup = inheritedGroup;
    }
}
