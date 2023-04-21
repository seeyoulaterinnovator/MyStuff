package ru.alamics.sso.util;

import freemarker.cache.ClassTemplateLoader;
import freemarker.core.HTMLOutputFormat;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.keycloak.theme.FreeMarkerException;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;


public class CustomFreeMarkerUtil {

    private final static String THEME_DOMRU = "/themes/domru/email";
    private final static String THEME_STELECOM = "/themes/stelecom/email";
    private final static String THEME_ERTELECOM = "/themes/ertelecom/email";
    private final static String DOMRU = "user";
    private final static String STELECOM = "S-TELECOM";
    private final static String ERTELECOM = "ER-TELECOM";

    private final static String[] REALM_NAMES = {"user", "S-TELECOM", "ERTELECOM"};

    private CustomFreeMarkerUtil() {
    }

    public static String processTemplate(Object data, String templateName, String realmName) throws FreeMarkerException {
        try {
            Template template = getTemplate(templateName, realmName);
            Writer out = new StringWriter();
            template.process(data, out);
            return out.toString();
        } catch (Exception e) {
            throw new FreeMarkerException("Failed to process template " + templateName, e);
        }
    }

    private static Template getTemplate(final String templateName, String realmName) throws IOException {
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_23);
        cfg.setLocalizedLookup(false);
        cfg.setDefaultEncoding("UTF-8");
        String pathTheme = "/themes/domru/email";
        for (String realm : REALM_NAMES) {
            if (realm.equals(realmName)) {
                switch (realm) {
                    case DOMRU:
                        pathTheme = THEME_DOMRU;
                        break;
                    case STELECOM:
                        pathTheme = THEME_STELECOM;
                        break;
                    case ERTELECOM:
                        pathTheme = THEME_ERTELECOM;
                        break;
                }
            }
        }

        ClassTemplateLoader ctl = new ClassTemplateLoader(CustomFreeMarkerUtil.class, pathTheme);
        cfg.setTemplateLoader(ctl);

        if (templateName.toLowerCase().endsWith(".ftl")) {
            cfg.setOutputFormat(HTMLOutputFormat.INSTANCE);
        }

        return cfg.getTemplate(templateName, "UTF-8");
    }


}
