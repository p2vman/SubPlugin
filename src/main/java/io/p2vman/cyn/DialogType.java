package io.p2vman.cyn;

public enum DialogType {
    NOTICE("minecraft:notice"),
    SERVER_LINKS("minecraft:server_links"),
    DIALOG_LIST("minecraft:dialog_list"),
    MULTI_ACTION("minecraft:multi_action"),
    CONFIRMATION("minecraft:confirmation");

    public final String id;
    DialogType(String id) {
        this.id = id;
    }
}
