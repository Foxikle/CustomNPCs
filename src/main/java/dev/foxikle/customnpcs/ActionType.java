package dev.foxikle.customnpcs;

public enum ActionType {
    RUN_COMMAND,
    SEND_MESSAGE,
    DISPLAY_TITLE,
    ACTION_BAR,
    TOGGLE_FOLLOWING,
    PLAY_SOUND,
    TELEPORT,
    SEND_TO_SERVER;

    public ActionType ofString(String str){
        for (ActionType actionType : ActionType.values()) {
            if(actionType.name().equalsIgnoreCase(str))
                return actionType;
        }
        return null;
    }
}
