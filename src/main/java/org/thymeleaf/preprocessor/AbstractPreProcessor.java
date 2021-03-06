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
package org.thymeleaf.preprocessor;

import org.thymeleaf.dialect.IPreProcessorDialect;
import org.thymeleaf.engine.ITemplateHandler;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.util.Validate;

/**
 *
 * @author Daniel Fern&aacute;ndez
 * @since 3.0.0
 *
 */
public abstract class AbstractPreProcessor implements IPreProcessor {

    private IPreProcessorDialect dialect;
    private final TemplateMode templateMode;
    private final Class<? extends ITemplateHandler> handlerClass;
    private final int precedence;



    public AbstractPreProcessor(
            final IPreProcessorDialect dialect, final TemplateMode templateMode,
            final Class<? extends ITemplateHandler> handlerClass, final int precedence) {

        super();

        Validate.notNull(dialect, "Dialect cannot be null");
        Validate.notNull(templateMode, "Template mode cannot be null");
        Validate.notNull(handlerClass, "Handler class cannot be null");

        this.dialect = dialect;
        this.templateMode = templateMode;
        this.handlerClass = handlerClass;
        this.precedence = precedence;

    }


    public final IPreProcessorDialect getDialect() {
        return this.dialect;
    }


    public final TemplateMode getTemplateMode() {
        return this.templateMode;
    }


    public final int getPrecedence() {
        return this.precedence;
    }


    public final Class<? extends ITemplateHandler> getHandlerClass() {
        return this.handlerClass;
    }

}
