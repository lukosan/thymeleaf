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
package org.thymeleaf.model;

import java.io.IOException;
import java.io.Writer;

import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.templatemode.TemplateMode;


/**
 *
 * @author Daniel Fern&aacute;ndez
 * @since 3.0.0
 *
 */
public interface IModel {


    public IEngineConfiguration getConfiguration();

    public TemplateMode getTemplateMode();

    public int size();

    public ITemplateEvent get(final int pos);

    public void add(final ITemplateEvent event);
    public void insert(final int pos, final ITemplateEvent event);

    public void addModel(final IModel model);
    public void insertModel(final int pos, final IModel model);

    public void remove(final int pos);

    public void reset();

    public IModel cloneModel();

    public void accept(final IModelVisitor visitor);

    public void write(final Writer writer) throws IOException;

}