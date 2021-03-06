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
package org.thymeleaf.engine;

import java.io.IOException;
import java.io.Writer;

import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.model.IDocType;
import org.thymeleaf.model.IModelVisitor;
import org.thymeleaf.text.ITextRepository;
import org.thymeleaf.util.TextUtils;
import org.thymeleaf.util.Validate;

/**
 *
 * @author Daniel Fern&aacute;ndez
 * @since 3.0.0
 * 
 */
final class DocType extends AbstractTemplateEvent implements IDocType, IEngineTemplateEvent {

    // DOCTYPE nodes do not exist in text parsing, so we are safe expliciting markup structures here
    public static final String DEFAULT_KEYWORD = "DOCTYPE";
    public static final String DEFAULT_ELEMENT_NAME = "html";
    public static final String DEFAULT_TYPE_PUBLIC = "PUBLIC";
    public static final String DEFAULT_TYPE_SYSTEM = "SYSTEM";

    private final ITextRepository textRepository;

    private String docType;
    private String keyword;
    private String elementName;
    private String type;
    private String publicId;
    private String systemId;
    private String internalSubset;


    /*
     * Objects of this class are meant to both be reused by the engine and also created fresh by the processors. This
     * should allow reducing the number of instances of this class to the minimum.
     *
     * The 'doctype' property, which is computed from the other properties, should be computed lazily in order to avoid
     * unnecessary creation of Strings which would use more memory than needed.
     */


    // Meant to be called only from the template handler adapter
    DocType(final ITextRepository textRepository) {
        super();
        this.textRepository = textRepository;
    }


    // Meant to be called only from the model factory
    DocType(final ITextRepository textRepository, final String publicId, final String systemId) {
        this(textRepository, DEFAULT_KEYWORD, DEFAULT_ELEMENT_NAME, computeType(publicId, systemId), publicId, systemId, null);
    }


    // Meant to be called only from the model factory
    DocType(
            final ITextRepository textRepository,
            final String keyword,
            final String elementName,
            final String type,
            final String publicId,
            final String systemId,
            final String internalSubset) {
        super();
        this.textRepository = textRepository;
        initializeFromDocType(keyword, elementName, type, publicId, systemId, internalSubset);
    }



    public String getKeyword() {
        return this.keyword;
    }

    public String getElementName() {
        return this.elementName;
    }

    public String getType() {
        return this.type;
    }

    public String getPublicId() {
        return this.publicId;
    }

    public String getSystemId() {
        return this.systemId;
    }

    public String getInternalSubset() {
        return this.internalSubset;
    }




    public String getDocType() {

        if (this.docType == null) {

            final StringBuilder strBuilder = new StringBuilder();

            strBuilder.append("<!");
            strBuilder.append(this.keyword);
            strBuilder.append(' ');
            strBuilder.append(this.elementName);
            if (this.type != null) {
                strBuilder.append(' ');
                strBuilder.append(type);
                if (this.publicId != null) {
                    strBuilder.append(" \"");
                    strBuilder.append(this.publicId);
                    strBuilder.append('"');
                }
                strBuilder.append(" \"");
                strBuilder.append(this.systemId);
                strBuilder.append('"');
            }
            if (this.internalSubset != null) {
                strBuilder.append(" [");
                strBuilder.append(this.internalSubset);
                strBuilder.append(']');
            }
            strBuilder.append('>');

            this.docType = this.textRepository.getText(strBuilder);

        }

        return this.docType;

    }




    public void setKeyword(final String keyword) {
        initializeFromDocType(keyword, this.elementName, this.type, this.publicId, this.systemId, this.internalSubset);
    }

    public void setElementName(final String elementName) {
        initializeFromDocType(this.keyword, elementName, this.type, this.publicId, this.systemId, this.internalSubset);
    }

    public void setToHTML5() {
        // This is just a convenience method with a nicer name
        setIDs(null, null);
    }

    public void setIDs(final String publicId, final String systemId) {
        initializeFromDocType(this.keyword, this.elementName, computeType(publicId, systemId), publicId, systemId, this.internalSubset);
    }

    public void setIDs(final String type, final String publicId, final String systemId) {
        initializeFromDocType(this.keyword, this.elementName, type, publicId, systemId, this.internalSubset);
    }

    public void setInternalSubset(final String internalSubset) {
        initializeFromDocType(this.keyword, this.elementName, this.type, this.publicId, this.systemId, internalSubset);
    }



