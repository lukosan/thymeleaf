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
package org.thymeleaf.processor.element;

import org.thymeleaf.context.ITemplateProcessingContext;
import org.thymeleaf.dialect.IProcessorDialect;
import org.thymeleaf.engine.AttributeNames;
import org.thymeleaf.engine.ElementNames;
import org.thymeleaf.exceptions.TemplateProcessingException;
import org.thymeleaf.model.IModel;
import org.thymeleaf.model.ITemplateEvent;
import org.thymeleaf.processor.AbstractProcessor;
import org.thymeleaf.templatemode.TemplateMode;

/**
 *
 * @author Daniel Fern&aacute;ndez
 *
 * @since 3.0.0
 *
 */
public abstract class AbstractElementModelProcessor
        extends AbstractProcessor implements IElementModelProcessor {

    private final String dialectPrefix;
    private final String elementName;
    private final String attributeName;
    private final MatchingElementName matchingElementName;
    private final MatchingAttributeName matchingAttributeName;



    public AbstractElementModelProcessor(
            final IProcessorDialect dialect, final TemplateMode templateMode, final String dialectPrefix,
            final String elementName, final boolean prefixElementName,
            final String attributeName, final boolean prefixAttributeName,
            final int precedence) {

        super(dialect, templateMode, precedence);

        this.dialectPrefix = dialectPrefix;

        this.elementName = elementName;
        this.attributeName = attributeName;

        this.matchingElementName =
                (this.elementName == null?
                    null :
                    MatchingElementName.forElementName(
                            templateMode, ElementNames.forName(templateMode, (prefixElementName? this.dialectPrefix : null), this.elementName)));
        this.matchingAttributeName =
                (this.attributeName == null?
                    null :
                    MatchingAttributeName.forAttributeName(
                            templateMode, AttributeNames.forName(templateMode, (prefixAttributeName? this.dialectPrefix : null), this.attributeName)));

    }


    public final MatchingElementName getMatchingElementName() {
        return this.matchingElementName;
    }


    public final MatchingAttributeName getMatchingAttributeName() {
        return this.matchingAttributeName;
    }



    public final void process(
            final ITemplateProcessingContext processingContext,
            final IModel model, final IElementModelStructureHandler structureHandler) {

        String modelTemplateName = null;
        int modelLine = -1;
        int modelCol = -1;
        try {

            final ITemplateEvent firstEvent = model.get(0);
            modelTemplateName = firstEvent.getTemplateName();
            modelLine = firstEvent.getLine();
            modelCol = firstEvent.getLine();

            doProcess(processingContext, model, modelTemplateName, modelLine, modelCol, structureHandler);

        } catch (final TemplateProcessingException e) {

            if (modelTemplateName != null) {
                if (!e.hasTemplateName()) {
                    e.setTemplateName(modelTemplateName);
                }
            }
            if (modelLine != -1 && modelCol != -1) {
                if (!e.hasLineAndCol()) {
                    e.setLineAndCol(modelLine, modelCol);
                }
            }
            throw e;

        } catch (final Exception e) {

            throw new TemplateProcessingException(
                    "Error during execution of processor '" + this.getClass().getName() + "'", modelTemplateName, modelLine, modelCol, e);

        }

    }


    protected abstract void doProcess(
            final ITemplateProcessingContext processingContext,
            final IModel model,
            final String modelTemplateName, final int modelLine, final int modelCol,
            final IElementModelStructureHandler structureHandler);

}
