package com.cheeza.Cheeza;

import com.cheeza.Cheeza.dto.PizzaResponse;
import com.cheeza.Cheeza.model.Pizza;
import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

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

}
