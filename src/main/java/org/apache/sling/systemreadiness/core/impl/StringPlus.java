package org.apache.sling.systemreadiness.core.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class StringPlus {

    private static final Logger LOG = LoggerFactory.getLogger(StringPlus.class);

    private StringPlus() {
        // never constructed
    }

    @SuppressWarnings("rawtypes")
    public static List<String> normalize(Object object) {
        if (object instanceof String) {
            String s = (String)object;
            String[] values = s.split(",");
            List<String> list = new ArrayList<String>();
            for (String val : values) {
                String actualValue = val.trim();
                if (!actualValue.isEmpty()) {
                    list.add(actualValue);
                }
            }
            return list;
        }

        if (object instanceof String[]) {
            return Arrays.asList((String[])object);
        }
        
        if (object instanceof Collection) {
            Collection col = (Collection)object;
            List<String> ar = new ArrayList<String>(col.size());
            for (Object o : col) {
                if (o instanceof String) {
                    String s = (String)o;
                    ar.add(s);
                } else {
                    LOG.warn("stringPlus contained non string element in list! Element was skipped");
                }
            }
            return ar;
        }

        return null;
    }

}
