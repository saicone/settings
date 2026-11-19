/**
 * This package contains the main objects related with settings API.
 *
 * <p>The important class here is {@link com.saicone.settings.SettingsData} which is used to load
 * data into a {@link com.saicone.settings.SettingsNode} and also write out.</p>
 *
 * <p>For memory-related features on complex path resolving, the {@link com.saicone.settings.SettingsMemory}
 * is used along with a {@link com.saicone.settings.Settings} object.</p>
 *
 * <p>For complex expression parsing, node updates and data manipulations on load time, the
 * {@link com.saicone.settings.SettingsLoader} object can be used along with {@link com.saicone.settings.SettingsParser}</p>
 *
 * @author Rubenicos
 */
package com.saicone.settings;