package io.github.ibuildthecloud.gdapi.factory.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import io.github.ibuildthecloud.gdapi.factory.SchemaFactory;
import io.github.ibuildthecloud.gdapi.model.Schema;
import io.github.ibuildthecloud.model.impl.SchemaImpl;

public class SubSchemaFactory extends AbstractSchemaFactory implements SchemaFactory, SchemaPostProcessor {
    SchemaFactory schemaFactory;
    String id;
    Map<String,Schema> schemaMap;
    List<SchemaImpl> schemaList = new ArrayList<SchemaImpl>();
    List<SchemaPostProcessor> postProcessors = new ArrayList<SchemaPostProcessor>();
    List<SchemaPostProcessor> additionalPostProcessors = new ArrayList<SchemaPostProcessor>();
    boolean init = false;

    public synchronized void init() {
        if ( init ) {
            return;
        }

        if ( schemaFactory instanceof SubSchemaFactory ) {
            ((SubSchemaFactory)schemaFactory).init();
        }

        List<SchemaPostProcessor> postProcessors = getAllPostProcessors();
        List<SchemaImpl> result = new ArrayList<SchemaImpl>();

        Iterator<SchemaImpl> iter = schemaList.iterator();
        outer:
        while ( iter.hasNext() ) {
            SchemaImpl schema = new SchemaImpl(iter.next());
            for ( SchemaPostProcessor post : postProcessors ) {
                if ( post.postProcessRegister(schema, this) == null) {
                    iter.remove();
                    break outer;
                }
            }

            result.add(schema);
        }

        schemaList = result;

        for ( SchemaImpl schema : schemaList ) {
            for ( SchemaPostProcessor post : postProcessors ) {
                schema = post.postProcess(schema, this);
            }
        }

        init = true;
    }

    public List<SchemaPostProcessor> getAllPostProcessors() {
        List<SchemaPostProcessor> result = new ArrayList<SchemaPostProcessor>(additionalPostProcessors);
        if ( postProcessors != null ) {
            result.addAll(postProcessors);
        }

        return result;
    }

    @Override
    public String getId() {
        return id;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public List<Schema> listSchemas() {
        return (List)schemaList;
    }

    @Override
    public Schema getSchema(Class<?> clz) {
        return getSchema(schemaFactory.getSchemaName(clz));
    }

    @Override
    public Schema getSchema(String type) {
        Schema parentSchema = schemaFactory.getSchema(type);
        return parentSchema == null ? null : schemaMap.get(parentSchema.getId());
    }

    @Override
    public Class<?> getSchemaClass(String type) {
        Schema schema = getSchema(type);
        return schema == null ? null : schemaFactory.getSchemaClass(type);
    }

    @Override
    public Schema registerSchema(Object obj) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Schema parseSchema(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addPostProcessor(SchemaPostProcessor postProcessor) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SchemaImpl postProcessRegister(SchemaImpl schema, SchemaFactory factory) {
        schemaList.add(schema);
        return schema;
    }

    @Override
    public SchemaImpl postProcess(SchemaImpl schema, SchemaFactory factory) {
        return schema;
    }

    public SchemaFactory getSchemaFactory() {
        return schemaFactory;
    }

    @Inject
    public void setSchemaFactory(SchemaFactory schemaFactory) {
        this.schemaFactory = schemaFactory;
        if ( schemaFactory != null ) {
            schemaFactory.addPostProcessor(this);
        }
    }

    public List<SchemaPostProcessor> getPostProcessors() {
        return postProcessors;
    }

    public void setPostProcessors(List<SchemaPostProcessor> postProcessors) {
        this.postProcessors = postProcessors;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<SchemaPostProcessor> getPostProcessor() {
        return additionalPostProcessors;
    }

    public void setPostProcessor(List<SchemaPostProcessor> postProcessor) {
        this.additionalPostProcessors = postProcessor;
    }

}