    // Meant to be called only from within the engine - removes the need to validate compute the 'docType' field
    void reset(final String docType,
               final String keyword,
               final String elementName,
               final String type,
               final String publicId,
               final String systemId,
               final String internalSubset,
               final String templateName, final int line, final int col) {

        super.resetTemplateEvent(templateName, line, col);

        this.keyword = keyword;
        this.elementName = elementName;
        this.type = type;
        this.publicId = publicId;
        this.systemId = systemId;
        this.internalSubset = internalSubset;

        this.docType = docType;

    }



    private void initializeFromDocType(
            final String keyword,
            final String elementName,
            final String type,
            final String publicId,
            final String systemId,
            final String internalSubset) {

        if (keyword == null || !TextUtils.equals(false, DEFAULT_KEYWORD, keyword)) {
            throw new IllegalArgumentException("DOCTYPE keyword must be non-null and case-insensitively equal to '" + DEFAULT_KEYWORD + "'");
        }
        if (elementName == null || elementName.trim().length() == 0) {
            throw new IllegalArgumentException("DOCTYPE element name must be non-null and non-empty");
        }
        if (type != null && !(TextUtils.equals(false, DEFAULT_TYPE_PUBLIC, type) || TextUtils.equals(false, DEFAULT_TYPE_SYSTEM, type))) {
            throw new IllegalArgumentException("DOCTYPE type must be either null or equal either to '" + DEFAULT_TYPE_PUBLIC + "' or '" + DEFAULT_TYPE_SYSTEM + "'");
        }
        if (publicId != null && (type == null || TextUtils.equals(false, DEFAULT_TYPE_SYSTEM, type))) {
            throw new IllegalArgumentException("DOCTYPE PUBLIC ID cannot be non-null if type is null or equal to '" + DEFAULT_TYPE_SYSTEM + "'");
        }
        if (publicId == null && (type != null && TextUtils.equals(false, DEFAULT_TYPE_PUBLIC, type))) {
            throw new IllegalArgumentException("DOCTYPE PUBLIC ID cannot be null if type is equal to '" + DEFAULT_TYPE_PUBLIC + "'");
        }
        if (systemId != null && type == null) {
            throw new IllegalArgumentException("DOCTYPE SYSTEM ID cannot be non-null if type is null");
        }
        if (systemId == null && type != null) {
            throw new IllegalArgumentException("DOCTYPE SYSTEM ID cannot be null if type is non-null");
        }

        super.resetTemplateEvent(null, -1, -1);

        this.keyword = keyword;
        this.elementName = elementName;
        this.type = type;
        this.publicId = publicId;
        this.systemId = systemId;
        this.internalSubset = internalSubset;

        this.docType = null;

    }



    private static String computeType(final String publicId, final String systemId) {

        if (publicId != null && systemId == null) {
            throw new IllegalArgumentException(
                    "DOCTYPE clause cannot have a non-null PUBLIC ID and a null SYSTEM ID");
        }

        if (publicId == null && systemId == null) {
            return null;
        }

        if (publicId != null) {
            return DEFAULT_TYPE_PUBLIC;
        }

        return DEFAULT_TYPE_SYSTEM;

    }






    public void accept(final IModelVisitor visitor) {
        visitor.visit(this);
    }


    public void write(final Writer writer) throws IOException {
        Validate.notNull(writer, "Writer cannot be null");
        writer.write(getDocType());
    }






    public DocType cloneEvent() {
        final DocType clone = new DocType(this.textRepository);
        clone.resetAsCloneOf(this);
        return clone;
    }



    // Meant to be called only from within the engine
    void resetAsCloneOf(final DocType original) {

        super.resetAsCloneOfTemplateEvent(original);
        this.docType = original.docType;
        this.keyword = original.keyword;
        this.elementName = original.elementName;
        this.type = original.type;
        this.publicId = original.publicId;
        this.systemId = original.systemId;
        this.internalSubset = original.internalSubset;

    }



    // Meant to be called only from within the engine
    static DocType asEngineDocType(
            final IEngineConfiguration configuration, final IDocType docType, final boolean cloneAlways) {

        if (docType instanceof DocType) {
            if (cloneAlways) {
                return ((DocType) docType).cloneEvent();
            }
            return (DocType) docType;
        }

        final DocType newInstance = new DocType(configuration.getTextRepository());
        newInstance.docType = docType.getDocType();
        newInstance.keyword = docType.getKeyword();
        newInstance.elementName = docType.getElementName();
        newInstance.type = docType.getType();
        newInstance.publicId = docType.getPublicId();
        newInstance.systemId = docType.getSystemId();
        newInstance.internalSubset = docType.getInternalSubset();
        newInstance.resetTemplateEvent(docType.getTemplateName(), docType.getLine(), docType.getCol());
        return newInstance;

    }


}
