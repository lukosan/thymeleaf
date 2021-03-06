/*
 * =============================================================================
 *
 *   Copyright (c) 2011-2014, The THYMELEAF team (http://www.thymeleaf.org)
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 * =============================================================================
 */
package org.thymeleaf.cache;

import java.util.Arrays;

import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.util.LoggingUtils;
import org.thymeleaf.util.Validate;


/**
 * <p>
 *   This class models objects used as keys in the Fragment Cache.
 * </p>
 *
 * @author Daniel Fern&aacute;ndez
 *
 * @since 3.0.0
 */
public final class FragmentCacheKey {

    private final String ownerTemplate;
    private final String fragment;
    private final String[] selectors;
    private final int lineOffset;
    private final int colOffset;
    private final TemplateMode templateMode;




    public FragmentCacheKey(
            final String ownerTemplate, final String fragment, final String[] selectors,
            final int lineOffset, final int colOffset, final TemplateMode templateMode) {

        super();

        // ownerTemplate will be null if this fragment is actually a standalone template (that will be included as a template somewhere else)
        Validate.notNull(fragment, "Fragment cannot be null");
        // selectors can be null if we are selecting the entire fragment
        // templateMode can be null if this fragment is standalone (no owner template) AND we are forcing a specific template mode for its processing

        this.ownerTemplate = ownerTemplate;
        this.fragment = fragment;
        if (selectors != null && selectors.length > 0) {
            this.selectors = selectors.clone();
            Arrays.sort(this.selectors); // we need to sort this so that Arrays.sort() works in equals()
        } else {
            this.selectors = null;
        }
        this.lineOffset = lineOffset;
        this.colOffset = colOffset;
        this.templateMode = templateMode;

    }


    public String getOwnerTemplate() {
        return this.ownerTemplate;
    }

    public String[] getSelectors() {
        return this.selectors;
    }

    public String getFragment() {
        return this.fragment;
    }

    public int getLineOffset() {
        return this.lineOffset;
    }

    public int getColOffset() {
        return this.colOffset;
    }

    public TemplateMode getTemplateMode() {
        return this.templateMode;
    }




    @Override
    public boolean equals(final Object o) {

        if (this == o) {
            return true;
        }

        if (!(o instanceof FragmentCacheKey)) {
            return false;
        }

        final FragmentCacheKey that = (FragmentCacheKey) o;

        if (this.lineOffset != that.lineOffset) {
            return false;
        }
        if (this.colOffset != that.colOffset) {
            return false;
        }
        if (this.ownerTemplate != null ? !this.ownerTemplate.equals(that.ownerTemplate) : that.ownerTemplate != null) {
            return false;
        }
        if (!this.fragment.equals(that.fragment)) {
            return false;
        }
        if (!Arrays.equals(this.selectors, that.selectors)) { // Works because we ordered the arrays
            return false;
        }
        return this.templateMode == that.templateMode;

    }


    @Override
    public int hashCode() {
        int result = this.ownerTemplate != null ? this.ownerTemplate.hashCode() : 0;
        result = 31 * result + this.fragment.hashCode();
        result = 31 * result + (this.selectors != null ? Arrays.hashCode(this.selectors) : 0);
        result = 31 * result + this.lineOffset;
        result = 31 * result + this.colOffset;
        result = 31 * result + (this.templateMode != null ? this.templateMode.hashCode() : 0);
        return result;
    }




    @Override
    public String toString() {
        final StringBuilder strBuilder = new StringBuilder();
        strBuilder.append(LoggingUtils.loggifyTemplateName(this.fragment));
        if (this.ownerTemplate != null) {
            strBuilder.append('@');
            strBuilder.append('(');
            strBuilder.append(LoggingUtils.loggifyTemplateName(this.ownerTemplate));
            strBuilder.append(';');
            strBuilder.append(this.lineOffset);
            strBuilder.append(',');
            strBuilder.append(this.colOffset);
            strBuilder.append(')');
        }
        if (this.selectors != null && this.selectors.length > 0) {
            strBuilder.append("::");
            strBuilder.append(Arrays.toString(this.selectors));
        }
        if (this.templateMode != null) {
            strBuilder.append("@(");
            strBuilder.append(this.templateMode);
            strBuilder.append(")");
        }
        return strBuilder.toString();
    }

}
