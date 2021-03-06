/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 ********************************************************************************/
package org.aoju.bus.extra.json.provider;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * FastJson 解析器
 *
 * @author Kimi Liu
 * @version 6.0.9
 * @since JDK 1.8+
 */
public class FastJsonProvider extends AbstractJsonProvider {

    private static final SerializerFeature[] FEATURES = {
            // 输出空置字段
            SerializerFeature.WriteMapNullValue,
            // list字段如果为null,输出为[],而不是null
            SerializerFeature.WriteNullListAsEmpty,
            // 数值字段如果为null,输出为0,而不是null
            SerializerFeature.WriteNullNumberAsZero,
            // Boolean字段如果为null,输出为false,而不是null
            SerializerFeature.WriteNullBooleanAsFalse,
            // 字符类型字段如果为null,输出为"",而不是null
            SerializerFeature.WriteNullStringAsEmpty
    };

    /**
     * 构造
     */
    public FastJsonProvider() {

    }

    @Override
    public String toJsonString(Object object) {
        return JSON.toJSONString(object, FEATURES);
    }

    @Override
    public String toJsonString(Object object, String format) {
        return JSON.toJSONStringWithDateFormat(object, format, SerializerFeature.WriteDateUseDateFormat);
    }

    @Override
    public <T> T toPojo(String json, Class<T> clazz) {
        return JSON.parseObject(json, clazz);
    }

    @Override
    public <T> T toPojo(Map map, Class<T> clazz) {
        return JSON.parseObject(JSON.toJSONString(map), clazz);
    }

    @Override
    public List toList(String json) {
        return JSON.parseObject(json, LinkedList.class);
    }

    @Override
    public <T> List<T> toList(String json, Type type) {
        TypeReference<T> typeReference = new TypeReference<T>() {
            @Override
            public Type getType() {
                return type;
            }
        };
        return JSON.parseObject(json, typeReference.getType());
    }

    @Override
    public Map toMap(String json) {
        return JSON.parseObject(json, LinkedHashMap.class);
    }

    @Override
    public Map toMap(Object object) {
        return JSON.parseObject(JSON.toJSONString(object), LinkedHashMap.class);
    }

    @Override
    public boolean isJson(String json) {
        try {
            JSON.parseObject(json);
        } catch (JSONException ex) {
            try {
                JSON.parseArray(json);
            } catch (JSONException ex1) {
                return false;
            }
        }
        return true;
    }

}
