package ru.alamics.sso.keycloak.theme;

import org.keycloak.theme.Theme;
import org.keycloak.theme.ThemeProvider;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * @see org.keycloak.theme.ClasspathThemeProvider
 */
public class CustomClasspathThemeProvider implements ThemeProvider {
    private final Map<Theme.Type, Map<String, CustomClassLoaderTheme>> themes;

    public CustomClasspathThemeProvider(Map<Theme.Type, Map<String, CustomClassLoaderTheme>> themes) {
        this.themes = themes;
    }

    @Override
    public int getProviderPriority() {
        return 1;
    }

    @Override
    public Theme getTheme(String name, Theme.Type type) {
        return hasTheme(name, type) ? themes.get(type).get(name) : null;
    }

    @Override
    public Set<String> nameSet(Theme.Type type) {
        if (themes.containsKey(type)) {
            return themes.get(type).keySet();
        } else {
            return Collections.emptySet();
        }
    }

    @Override
    public boolean hasTheme(String name, Theme.Type type) {
        return themes.containsKey(type) && themes.get(type).containsKey(name);
    }

    @Override
    public void close() {}
}
