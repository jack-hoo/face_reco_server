package com.example.facelogin;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.facelogin.face.tools.CommonOperate;
import com.example.facelogin.face.tools.Response;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class FaceLoginApplicationTests {

	@Value("${face.apiKey}")
	private String apiKey;
	@Value("${face.apiSecret}")
	private String apiSecret;
	private static final String RETURNFIELDS = "gender,age,smiling,headpose,facequality,blur,eyestatus,emotion,ethnicity,beauty,mouthstatus,eyegaze,skinstatus";
	@Test
	public void contextLoads() {
		CommonOperate commonOperate = new CommonOperate(apiKey,apiSecret,false);
		try {
			Response response = commonOperate.detectUrl("http://imgwx4.2345.com/dypcimg/star/img/b/0/222/photo_192x262.jpg?1509946079", 1, RETURNFIELDS);

			String content = new String(response.getContent(), "UTF-8");
			JSONObject jsonObject = JSON.parseObject(content);
			System.out.println(response);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
