package com.stylefeng.guns;

import com.stylefeng.guns.rest.AlipayApplication;
import com.stylefeng.guns.rest.common.util.FTPUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AlipayApplication.class)
public class GunsRestApplicationTests {

	@Autowired
	private FTPUtil ftpUtil;


	@Test
	public void contextLoads() {
		//String fileByAddress = ftpUtil.getFileStrByAddress("seats/cgs.json");


		File file = new File("/Users/pigman2/Desktop/qr-a510b53f9ab745aaa9aec8a7a2d24c0c.png");
		ftpUtil.uploadFile("/qrcode.png",file);
	//	System.out.println(fileByAddress);
	}

}
