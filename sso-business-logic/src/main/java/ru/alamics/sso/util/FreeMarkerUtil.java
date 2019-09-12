package ru.alamics.sso.util;

import freemarker.cache.FileTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.cache.URLTemplateLoader;
import freemarker.core.HTMLOutputFormat;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.keycloak.theme.FreeMarkerException;


import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;

import static freemarker.template.Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS;

public class FreeMarkerUtil {
    private FreeMarkerUtil () { }

    public static String processTemplate(Object data, String templateName) throws FreeMarkerException {
        try {
            Template template = getTemplate(templateName);
            Writer out = new StringWriter();
            template.process(data, out);
            return out.toString();
        } catch (Exception e) {
            throw new FreeMarkerException("Failed to process template " + templateName, e);
        }
    }

    private static Template getTemplate (final String templateName) throws IOException {
        Configuration cfg = new Configuration(DEFAULT_INCOMPATIBLE_IMPROVEMENTS);

        // Assume *.ftl files are html.  This lets freemarker know how to
        // sanitize and prevent XSS attacks.
        if (templateName.toLowerCase().endsWith(".ftl")) {
            cfg.setOutputFormat(HTMLOutputFormat.INSTANCE);
        }

        URL templateUrl = FreeMarkerUtil.class.getClassLoader().getResource(templateName);
        TemplateLoader templateLoader = new URLTemplateLoader() {
            @Override
            protected URL getURL (String name) {
                return templateUrl;
            }
        };

        cfg.setTemplateLoader(templateLoader);
        return cfg.getTemplate(templateName, "UTF-8");
    }


}
