package com.braisefish.jrst.i.entity;

import org.junit.Test;
import org.junit.Assert;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 测试 DoggyBox 类
 * @author 32365
 */
public class DoggyBoxTest {

    @Test
    public void testNoArgsConstructor() {
        DoggyBox<String> box = new DoggyBox<>();
        Assert.assertNull(box.getData());
        Assert.assertNull(box.getProperty());
    }

    @Test
    public void testConstructorWithData() {
        String testData = "test data";
        DoggyBox<String> box = new DoggyBox<>(testData);
        Assert.assertEquals(testData, box.getData());
        Assert.assertNotNull(box.getProperty());
        Assert.assertTrue(box.getProperty() instanceof LinkedHashMap);
        Assert.assertTrue(box.getProperty().isEmpty());
    }

    @Test
    public void testConstructorWithDataAndProperty() {
        String testData = "test data";
        Map<String, Object> property = new HashMap<>();
        property.put("key1", "value1");
        property.put("key2", 123);

        DoggyBox<String> box = new DoggyBox<>(testData, property);
        Assert.assertEquals(testData, box.getData());
        Assert.assertEquals(property, box.getProperty());
        Assert.assertEquals("value1", box.getProperty().get("key1"));
        Assert.assertEquals(123, box.getProperty().get("key2"));
    }

    @Test
    public void testStaticOfWithData() {
        Integer testData = 42;
        DoggyBox<Integer> box = DoggyBox.of(testData);
        Assert.assertEquals(testData, box.getData());
        Assert.assertNotNull(box.getProperty());
        Assert.assertTrue(box.getProperty() instanceof LinkedHashMap);
        Assert.assertTrue(box.getProperty().isEmpty());
    }

    @Test
    public void testStaticOfWithDataAndProperty() {
        String testData = "test";
        Map<String, Object> property = new LinkedHashMap<>();
        property.put("name", "DoggyBox");
        property.put("version", 1.0);

        DoggyBox<String> box = DoggyBox.of(testData, property);
        Assert.assertEquals(testData, box.getData());
        Assert.assertEquals(property, box.getProperty());
        Assert.assertEquals("DoggyBox", box.getProperty().get("name"));
        Assert.assertEquals(1.0, box.getProperty().get("version"));
    }

    @Test
    public void testStaticOfWithNullProperty() {
        String testData = "test";
        DoggyBox<String> box = DoggyBox.of(testData, null);
        Assert.assertEquals(testData, box.getData());
        Assert.assertNotNull(box.getProperty());
        Assert.assertTrue(box.getProperty() instanceof LinkedHashMap);
        Assert.assertTrue(box.getProperty().isEmpty());
    }

    @Test
    public void testGetData() {
        Double testData = 3.14;
        DoggyBox<Double> box = new DoggyBox<>(testData);
        Assert.assertEquals(testData, box.getData());
    }

    @Test
    public void testSetData() {
        DoggyBox<String> box = new DoggyBox<>();
        String newData = "new data";
        DoggyBox<String> result = box.setData(newData);
        Assert.assertEquals(newData, box.getData());
        Assert.assertSame(box, result); // 验证返回自身，支持链式调用
    }

    @Test
    public void testGetProperty() {
        Map<String, Object> property = new HashMap<>();
        property.put("test", "value");
        DoggyBox<String> box = new DoggyBox<>("data", property);
        Assert.assertEquals(property, box.getProperty());
    }

    @Test
    public void testSetProperty() {
        DoggyBox<String> box = new DoggyBox<>("data");
        Map<String, Object> newProperty = new HashMap<>();
        newProperty.put("key", "value");
        DoggyBox<String> result = box.setProperty(newProperty);
        Assert.assertEquals(newProperty, box.getProperty());
        Assert.assertSame(box, result); // 验证返回自身，支持链式调用
    }

    @Test
    public void testFluentSetters() {
        DoggyBox<String> box = new DoggyBox<>();
        Map<String, Object> property = new HashMap<>();
        property.put("key", "value");

        DoggyBox<String> result = box.setData("test")
                .setProperty(property);

        Assert.assertEquals("test", result.getData());
        Assert.assertEquals(property, result.getProperty());
        Assert.assertSame(box, result);
    }

    @Test
    public void testWithDifferentGenericTypes() {
        // 测试 String 类型
        DoggyBox<String> stringBox = DoggyBox.of("string");
        Assert.assertEquals("string", stringBox.getData());

        // 测试 Integer 类型
        DoggyBox<Integer> intBox = DoggyBox.of(100);
        Assert.assertEquals(Integer.valueOf(100), intBox.getData());

        // 测试自定义对象类型
        DoggyBox<Map<String, String>> mapBox = DoggyBox.of(new HashMap<>());
        Assert.assertNotNull(mapBox.getData());
    }

    @Test
    public void testPropertyModification() {
        Map<String, Object> property = new LinkedHashMap<>();
        property.put("initial", "value");
        DoggyBox<String> box = new DoggyBox<>("data", property);

        // 修改属性
        box.getProperty().put("newKey", "newValue");
        Assert.assertEquals(2, box.getProperty().size());
        Assert.assertEquals("newValue", box.getProperty().get("newKey"));
    }

    @Test
    public void testNullData() {
        DoggyBox<String> box = new DoggyBox<>(null);
        Assert.assertNull(box.getData());
        Assert.assertNotNull(box.getProperty());
    }

    @Test
    public void testEmptyProperty() {
        Map<String, Object> emptyProperty = new LinkedHashMap<>();
        DoggyBox<String> box = new DoggyBox<>("data", emptyProperty);
        Assert.assertTrue(box.getProperty().isEmpty());
    }
}

