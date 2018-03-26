/**
 * Copyright (C), 2008-2018, 杭州迪火科技有限公司
 * FileName: AliyunFaceController
 * Author:   shugan
 * Date:     2018/3/22 10:11
 * Description:
 */
package com.example.facelogin.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.facelogin.face.tools.CommonOperate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 〈〉
 *
 * @author shugan
 * @create 2018/3/22
 * @since 1.0.0
 */
@RestController
@RequestMapping("/ali")
public class AliyunFaceController {

    @PostMapping("/face_in")
    public Map faceIn(@RequestBody String face) throws Exception {
        AliYunFace aliYunFace = new AliYunFace();
        String ak_id = "LTAIQM2SU0COzaWp"; //用户ak
        String ak_secret1 = "EkxmpotcN3v7CQooJChIS2hCSVEaPN"; // 用户ak_secret
        String url = "https://dtplus-cn-shanghai.data.aliyuncs.com/face/attribute";
        Map body = new HashMap();
        body.put("type", 1);
        body.put("content", JSON.parseObject(face).getString("face").split(",")[1]);
        //保存base64图片
        File file = new File("D");
        String s = aliYunFace.sendPost(url, JSON.toJSONString(body), ak_id, ak_secret1);
        JSONObject jsonObject = JSON.parseObject(s);
        Map map = new HashMap();
        map.put("特征点定位结果", jsonObject.get("landmark"));
        map.put("人脸识别返回特征；1024个特征", jsonObject.get("dense_fea"));
        System.out.println("map======"+map.toString());
        return map;
    }
}