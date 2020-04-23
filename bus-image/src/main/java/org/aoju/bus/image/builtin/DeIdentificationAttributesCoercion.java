/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 ********************************************************************************/
package org.aoju.bus.image.builtin;

import org.aoju.bus.image.galaxy.data.Attributes;
import org.aoju.bus.image.galaxy.data.AttributesCoercion;

/**
 * @author Kimi Liu
 * @version 5.8.8
 * @since JDK 1.8+
 */
public class DeIdentificationAttributesCoercion implements AttributesCoercion {

    private final DeIdentifier deIdentifier;
    private final AttributesCoercion next;

    public DeIdentificationAttributesCoercion(DeIdentifier deIdentifier, AttributesCoercion next) {
        this.deIdentifier = deIdentifier;
        this.next = next;
    }

    public static AttributesCoercion valueOf(DeIdentifier.Option[] options, AttributesCoercion next) {
        return options != null && options.length > 0
                ? new DeIdentificationAttributesCoercion(new DeIdentifier(options), next)
                : next;
    }

    @Override
    public String remapUID(String uid) {
        String remappedUID = deIdentifier.remapUID(uid);
        return next != null ? next.remapUID(remappedUID) : remappedUID;
    }

    @Override
    public void coerce(Attributes attrs, Attributes modified) {
        deIdentifier.deidentify(attrs);
        if (next != null)
            next.coerce(attrs, modified);
    }

}
