package vista;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * Custom ResourceBundle.Control that reads .properties files using UTF-8
 * encoding instead of the default ISO-8859-1.
 *
 * This is necessary to load Japanese (and other non-Latin) characters
 * directly from the properties files without using unicode escape sequences.
 */
public class UTF8Control extends ResourceBundle.Control {

    @Override
    public ResourceBundle newBundle(String baseName, Locale locale, String format,
                                    ClassLoader loader, boolean reload) throws IOException {

        String bundleName  = toBundleName(baseName, locale);
        String resourceName = toResourceName(bundleName, "properties");

        try (InputStream is = loader.getResourceAsStream(resourceName)) {
            if (is != null) {
                return new PropertyResourceBundle(
                        new InputStreamReader(is, StandardCharsets.UTF_8));
            }
        }
        return null; // fall back to parent bundle
    }
}
