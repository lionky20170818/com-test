package com.ligl.common.redisTemplate;

import net.sf.ezmorph.object.DateMorpher;
import net.sf.json.JSONObject;
import net.sf.json.util.JSONUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

/**
 * 修改与2012-4-27
 */
public class JsonUtils {
    final static Logger LOG = LoggerFactory.getLogger(JsonUtils.class);

    public static String objectToJson(Object bean) {
        try {
            ObjectMapper om = new ObjectMapper();
            String json = om.writeValueAsString(bean);
            return json;
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            throw new RuntimeException("json convert error", e);
        }
    }

    public static String object2Json(Object obj) {
        return objectToJson(obj);
    }

    public static Map<?, ?> jsonToObject(String json) {
        try {
            ObjectMapper om = new ObjectMapper();
            Map<?, ?> map = om.readValue(json, Map.class);
            return map;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw new RuntimeException("json convert error", e);
        }
    }

    public static <T> T json2Object(String json, Class<T> clz) {
        try {
            ObjectMapper om = new ObjectMapper();
            T obj = om.readValue(json, clz);
            return obj;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw new RuntimeException("json convert error", e);
        }
    }

    public static <T> T jsonToBeanByNet(String json, Class<T> clz) {
        try {
            JSONObject result = JSONObject.fromObject(json);
            JSONUtils.getMorpherRegistry().registerMorpher(new DateMorpher(new String[]{"yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss"}));
            T obj = (T) JSONObject.toBean(result, clz);
            return obj;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw new RuntimeException("json convert error", e);
        }
    }


}
