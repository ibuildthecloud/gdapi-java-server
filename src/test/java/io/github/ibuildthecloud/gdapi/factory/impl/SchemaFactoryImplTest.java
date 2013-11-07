package io.github.ibuildthecloud.gdapi.factory.impl;

import static org.junit.Assert.*;
import io.github.ibuildthecloud.gdapi.factory.impl.SchemaFactoryImpl;
import io.github.ibuildthecloud.gdapi.model.Field;
import io.github.ibuildthecloud.gdapi.model.Schema;
import io.github.ibuildthecloud.gdapi.model.Field.Type;
import io.github.ibuildthecloud.gdapi.testobject.TestType;
import io.github.ibuildthecloud.gdapi.testobject.TestTypeCRUD;
import io.github.ibuildthecloud.gdapi.testobject.TestTypeRename;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class SchemaFactoryImplTest {

    SchemaFactoryImpl factory;
    
    @Before
    public void setUp() {
        factory = new SchemaFactoryImpl();
    }
    
    @Test
    public void testOrdering() {
        Schema schema = factory.getSchema(TestType.class);

        Iterator<String> fields = schema.getResourceFields().keySet().iterator();
        
        assertEquals("first", fields.next());
        assertEquals("second", fields.next());
        assertEquals("a", fields.next());
    }

    @Test
    public void testOnlyWriteable() {
        Schema schema = factory.getSchema(TestType.class);

        Iterator<String> fields = schema.getResourceFields().keySet().iterator();
        
        assertEquals("first", fields.next());
        assertEquals("second", fields.next());
        assertEquals("a", fields.next());
    }

    @Test
    public void testName() {
        Schema schema = factory.getSchema(TestType.class);
        
        assertEquals("testType", schema.getId());
        assertEquals("schema", schema.getType());
    }
    
    @Test
    public void testRename() {
        Schema schema = factory.getSchema(TestTypeRename.class);
        
        assertEquals("Renamed", schema.getId());
        assertEquals("schema", schema.getType());
    }

    @Test
    public void testSimpleTypes() {
        Schema schema = factory.getSchema(TestType.class);

        Map<String,Field> fields = schema.getResourceFields();
        
        assertEquals("blob", fields.get("typeBlob").getType());
        assertEquals("date", fields.get("typeDate").getType());
        assertEquals("enum", fields.get("typeEnum").getType());
        
        assertEquals("boolean", fields.get("typeBool").getType());
        assertTrue(!fields.get("typeBool").isNullable());
        assertEquals("boolean", fields.get("typeBoolean").getType());
        assertTrue(fields.get("typeBoolean").isNullable());
        
        assertEquals("float", fields.get("typeFloat").getType());
        assertTrue(!fields.get("typeFloat").isNullable());
        assertEquals("float", fields.get("typeFloatObject").getType());
        assertTrue(fields.get("typeFloatObject").isNullable());
        
        assertEquals("float", fields.get("typeDouble").getType());
        assertTrue(!fields.get("typeDouble").isNullable());
        assertEquals("float", fields.get("typeDoubleObject").getType());
        assertTrue(fields.get("typeDoubleObject").isNullable());
        
        assertEquals("int", fields.get("typeInt").getType());
        assertTrue(!fields.get("typeInt").isNullable());
        assertEquals("int", fields.get("typeInteger").getType());
        assertTrue(fields.get("typeInteger").isNullable());
        
        assertEquals("int", fields.get("typeLong").getType());
        assertTrue(!fields.get("typeLong").isNullable());
        assertEquals("int", fields.get("typeLongObject").getType());
        assertTrue(fields.get("typeLongObject").isNullable());
        
        assertEquals(Field.Type.PASSWORD.getExternalType(), fields.get("typePassword").getType());
        assertEquals(Field.Type.STRING.getExternalType(), fields.get("typeString").getType());
        
        assertEquals(Type.MAP, fields.get("typeMap").getTypeEnum());
        assertEquals(Type.REFERENCE, fields.get("typeReference").getTypeEnum());
        assertEquals(Type.ARRAY, fields.get("typeArray").getTypeEnum());
        assertEquals(Type.ARRAY, fields.get("typeList").getTypeEnum());        
    }
    
    @Test
    public void testDefaultValue() {
        fail();
    }
    
    @Test
    public void testComplexType() {
        factory.getSchema(TestTypeCRUD.class);
        factory.getSchema(TestTypeRename.class);
        Schema schema = factory.getSchema(TestType.class);
        
        Map<String,Field> fields = schema.getResourceFields();
        
        assertEquals("array[map]", fields.get("typeList").getType());
        assertEquals("array[string]", fields.get("typeArray").getType());

        assertEquals("reference[testTypeCrud]", fields.get("testTypeCrudId").getType());
        assertEquals("type[testTypeCrud]", fields.get("testTypeCrud").getType());
    }
    
    @Test
    public void testDefaults() {
        Schema schema = factory.getSchema(TestType.class);

        Map<String,Field> fields = schema.getResourceFields();
        
        assertNull(fields.get("defaultSettings").getDefault());
        assertEquals("DEFAULT", fields.get("defaultValue").getDefault());
    }

    @Test
    public void testNullable() {
        Schema schema = factory.getSchema(TestType.class);

        Map<String,Field> fields = schema.getResourceFields();
        
        assertTrue(!fields.get("defaultSettings").isNullable());
        assertTrue(fields.get("nullable").isNullable());
    }
    
    @Test
    public void testUnique() {
        Schema schema = factory.getSchema(TestType.class);

        Map<String,Field> fields = schema.getResourceFields();
        
        assertTrue(!fields.get("defaultSettings").isUnique());
        assertTrue(fields.get("unique").isUnique());
    }

    @Test
    public void testValidChars() {
        Schema schema = factory.getSchema(TestType.class);

        Map<String,Field> fields = schema.getResourceFields();
        
        assertNull(fields.get("defaultSettings").getValidChars());
        assertEquals("valid", fields.get("validChars").getValidChars());
    }

    @Test
    public void testInvalidChars() {
        Schema schema = factory.getSchema(TestType.class);

        Map<String,Field> fields = schema.getResourceFields();
        
        assertNull(fields.get("defaultSettings").getInvalidChars());
        assertEquals("invalid", fields.get("invalidChars").getInvalidChars());
    }

    @Test
    public void testRequired() {
        Schema schema = factory.getSchema(TestType.class);

        Map<String,Field> fields = schema.getResourceFields();
        
        assertTrue(!fields.get("defaultSettings").isRequired());
        assertTrue(fields.get("required").isRequired());
    }

    @Test
    public void testCreateUpdate() {
        Schema schema = factory.getSchema(TestType.class);

        Map<String,Field> fields = schema.getResourceFields();
        
        assertTrue(!fields.get("defaultSettings").isCreate());
        assertTrue(!fields.get("defaultSettings").isUpdate());
        
        assertTrue(fields.get("createUpdate").isCreate());
        assertTrue(fields.get("createUpdate").isUpdate());

    }

    @Test
    public void testNameOverride() {
        Schema schema = factory.getSchema(TestType.class);

        Map<String,Field> fields = schema.getResourceFields();
        
        assertNull(fields.get("gonnaBeNameOverride"));
        assertNotNull(fields.get("nameOverride"));
    }
    
    @Test
    public void testLengths() {
        Schema schema = factory.getSchema(TestType.class);

        Map<String,Field> fields = schema.getResourceFields();

        assertNull(fields.get("defaultSettings").getMinLength());
        assertNull(fields.get("defaultSettings").getMaxLength());
        assertNull(fields.get("defaultSettings").getMin());
        assertNull(fields.get("defaultSettings").getMax());
        
        assertEquals(new Long(142), fields.get("lengths").getMinLength());
        assertEquals(new Long(242), fields.get("lengths").getMaxLength());
        assertEquals(new Long(342), fields.get("lengths").getMin());
        assertEquals(new Long(442), fields.get("lengths").getMax());
    }
    
    @Test
    public void testOptions() {
        Schema schema = factory.getSchema(TestType.class);
        Map<String,Field> fields = schema.getResourceFields();

        assertNull(fields.get("defaultSettings").getOptions());

        List<String> options = fields.get("typeEnum").getOptions();
        
        assertEquals(2, options.size());
        assertEquals("FIRST", options.get(0));
        assertEquals("SECOND", options.get(1));
    }
    
    @Test
    public void testTypeCRUD() {
        List<String> resourceMethods = factory.getSchema(TestType.class).getResourceMethods();
        List<String> collectionMethods = factory.getSchema(TestType.class).getCollectionMethods(); 

        assertEquals(1, resourceMethods.size());
        assertEquals(1, collectionMethods.size());
        
        assertEquals("GET", resourceMethods.get(0));
        assertEquals("GET", collectionMethods.get(0));

        resourceMethods = factory.getSchema(TestTypeCRUD.class).getResourceMethods();
        collectionMethods = factory.getSchema(TestTypeCRUD.class).getCollectionMethods();

        assertEquals(2, resourceMethods.size());
        assertTrue(resourceMethods.contains("DELETE"));
        assertTrue(resourceMethods.contains("PUT"));
        
        assertEquals(0, collectionMethods.size());
    }

}