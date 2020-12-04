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
    private CustomFreeMarkerUtil() { }

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

    private static Template getTemplate(final String templateName) throws IOException {
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_23);
        cfg.setLocalizedLookup(false);
        cfg.setDefaultEncoding("UTF-8");
        ClassTemplateLoader ctl = new ClassTemplateLoader(CustomFreeMarkerUtil.class, "/templates/mail");
        cfg.setTemplateLoader(ctl);

        if (templateName.toLowerCase().endsWith(".ftl")) {
            cfg.setOutputFormat(HTMLOutputFormat.INSTANCE);
        }

        return cfg.getTemplate(templateName, "UTF-8");
    }


}
