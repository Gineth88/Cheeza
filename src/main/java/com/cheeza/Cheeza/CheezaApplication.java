package com.cheeza.Cheeza;

import com.cheeza.Cheeza.dto.PizzaResponse;
import com.cheeza.Cheeza.model.Pizza;
import org.modelmapper.ModelMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootApplication
public class CheezaApplication {

	public static void main(String[] args) {
		SpringApplication.run(CheezaApplication.class, args);
	}

	@Bean
	public ModelMapper modelMapper() {
		ModelMapper mapper = new ModelMapper();

		mapper.createTypeMap(Pizza.class, PizzaResponse.class)
				.addMappings(m -> {
					m.map(src -> src.getAvailableToppings(), PizzaResponse::setAvailableToppings);
				});

		return mapper;
	}
	@Bean
	CommandLineRunner init() {
		return args -> {
			URI uploadDir = null;
			Path uploadPath = Paths.get(uploadDir);
			if (!Files.exists(uploadPath)) {
				Files.createDirectories(uploadPath);
			}
		};
	}

}
