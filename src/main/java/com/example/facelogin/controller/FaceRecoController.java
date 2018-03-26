/**
 * Copyright (C), 2008-2018, 杭州迪火科技有限公司
 * FileName: FaceReco
 * Author:   shugan
 * Date:     2018/3/13 21:14
 * Description:
 */
package com.example.facelogin.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import com.example.facelogin.face.tools.CommonOperate;
import com.example.facelogin.face.tools.FaceOperate;
import com.example.facelogin.face.tools.FaceSetOperate;
import com.example.facelogin.face.tools.Response;
import com.fasterxml.jackson.annotation.JsonAlias;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.Base64Utils;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Repeatable;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 〈〉
 *
 * @author shugan
 * @create 2018/3/13
 * @since 1.0.0
 */
@RestController
@RequestMapping("/")
public class FaceRecoController {
    @Value("${face.apiKey}")
    private String apiKey;
    @Value("${face.apiSecret}")
    private String apiSecret;
    private static final String RETURNFIELDS = "gender,age,smiling,headpose,facequality,blur,eyestatus,emotion,ethnicity,beauty,mouthstatus,eyegaze,skinstatus";

    /*public static void main(String[] args) {
        CommonOperate commonOperate = new CommonOperate(apiKey, apiSecret, false);
    }*/


    @PostMapping("/face_in")
    public Map faceIn(@RequestBody String face) throws Exception {
        CommonOperate commonOperate = new CommonOperate(apiKey, apiSecret, false);
        /*String returnAttrs = "gender,age,smiling,headpose,facequality,blur,eyestatus,emotion,ethnicity,beauty,mouthstatus,eyegaze,skinstatus";*/
        Response detectByte = commonOperate.detectByte(getImgByte(face), 1, RETURNFIELDS);
        if (detectByte.getStatus() != 200) {
            //face++人脸分析失败
            HashMap<Object, Object> res = new HashMap<>();
            res.put("status", 403);
            res.put("msg", "face++人脸分析失败");
            return res;
        }
        String response = new String(detectByte.getContent(),"UTF-8");
        //收集人脸token
        List<String> face_tokens = getFaceTokens(detectByte.getContent());
        String faceTokensString = face_tokens.stream().collect(Collectors.joining(","));

        //加入脸集
        FaceSetOperate faceSetOperate = new FaceSetOperate(apiKey, apiSecret, false);
        Response addFaceByOuterId = faceSetOperate.addFaceByOuterId(faceTokensString, "11111111");
        JSONObject faceJoin = JSON.parseObject(new String(addFaceByOuterId.getContent(), "UTF-8"));
        if (detectByte.getStatus() != 200) {
            //face++人脸分析失败
            HashMap<Object, Object> res = new HashMap<>();
            res.put("status", 403);
            res.put("msg", "face++抽风,face++加入脸集失败");
            return res;
        }
        //人脸与用户唯一识别码绑定

        System.out.println(faceJoin);
            /*FileOutputStream out = new FileOutputStream("D://aaa.jpg");
            ByteArrayInputStream is = new ByteArrayInputStream(bs);
            byte[] buff = new byte[1024];
            int len = 0;
            while((len=is.read(buff))!=-1){
                out.write(buff, 0, len);
            }
            is.close();
            out.close();*/
        HashMap<Object, Object> res = new HashMap<>();
        if (face_tokens.size() == 0) {
            res.put("status", 403);
            res.put("msg", "未检测到人脸，请重试");
            return res;
        }
        if (face_tokens.size() > 1) {
            res.put("status", 403);
            res.put("msg", "检测到多张人脸，请重试");
            return res;
        }
        res.put("status", 200);
        res.put("msg", "人脸录入成功");
        res.put("face_token", face_tokens.get(0));
        return res;
    }

    @PostMapping("/search_faces")
    public Map searchFaces(@RequestBody String face) throws Exception {
        byte[] imgByte = getImgByte(face);
        CommonOperate commonOperate = new CommonOperate(apiKey, apiSecret, false);
        Response search = commonOperate.searchByOuterId(null, null, imgByte, "11111111", 2);

        JSONObject searchObject = JSON.parseObject(new String(search.getContent(), "UTF-8"));
        if (search.getStatus() != 200) {
            //face++人脸分析失败
            HashMap<Object, Object> res = new HashMap<>();
            res.put("status", 403);
            res.put("msg", "face++抽风,人脸检索失败");
            return res;
        }
        if (searchObject.getJSONArray("faces").size() == 0) {
            HashMap<Object, Object> res = new HashMap<>();
            res.put("status", 403);
            res.put("msg", "未检测到人脸，请重试！");
            return res;
        }
        //误识率
        JSONObject thresholds = searchObject.getJSONObject("thresholds");
        //识别结果
        JSONArray results = searchObject.getJSONArray("results");

        HashMap<Object, Object> res = new HashMap<>();
        res.put("status", 200);
        res.put("msg", "ok");
        res.put("thresholds", thresholds);
        res.put("results", results);
        return res;
    }

