/*
 * The MIT License
 *
 * Copyright (c) 2017, aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.sensitive;

import lombok.Data;
import org.aoju.bus.core.lang.exception.InstrumentException;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * 脱敏的执行上下文
 *
 * @author Kimi Liu
 * @version 3.0.5
 * @since JDK 1.8
 */
@Data
public class Context {

    /**
     * 当前对象
     */
    private Object currentObject;

    /**
     * 当前字段
     */
    private Field currentField;

    /**
     * 所有字段
     */
    private List<Field> allFieldList = new ArrayList<>();

    /**
     * 类信息
     *
     * @since 0.0.6
     */
    private Class beanClass;

    /**
     * 明细信息
     *
     * @since 0.0.6
     */
    private Object entry;

    /**
     * 新建一个对象实例
     *
     * @return this
     * @since 0.0.6
     */
    public static Context newInstance() {
        return new Context();
    }

    /**
     * 获取当前字段名称
     *
     * @return 字段名称
     * @since 0.0.4
     */
    public String getCurrentFieldName() {
        return this.currentField.getName();
    }


    /**
     * 获取当前字段值
     *
     * @return 字段值
     * @since 0.0.4
     */
    public Object getCurrentFieldValue() {
        try {
            return this.currentField.get(this.currentObject);
        } catch (IllegalAccessException e) {
            throw new InstrumentException(e);
        }
    }

}