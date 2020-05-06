package com.example.springboot;

import com.example.springboot.util.utils;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
public class HelloController {

	@RequestMapping("/")
	public String index() {
		return "Greetings from Spring Boot! "+ utils.QueryWithOutPool();
	}

}