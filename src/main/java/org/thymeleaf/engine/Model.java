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
import java.io.StringWriter;
import java.io.Writer;

import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.exceptions.TemplateProcessingException;
import org.thymeleaf.model.ICDATASection;
import org.thymeleaf.model.ICloseElementTag;
import org.thymeleaf.model.IComment;
import org.thymeleaf.model.IDocType;
import org.thymeleaf.model.IModel;
import org.thymeleaf.model.IModelVisitor;
import org.thymeleaf.model.IOpenElementTag;
import org.thymeleaf.model.IProcessingInstruction;
import org.thymeleaf.model.IStandaloneElementTag;
import org.thymeleaf.model.ITemplateEnd;
import org.thymeleaf.model.ITemplateEvent;
import org.thymeleaf.model.ITemplateStart;
import org.thymeleaf.model.IText;
import org.thymeleaf.model.IXMLDeclaration;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.util.Validate;


/**
 *
 * @author Daniel Fern&aacute;ndez
 * @since 3.0.0
 *
 */
final class Model implements IModel {

    private static final int INITIAL_EVENT_QUEUE_SIZE = 100; // 100 events by default, will auto-grow

    private final IEngineConfiguration configuration;
    private final TemplateMode templateMode;
    private final EngineEventQueue queue;




    // Only to be called from the IModelFactory
    Model(final IEngineConfiguration configuration, final TemplateMode templateMode) {
        super();
        Validate.notNull(configuration, "Engine Configuration cannot be null");
        Validate.notNull(templateMode, "Template Mode cannot be null");
        this.configuration = configuration;
        this.templateMode = templateMode;
        this.queue = new EngineEventQueue(this.configuration, this.templateMode, INITIAL_EVENT_QUEUE_SIZE);
    }


    Model(final IModel model) {

        super();

        Validate.notNull(model, "Model cannot be null");

        this.configuration = model.getConfiguration();
        this.templateMode = model.getTemplateMode();
        this.queue = new EngineEventQueue(this.configuration, this.templateMode, INITIAL_EVENT_QUEUE_SIZE);

        if (model instanceof Model) {

            this.queue.resetAsCloneOf(((Model)model).queue, true);

        } else if (model instanceof ImmutableModel) {

            this.queue.resetAsCloneOf(((ImmutableModel)model).getInternalModel().queue, true);

        } else {

            final int modelSize = model.size();
            for (int i = 0; i < modelSize; i++) {
                this.queue.add(asEngineEvent(this.configuration, this.templateMode, model.get(i), true), false);
            }

        }

    }




    public final IEngineConfiguration getConfiguration() {
        return this.configuration;
    }


    public final TemplateMode getTemplateMode() {
        return this.templateMode;
    }



    public int size() {
        return this.queue.size();
    }


    public ITemplateEvent get(final int pos) {
        return this.queue.get(pos);
    }


    public void add(final ITemplateEvent event) {
        this.queue.add(asEngineEvent(this.configuration, this.templateMode, event, true), false);
    }


    public void insert(final int pos, final ITemplateEvent event) {
        this.queue.insert(pos, asEngineEvent(this.configuration, this.templateMode, event, true), false);
    }


    public void addModel(final IModel model) {
        this.queue.addModel(model);
    }


    public void insertModel(final int pos, final IModel model) {
        this.queue.insertModel(pos, model);
    }


    public void remove(final int pos) {
        this.queue.remove(pos);
    }


    public void reset() {
        this.queue.reset();
    }




    // Note we don't want to put this visibility to public, because we want to access the internal queue only from
    // the engine...
    EngineEventQueue getEventQueue() {
        return this.queue;
    }



    void process(final ITemplateHandler templateHandler) {

        // Queued events themselves will not be cloned, our events will remain as the master ones,
        // and the new EngineEventQueue will use its own buffers.
        // The reason we are cloning the queue object itself before processing is precisely that the buffer objects
        // (which are part of the state of the EngineEventQueue object) will be used for firing the events, and those
        // can be modified during processing. So not queuing the queue and therefore not creating a new ser of buffers
        // could result in pretty bad interactions between template executions...
        final EngineEventQueue eventQueue = this.queue.cloneEventQueue(false, false);

        // Process the new, cloned queue
        // NOTE It is VERY important that 'reset' is here set to FALSE, because we will be sharing the actual
        // event array with the original (cached) event queue!
        eventQueue.process(templateHandler, false);

    }



    public IModel cloneModel() {
        return new Model(this);
    }




    public void write(final Writer writer) throws IOException {
        final OutputTemplateHandler outputTemplateHandler = new OutputTemplateHandler(writer);
        process(outputTemplateHandler);
    }




    public void accept(final IModelVisitor visitor) {

        final int queueSize = this.queue.size();
        for (int i = 0; i < queueSize; i++) {
            this.queue.get(i).accept(visitor);
        }

    }




    @Override
    public String toString() {
        try {
            final StringWriter writer = new StringWriter();
            write(writer);
            return writer.toString();
        } catch (final IOException e) {
            throw new TemplateProcessingException(
                    "Error while creating String representation of model");
        }
    }




    private static IEngineTemplateEvent asEngineEvent(
            final IEngineConfiguration configuration, final TemplateMode templateMode,
            final ITemplateEvent event, final boolean cloneAlways) {

        if (event instanceof IText) {
            return Text.asEngineText(configuration, (IText) event, cloneAlways);
        }
        if (event instanceof IOpenElementTag) {
            return OpenElementTag.asEngineOpenElementTag(templateMode, configuration, (IOpenElementTag) event, cloneAlways);
        }
        if (event instanceof ICloseElementTag) {
            return CloseElementTag.asEngineCloseElementTag(templateMode, configuration, (ICloseElementTag) event, cloneAlways);
        }
        if (event instanceof IStandaloneElementTag) {
            return StandaloneElementTag.asEngineStandaloneElementTag(templateMode, configuration, (IStandaloneElementTag) event, cloneAlways);
        }
        if (event instanceof IDocType) {
            return DocType.asEngineDocType(configuration, (IDocType) event, cloneAlways);
        }
        if (event instanceof IComment) {
            return Comment.asEngineComment(configuration, (IComment) event, cloneAlways);
        }
        if (event instanceof ICDATASection) {
            return CDATASection.asEngineCDATASection(configuration, (ICDATASection) event, cloneAlways);
        }
        if (event instanceof IXMLDeclaration) {
            return XMLDeclaration.asEngineXMLDeclaration(configuration, (IXMLDeclaration) event, cloneAlways);
        }
        if (event instanceof IProcessingInstruction) {
            return ProcessingInstruction.asEngineProcessingInstruction(configuration, (IProcessingInstruction) event, cloneAlways);
        }
        if (event instanceof ITemplateStart) {
            return TemplateStart.asEngineTemplateStart((ITemplateStart) event, cloneAlways);
        }
        if (event instanceof ITemplateEnd) {
            return TemplateEnd.asEngineTemplateEnd((ITemplateEnd) event, cloneAlways);
        }
        throw new TemplateProcessingException(
                "Cannot handle in queue event of type: " + event.getClass().getName());

    }



}