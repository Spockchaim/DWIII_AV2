package com.autobots.automanager;

import java.util.Calendar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

import com.autobots.automanager.entidades.Cliente;
import com.autobots.automanager.entidades.Documento;
import com.autobots.automanager.entidades.Endereco;
import com.autobots.automanager.entidades.Telefone;
import com.autobots.automanager.repositorios.ClienteRepositorio;

@SpringBootApplication
public class AutomanagerApplication {

	public static void main(String[] args) {
		SpringApplication.run(AutomanagerApplication.class, args);
	}

	@Component
	public static class Runner implements ApplicationRunner {
		@Autowired
		public ClienteRepositorio repositorio;

		@Override
		public void run(ApplicationArguments args) throws Exception {
			if (repositorio.count() > 0) {
				return;
			}

			// Cliente 1: Dom Pedro
			Calendar cal1 = Calendar.getInstance();
			cal1.set(1798, Calendar.OCTOBER, 12);
			Cliente c1 = new Cliente();
			c1.setNome("Pedro de Alcântara Francisco Antônio João Carlos Xavier de Paula Miguel Rafael Joaquim José Gonzaga Pascoal Cipriano Serafim de Bragança e Bourbon");
			c1.setNomeSocial("Dom Pedro I");
			c1.setDataCadastro(Calendar.getInstance().getTime());
			c1.setDataNascimento(cal1.getTime());

			Telefone t1 = new Telefone();
			t1.setDdd("21");
			t1.setNumero("981234576");
			c1.getTelefones().add(t1);

			Endereco e1 = new Endereco();
			e1.setEstado("Rio de Janeiro");
			e1.setCidade("Rio de Janeiro");
			e1.setBairro("São Cristóvão");
			e1.setRua("Quinta da Boa Vista");
			e1.setNumero("S/N");
			e1.setCodigoPostal("20940040");
			e1.setInformacoesAdicionais("Palácio Imperial");
			c1.setEndereco(e1);

			Documento rg1 = new Documento();
			rg1.setTipo("RG");
			rg1.setNumero("1500");
			Documento cpf1 = new Documento();
			cpf1.setTipo("CPF");
			cpf1.setNumero("00000000001");
			c1.getDocumentos().add(rg1);
			c1.getDocumentos().add(cpf1);

			repositorio.save(c1);

			// Cliente 2: Princesa Isabel
			Calendar cal2 = Calendar.getInstance();
			cal2.set(1846, Calendar.JULY, 29);
			Cliente c2 = new Cliente();
			c2.setNome("Isabel Cristina Leopoldina Augusta Micaela Gabriela Rafaela Gonzaga de Bourbon e Bragança");
			c2.setNomeSocial("Princesa Isabel");
			c2.setDataCadastro(Calendar.getInstance().getTime());
			c2.setDataNascimento(cal2.getTime());

			Telefone t2 = new Telefone();
			t2.setDdd("24");
			t2.setNumero("999991888");
			c2.getTelefones().add(t2);

			Endereco e2 = new Endereco();
			e2.setEstado("Rio de Janeiro");
			e2.setCidade("Petrópolis");
			e2.setBairro("Centro");
			e2.setRua("Rua da Imperatriz");
			e2.setNumero("220");
			e2.setCodigoPostal("25610320");
			e2.setInformacoesAdicionais("Museu Imperial");
			c2.setEndereco(e2);

			Documento rg2 = new Documento();
			rg2.setTipo("RG");
			rg2.setNumero("1888");
			Documento cpf2 = new Documento();
			cpf2.setTipo("CPF");
			cpf2.setNumero("00000000002");
			c2.getDocumentos().add(rg2);
			c2.getDocumentos().add(cpf2);

			repositorio.save(c2);

			// Cliente 3: Machado de Assis
			Calendar cal3 = Calendar.getInstance();
			cal3.set(1839, Calendar.JUNE, 21);
			Cliente c3 = new Cliente();
			c3.setNome("Joaquim Maria Machado de Assis");
			c3.setNomeSocial("Machado de Assis");
			c3.setDataCadastro(Calendar.getInstance().getTime());
			c3.setDataNascimento(cal3.getTime());

			Telefone t3 = new Telefone();
			t3.setDdd("21");
			t3.setNumero("988887777");
			c3.getTelefones().add(t3);

			Endereco e3 = new Endereco();
			e3.setEstado("Rio de Janeiro");
			e3.setCidade("Rio de Janeiro");
			e3.setBairro("Cosme Velho");
			e3.setRua("Rua Cosme Velho");
			e3.setNumero("18");
			e3.setCodigoPostal("22241090");
			e3.setInformacoesAdicionais("Bruxo do Cosme Velho");
			c3.setEndereco(e3);

			Documento rg3 = new Documento();
			rg3.setTipo("RG");
			rg3.setNumero("1897");
			Documento cpf3 = new Documento();
			cpf3.setTipo("CPF");
			cpf3.setNumero("00000000003");
			c3.getDocumentos().add(rg3);
			c3.getDocumentos().add(cpf3);

			repositorio.save(c3);
		}
	}
}
