/*************************************************************************
 *
 * ADOBE CONFIDENTIAL
 * ___________________
 *
 *  Copyright 2017 Adobe Systems Incorporated
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Adobe Systems Incorporated and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Adobe Systems Incorporated and its
 * suppliers and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Adobe Systems Incorporated.
 **************************************************************************/
package org.apache.sling.systemreadiness.core.osgi;

import static org.ops4j.pax.exam.CoreOptions.streamBundle;
import static org.ops4j.pax.tinybundles.core.TinyBundles.withBnd;

import org.ops4j.pax.exam.Option;
import org.ops4j.pax.tinybundles.core.TinyBundle;

public class BndDSOptions {

    public static Option dsBundle(String symbolicName, TinyBundle bundleDef) {
        return streamBundle(bundleDef
                .symbolicName(symbolicName)
                .build(withBnd()));
    }
}
