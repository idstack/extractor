package org.idstack.extractor;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.idstack.feature.Constant;
import org.idstack.feature.document.*;

import java.util.*;

/**
 * @author Chandu Herath
 * @date 30/08/2017
 * @since 1.0
 */

public class JsonCreator {

    public String constructAsNestedJson(String receivedJson) {
        JsonObject obj = new JsonParser().parse(receivedJson).getAsJsonObject();
        JsonObject metadataObject = obj.getAsJsonObject(Constant.JsonAttribute.META_DATA);
        JsonObject contentObject = obj.getAsJsonObject(Constant.JsonAttribute.CONTENT);

        //create metadata object
        MetaData metaData = new MetaData(metadataObject.get(Constant.JsonAttribute.MetaData.NAME).getAsString(), metadataObject.get(Constant.JsonAttribute.MetaData.VERSION).getAsString(), metadataObject.get(Constant.JsonAttribute.MetaData.DOCUMENT_ID).getAsString(), metadataObject.get(Constant.JsonAttribute.MetaData.DOCUMENT_TYPE).getAsString(), new Gson().fromJson(metadataObject.get(Constant.JsonAttribute.MetaData.ISSUER).toString(), Issuer.class));

        //create linked hash map
        LinkedHashMap<String, Object> contentMap = constructJsonContent(contentObject);

        //create empty extractor
        Extractor extractor = new Extractor("", new Signature("", ""));

        //create empty validator array
        ArrayList<Validator> validators = new ArrayList<Validator>();

        Document doc = new Document(metaData, contentMap, extractor, validators);

        String jsonStringToSign = new Gson().toJson(doc);

        return jsonStringToSign;
    }

    private LinkedHashMap<String, Object> constructJsonContent(JsonObject obj) {
        Set<Map.Entry<String, JsonElement>> set = obj.entrySet();
        Iterator<Map.Entry<String, JsonElement>> iterator = set.iterator();
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        while (iterator.hasNext()) {
            Map.Entry<String, JsonElement> entry = iterator.next();
            String key = entry.getKey();
            if (key.contains(":")) {
                String[] keyArray = key.split(":");
                getNestedHashMap(keyArray, map, entry.getValue());
            } else {
                map.put(key, entry.getValue());
            }

        }
        return map;
    }

    private LinkedHashMap<String, Object> getNestedHashMap(String[] keyArray, LinkedHashMap<String, Object> originalMap, JsonElement value) {
        if (originalMap.containsKey(keyArray[0])) {
            LinkedHashMap<String, Object> map1 = (LinkedHashMap<String, Object>) originalMap.get(keyArray[0]);
            String[] childArray = Arrays.copyOfRange(keyArray, 1, keyArray.length);
            getNestedHashMap(childArray, map1, value);
        } else {
            if (keyArray.length != 1) {
                LinkedHashMap<String, Object> map = new LinkedHashMap<>();
                String keyValue = keyArray[0];
                String[] childArray = Arrays.copyOfRange(keyArray, 1, keyArray.length);
                originalMap.put(keyValue, getNestedHashMap(childArray, map, value));
            } else {
                originalMap.put(keyArray[0], value);
            }
        }
        return originalMap;
    }
}