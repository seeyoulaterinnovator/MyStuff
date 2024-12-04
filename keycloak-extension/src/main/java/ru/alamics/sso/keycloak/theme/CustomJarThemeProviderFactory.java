package ru.alamics.sso.keycloak.theme;

import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.theme.ClasspathThemeProviderFactory;
import org.keycloak.theme.JarThemeProviderFactory;
import org.keycloak.theme.Theme;
import org.keycloak.theme.ThemeProvider;
import org.keycloak.util.JsonSerialization;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * @see JarThemeProviderFactory
 */
public class CustomJarThemeProviderFactory extends ClasspathThemeProviderFactory {
    public static final String KEYCLOAK_THEMES_JSON = "META-INF/keycloak-themes-custom.json";

    protected static Map<Theme.Type, Map<String, CustomClassLoaderTheme>> themes = new HashMap<>();

    public CustomJarThemeProviderFactory() {
        super("customJar");
    }

    @Override
    public ThemeProvider create(KeycloakSession session) {
        return new CustomClasspathThemeProvider(themes);
    }

    @Override
    public void init(Config.Scope config) {
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            Enumeration<URL> resources = classLoader.getResources(KEYCLOAK_THEMES_JSON);
            while (resources.hasMoreElements()) {
                loadThemes(classLoader, resources.nextElement().openStream());
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load themes", e);
        }
    }

    @Override
    public String getId() {
        return "custom";
    }

    protected void loadThemes(ClassLoader classLoader, InputStream themesInputStream) {
        try {
            loadThemes(classLoader, JsonSerialization.readValue(themesInputStream, ClasspathThemeProviderFactory.ThemesRepresentation.class));
        } catch (Exception e) {
            throw new RuntimeException("Failed to load themes", e);
        }
    }

    protected void loadThemes(ClassLoader classLoader, ClasspathThemeProviderFactory.ThemesRepresentation themesRep) {
        try {
            for (ClasspathThemeProviderFactory.ThemeRepresentation themeRep : themesRep.getThemes()) {
                for (String t : themeRep.getTypes()) {
                    Theme.Type type = Theme.Type.valueOf(t.toUpperCase());
                    if (!themes.containsKey(type)) {
                        themes.put(type, new HashMap<>());
                    }
                    themes.get(type).put(themeRep.getName(), new CustomClassLoaderTheme(themeRep.getName(), type, classLoader));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load themes", e);
        }
    }
}
