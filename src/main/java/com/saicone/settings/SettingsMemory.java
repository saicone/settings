package com.saicone.settings;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface SettingsMemory {

    @Nullable
    SettingsNode get(@NotNull String id);

    void save(@NotNull String id, @NotNull SettingsNode node);

    void remove(@NotNull String id);

    void remove(@NotNull SettingsNode node);

    void clear();

}