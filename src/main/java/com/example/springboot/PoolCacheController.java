package com.example.springboot;

import com.example.springboot.util.utils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PoolController {

	@RequestMapping("/Pool")
	public String index() {
		return "Greetings from Spring Boot! "+ utils.QueryWithPool();
	}

}