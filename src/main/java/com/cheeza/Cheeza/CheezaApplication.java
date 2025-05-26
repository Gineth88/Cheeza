package com.cheeza.Cheeza;

import com.cheeza.Cheeza.config.PizzaUploadProperties;
import com.cheeza.Cheeza.dto.PizzaResponse;
import com.cheeza.Cheeza.model.Pizza;
import org.modelmapper.ModelMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootApplication
@EnableConfigurationProperties(PizzaUploadProperties.class)
@EnableWebSocketMessageBroker
public class CheezaApplication {

	public static void main(String[] args) {
		SpringApplication.run(CheezaApplication.class, args);
	}

//	@Bean
//	public ModelMapper modelMapper() {
//		ModelMapper mapper = new ModelMapper();

//		mapper.createTypeMap(Pizza.class, PizzaResponse.class)
//				.addMappings(m -> {
//					m.map(src -> src.getAvailableToppings(), PizzaResponse::setAvailableToppings);
//				});
//
//		return mapper;
//	}



}
