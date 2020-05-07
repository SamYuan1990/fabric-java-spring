package com.example.springboot;

import com.example.springboot.util.utils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

@RestController
public class PoolCacheController {

	@RequestMapping("/PoolCache")
	public String index() {

		String data = "";
		Random r = new Random(10);
		double d2 = r.nextDouble()* 5;
		if(d2>4) {
			data = utils.QueryWithPool();
		}
		else{
			data = "90";
		}

		return "Greetings from Spring Boot! "+ data;
	}

}