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
package org.thymeleaf.standard.processor;

import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.context.ITemplateProcessingContext;
import org.thymeleaf.dialect.IProcessorDialect;
import org.thymeleaf.engine.AttributeName;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.AbstractAttributeTagProcessor;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.standard.expression.IStandardExpression;
import org.thymeleaf.standard.expression.IStandardExpressionParser;
import org.thymeleaf.standard.expression.StandardExpressions;
import org.thymeleaf.templatemode.TemplateMode;

/**
 *
 * @author Daniel Fern&aacute;ndez
 *
 * @since 3.0.0
 *
 */
public abstract class AbstractStandardExpressionAttributeTagProcessor extends AbstractAttributeTagProcessor {



    protected AbstractStandardExpressionAttributeTagProcessor(
            final IProcessorDialect dialect, final TemplateMode templateMode, final String dialectPrefix,
            final String attrName, final int precedence, final boolean removeAttribute) {
        super(dialect, templateMode, dialectPrefix, null, false, attrName, true, precedence, removeAttribute);
    }



    @Override
    protected final void doProcess(
            final ITemplateProcessingContext processingContext,
            final IProcessableElementTag tag,
            final AttributeName attributeName,
            final String attributeValue,
            final String attributeTemplateName, final int attributeLine, final int attributeCol,
            final IElementTagStructureHandler structureHandler) {

        final IEngineConfiguration configuration = processingContext.getConfiguration();
        final IStandardExpressionParser expressionParser = StandardExpressions.getExpressionParser(configuration);

        final Object expressionResult;
        if (attributeValue != null) {
            final IStandardExpression expression = expressionParser.parseExpression(processingContext, attributeValue);
            expressionResult = expression.execute(processingContext);
        } else {
            expressionResult = null;
        }

        doProcess(
                processingContext, tag,
                attributeName, attributeValue, attributeTemplateName, attributeLine, attributeCol,
                expressionResult, structureHandler);

    }


    protected abstract void doProcess(
            final ITemplateProcessingContext processingContext,
            final IProcessableElementTag tag,
            final AttributeName attributeName,
            final String attributeValue,
            final String attributeTemplateName, final int attributeLine, final int attributeCol,
            final Object expressionResult,
            final IElementTagStructureHandler structureHandler);


}
