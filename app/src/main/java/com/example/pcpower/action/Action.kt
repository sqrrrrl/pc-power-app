package com.example.pcpower.action

enum class Action(val text: String) {
    DISMISS("Dismiss"),
    POWER_ON("Power on"),
    POWER_OFF("Power off"),
    REBOOT("Reboot"),
    FORCE_SHUTDOWN("Force shutdown"),
    RENAME("Rename"),
    DELETE("Delete"),
    CREATE("Create")
}