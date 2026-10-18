package com.autobots.automanager.configuracao;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI customOpenAPI() {
		return new OpenAPI()
				.info(new Info()
						.title("AutoBots — API AutoManager (ATVII)")
						.version("1.0.0")
						.description("Microsserviço de gestão de clientes, documentos, endereços e telefones implementando os 4 níveis do Richardson Maturity Model (RMM) e HATEOAS.")
						.contact(new Contact()
								.name("FATEC São José dos Campos — Desenvolvimento Web III")));
	}
}