    //创建一个人脸集合
    @GetMapping("/create_faceset")
    public Map createFaceSet() throws Exception {
        FaceSetOperate faceSetOperate = new FaceSetOperate(apiKey, apiSecret, false);

        Response faceSet = faceSetOperate.createFaceSet("test_face_set", "11111111", "test", "", "jack_s test faceSet", 1);
        if (faceSet.getStatus() != 200) {
            //face++创建脸集失败
            HashMap<Object, Object> res = new HashMap<>();
            res.put("status", 403);
            res.put("msg", "face++抽风,face++创建脸集失败");
            return res;
        }
        JSONObject faceSetJson = JSON.parseObject(new String(faceSet.getContent(), "UTF-8"));
        HashMap<Object, Object> res = new HashMap<>();
        res.put("status", 200);
        res.put("msg", "ok");
        res.put("data", faceSetJson);
        return res;
    }

    //查询脸集根据outId
    @GetMapping("/get_face_set")
    public Map getFaceSet() throws Exception {
        FaceSetOperate faceSetOperate = new FaceSetOperate(apiKey, apiSecret, false);
        Response detailByOuterId = faceSetOperate.getDetailByOuterId("11111111");
        if (detailByOuterId.getStatus() != 200) {
            HashMap<Object, Object> res = new HashMap<>();
            res.put("status", 403);
            res.put("msg", "face++抽风,查询指定脸集失败");
            return res;
        }
        JSONObject Json = JSON.parseObject(new String(detailByOuterId.getContent(), "UTF-8"));
        HashMap<Object, Object> res = new HashMap<>();
        res.put("status", 200);
        res.put("msg", "ok");
        res.put("data", Json);
        return res;
    }

    //人脸绑定用户id
    @GetMapping("/bind_user")
    public Map bindUser(@RequestParam("face_token") String faceToken, @RequestParam("user_id") String userId) throws Exception {
        FaceOperate faceOperate = new FaceOperate(apiKey, apiSecret, false);
        Response faceSetUserId = faceOperate.faceSetUserId(faceToken, userId);
        if (faceSetUserId.getStatus() != 200) {
            HashMap<Object, Object> res = new HashMap<>();
            res.put("status", 403);
            res.put("msg", "绑定用户关系失败，face++抽风,again！");
            return res;
        }
        JSONObject jsonObject = JSON.parseObject(new String(faceSetUserId.getContent(), "UTF-8"));
        HashMap<Object, Object> res = new HashMap<>();
        res.put("status", 200);
        res.put("msg", "ok");
        return res;
    }

    private List<String> getFaceTokens(byte[] response) throws UnsupportedEncodingException {
        List<String> face_tokens = new ArrayList<>();
        String content = new String(response, "UTF-8");
        JSONObject jsonObject = JSON.parseObject(content);
        JSONArray faces = jsonObject.getJSONArray("faces");
        Iterator iterator = faces.iterator();
        while (iterator.hasNext()) {
            JSONObject faceObject = (JSONObject) iterator.next();
            face_tokens.add(faceObject.getString("face_token"));
        }
        return face_tokens;
    }

    private byte[] getImgByte(String face) throws Exception {

        JSONObject img = JSON.parseObject(face);
        String base64Data = img.get("face").toString();
        String dataPrix = "";
        String data = "";
        if (base64Data == null || "".equals(base64Data)) {
            throw new Exception("上传失败，上传图片数据为空");
        } else {
            String[] d = base64Data.split("base64,");
            if (d != null && d.length == 2) {
                dataPrix = d[0];
                data = d[1];
            } else {
                throw new Exception("上传失败，数据不合法");
            }
        }
        String suffix = "";
        if ("data:image/jpeg;".equalsIgnoreCase(dataPrix)) {//data:image/jpeg;base64,base64编码的jpeg图片数据
            suffix = ".jpg";
        } else if ("data:image/x-icon;".equalsIgnoreCase(dataPrix)) {//data:image/x-icon;base64,base64编码的icon图片数据
            suffix = ".ico";
        } else if ("data:image/gif;".equalsIgnoreCase(dataPrix)) {//data:image/gif;base64,base64编码的gif图片数据
            suffix = ".gif";
        } else if ("data:image/png;".equalsIgnoreCase(dataPrix)) {//data:image/png;base64,base64编码的png图片数据
            suffix = ".png";
        } else {
            throw new Exception("上传图片格式不合法");
        }
        String tempFileName = "aaaa" + suffix;

        //因为BASE64Decoder的jar问题，此处使用spring框架提供的工具包
        byte[] bs = Base64Utils.decodeFromString(data);
        return bs;
    }
}