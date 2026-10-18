package com.autobots.automanager;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.autobots.automanager.entidades.Cliente;
import com.autobots.automanager.entidades.Documento;
import com.autobots.automanager.entidades.Endereco;
import com.autobots.automanager.entidades.Telefone;
import com.autobots.automanager.repositorios.ClienteRepositorio;
import com.autobots.automanager.repositorios.DocumentoRepositorio;
import com.autobots.automanager.repositorios.EnderecoRepositorio;
import com.autobots.automanager.repositorios.TelefoneRepositorio;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
public class Fase6EndpointsTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private ClienteRepositorio clienteRepositorio;

	@Autowired
	private DocumentoRepositorio documentoRepositorio;

	@Autowired
	private EnderecoRepositorio enderecoRepositorio;

	@Autowired
	private TelefoneRepositorio telefoneRepositorio;

	@Nested
	@DisplayName("6.1. Testes de Cliente (CRUD + RMM + HATEOAS)")
	class ClienteTests {

		@Test
		@DisplayName("6.1.1 - Inserção de cliente com endereço, documentos e telefones")
		void testarInsercaoClienteCompleto() throws Exception {
			Cliente novoCliente = new Cliente();
			novoCliente.setNome("Maria Leopoldina");
			novoCliente.setNomeSocial("Imperatriz Leopoldina");
			novoCliente.setDataNascimento(new Date());
			novoCliente.setDataCadastro(new Date());

			Endereco endereco = new Endereco();
			endereco.setEstado("Rio de Janeiro");
			endereco.setCidade("Rio de Janeiro");
			endereco.setBairro("São Cristóvão");
			endereco.setRua("Quinta da Boa Vista");
			endereco.setNumero("S/N");
			endereco.setCodigoPostal("20940040");
			endereco.setInformacoesAdicionais("Palácio Real");
			novoCliente.setEndereco(endereco);

			Documento passaporte = new Documento();
			passaporte.setTipo("PASSAPORTE");
			passaporte.setNumero("AT123456");
			novoCliente.getDocumentos().add(passaporte);

			Telefone telefone = new Telefone();
			telefone.setDdd("21");
			telefone.setNumero("998877665");
			novoCliente.getTelefones().add(telefone);

			mockMvc.perform(post("/cliente/cadastro")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(novoCliente)))
					.andExpect(status().isCreated());

			List<Cliente> clientes = clienteRepositorio.findAll();
			assertTrue(clientes.stream().anyMatch(c -> "Maria Leopoldina".equals(c.getNome())),
					"Cliente Maria Leopoldina deve existir no repositório");
		}

		@Test
		@DisplayName("6.1.2 - Listagem geral de clientes e busca por ID com links HATEOAS")
		void testarListagemEBuscaPorId() throws Exception {
			mockMvc.perform(get("/cliente/clientes"))
					.andExpect(status().isOk())
					.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
					.andExpect(jsonPath("$[0].links").exists());

			List<Cliente> clientes = clienteRepositorio.findAll();
			assertFalse(clientes.isEmpty(), "Lista de clientes não deve ser vazia");

			Cliente primeiro = clientes.get(0);
			mockMvc.perform(get("/cliente/" + primeiro.getId()))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.id").value(primeiro.getId()))
					.andExpect(jsonPath("$.nome").value(primeiro.getNome()))
					.andExpect(jsonPath("$._links.self.href").exists())
					.andExpect(jsonPath("$._links.clientes.href").exists())
					.andExpect(jsonPath("$._links.atualizar.href").exists())
					.andExpect(jsonPath("$._links.excluir.href").exists());

			mockMvc.perform(get("/cliente/999999"))
					.andExpect(status().isNotFound());
		}

		@Test
		@DisplayName("6.1.3 - Atualização de cliente")
		void testarAtualizacaoCliente() throws Exception {
			Cliente c = new Cliente();
			c.setNome("Cliente Para Atualizar");
			c.setNomeSocial("Antes");
			c.setDataCadastro(new Date());
			c = clienteRepositorio.save(c);

			Cliente atualizacao = new Cliente();
			atualizacao.setId(c.getId());
			atualizacao.setNome("Cliente Atualizado com Sucesso");
			atualizacao.setNomeSocial("Depois");

			mockMvc.perform(put("/cliente/atualizar")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(atualizacao)))
					.andExpect(status().isOk());

			Cliente clienteAtualizado = clienteRepositorio.findById(c.getId()).orElse(null);
			assertNotNull(clienteAtualizado);
			assertEquals("Cliente Atualizado com Sucesso", clienteAtualizado.getNome());
			assertEquals("Depois", clienteAtualizado.getNomeSocial());
		}

		@Test
		@DisplayName("6.1.4 - Exclusão de cliente e verificação de exclusão em cascata")
		void testarExclusaoEmCascata() throws Exception {
			Cliente c = new Cliente();
			c.setNome("Cliente Para Exclusão Cascata");
			c.setDataCadastro(new Date());

			Endereco end = new Endereco();
			end.setRua("Rua Cascata");
			end.setNumero("10");
			end.setBairro("Bairro");
			end.setCidade("Cidade");
			end.setEstado("SP");
			c.setEndereco(end);

			Documento doc = new Documento();
			doc.setTipo("CNH");
			doc.setNumero("999998888");
			c.getDocumentos().add(doc);

			Telefone tel = new Telefone();
			tel.setDdd("11");
			tel.setNumero("911112222");
			c.getTelefones().add(tel);

			c = clienteRepositorio.save(c);
			Long clienteId = c.getId();
			Long enderecoId = c.getEndereco().getId();
			Long documentoId = c.getDocumentos().get(0).getId();
			Long telefoneId = c.getTelefones().get(0).getId();

			assertNotNull(clienteId);
			assertNotNull(enderecoId);
			assertNotNull(documentoId);
			assertNotNull(telefoneId);

			mockMvc.perform(delete("/cliente/excluir/" + clienteId))
					.andExpect(status().isOk());

			assertFalse(clienteRepositorio.findById(clienteId).isPresent(), "Cliente deve ter sido removido");
			assertFalse(enderecoRepositorio.findById(enderecoId).isPresent(),
					"Endereço em cascata deve ter sido removido");
			assertFalse(documentoRepositorio.findById(documentoId).isPresent(),
					"Documento em cascata deve ter sido removido");
			assertFalse(telefoneRepositorio.findById(telefoneId).isPresent(),
					"Telefone em cascata deve ter sido removido");
		}
	}

	@Nested
	@DisplayName("6.2. Testes de Documento (CRUD + RMM + HATEOAS)")
	class DocumentoTests {

		@Test
		@DisplayName("6.2.1 - Criação de documento via endpoint")
		void testarCadastroDocumento() throws Exception {
			Documento doc = new Documento();
			doc.setTipo("TITULO_ELEITOR");
			doc.setNumero("123456789012");

			mockMvc.perform(post("/documento/cadastro")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(doc)))
					.andExpect(status().isCreated());

			List<Documento> docs = documentoRepositorio.findAll();
			assertTrue(docs.stream()
					.anyMatch(d -> "TITULO_ELEITOR".equals(d.getTipo()) && "123456789012".equals(d.getNumero())));
		}

		@Test
		@DisplayName("6.2.2 - Listagem e busca por ID de documento com HATEOAS")
		void testarListagemEBuscaDocumento() throws Exception {
			Documento doc = new Documento();
			doc.setTipo("CERTIDAO_NASCIMENTO");
			doc.setNumero("987654");
			doc = documentoRepositorio.save(doc);

			mockMvc.perform(get("/documento/documentos"))
					.andExpect(status().isOk())
					.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
					.andExpect(jsonPath("$[0].links").exists());

			mockMvc.perform(get("/documento/" + doc.getId()))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.id").value(doc.getId()))
					.andExpect(jsonPath("$.tipo").value("CERTIDAO_NASCIMENTO"))
					.andExpect(jsonPath("$.numero").value("987654"))
					.andExpect(jsonPath("$._links.self.href").exists())
					.andExpect(jsonPath("$._links.documentos.href").exists());

			mockMvc.perform(get("/documento/999999"))
					.andExpect(status().isNotFound());
		}

		@Test
		@DisplayName("6.2.3 - Atualização direta de documento")
		void testarAtualizacaoDocumento() throws Exception {
			Documento doc = new Documento();
			doc.setTipo("PASSAPORTE");
			doc.setNumero("OLD123");
			doc = documentoRepositorio.save(doc);

			Documento atualizacao = new Documento();
			atualizacao.setId(doc.getId());
			atualizacao.setTipo("PASSAPORTE");
			atualizacao.setNumero("NEW999");

			mockMvc.perform(put("/documento/atualizar")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(atualizacao)))
					.andExpect(status().isOk());

			Documento atualizado = documentoRepositorio.findById(doc.getId()).orElse(null);
			assertNotNull(atualizado);
			assertEquals("NEW999", atualizado.getNumero());
		}

		@Test
		@DisplayName("6.2.4 - Exclusão direta de documento")
		void testarExclusaoDocumento() throws Exception {
			Documento doc = new Documento();
			doc.setTipo("DOCUMENTO_TEMP");
			doc.setNumero("000111");
			doc = documentoRepositorio.save(doc);

			mockMvc.perform(delete("/documento/excluir/" + doc.getId()))
					.andExpect(status().isOk());

			assertFalse(documentoRepositorio.findById(doc.getId()).isPresent());
		}
	}

	@Nested
	@DisplayName("6.3. Testes de Endereço (CRUD + RMM + HATEOAS)")
	class EnderecoTests {

		@Test
		@DisplayName("6.3.1 - Criação de endereço via endpoint")
		void testarCadastroEndereco() throws Exception {
			Endereco end = new Endereco();
			end.setEstado("Minas Gerais");
			end.setCidade("Belo Horizonte");
			end.setBairro("Savassi");
			end.setRua("Rua Pernambuco");
			end.setNumero("100");
			end.setCodigoPostal("30130150");
			end.setInformacoesAdicionais("Apto 502");

			mockMvc.perform(post("/endereco/cadastro")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(end)))
					.andExpect(status().isCreated());

			List<Endereco> enderecos = enderecoRepositorio.findAll();
			assertTrue(enderecos.stream()
					.anyMatch(e -> "Belo Horizonte".equals(e.getCidade()) && "30130150".equals(e.getCodigoPostal())));
		}

		@Test
		@DisplayName("6.3.2 - Listagem e busca por ID de endereço com HATEOAS")
		void testarListagemEBuscaEndereco() throws Exception {
			Endereco end = new Endereco();
			end.setEstado("PR");
			end.setCidade("Curitiba");
			end.setBairro("Batel");
			end.setRua("Avenida do Batel");
			end.setNumero("200");
			end = enderecoRepositorio.save(end);

			mockMvc.perform(get("/endereco/enderecos"))
					.andExpect(status().isOk())
					.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
					.andExpect(jsonPath("$[0].links").exists());

			mockMvc.perform(get("/endereco/" + end.getId()))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.id").value(end.getId()))
					.andExpect(jsonPath("$.cidade").value("Curitiba"))
					.andExpect(jsonPath("$._links.self.href").exists())
					.andExpect(jsonPath("$._links.enderecos.href").exists());

			mockMvc.perform(get("/endereco/999999"))
					.andExpect(status().isNotFound());
		}

		@Test
		@DisplayName("6.3.3 - Atualização direta de endereço")
		void testarAtualizacaoEndereco() throws Exception {
			Endereco end = new Endereco();
			end.setEstado("SP");
			end.setCidade("Campinas");
			end.setBairro("Cambuí");
			end.setRua("Rua Coronel Quirino");
			end.setNumero("50");
			end.setCodigoPostal("13025001");
			end = enderecoRepositorio.save(end);

			Endereco atualizacao = new Endereco();
			atualizacao.setId(end.getId());
			atualizacao.setNumero("55");
			atualizacao.setCodigoPostal("13025999");
			atualizacao.setInformacoesAdicionais("Conjunto 3");

			mockMvc.perform(put("/endereco/atualizar")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(atualizacao)))
					.andExpect(status().isOk());

			Endereco atualizado = enderecoRepositorio.findById(end.getId()).orElse(null);
			assertNotNull(atualizado);
			assertEquals("55", atualizado.getNumero());
			assertEquals("13025999", atualizado.getCodigoPostal());
			assertEquals("Conjunto 3", atualizado.getInformacoesAdicionais());
			assertEquals("Campinas", atualizado.getCidade());
		}

		@Test
		@DisplayName("6.3.4 - Exclusão direta de endereço")
		void testarExclusaoEndereco() throws Exception {
			Endereco end = new Endereco();
			end.setEstado("RS");
			end.setCidade("Porto Alegre");
			end.setBairro("Moinhos de Vento");
			end.setRua("Rua Padre Chagas");
			end.setNumero("300");
			end = enderecoRepositorio.save(end);

			mockMvc.perform(delete("/endereco/excluir/" + end.getId()))
					.andExpect(status().isOk());

			assertFalse(enderecoRepositorio.findById(end.getId()).isPresent());
		}
	}

	@Nested
	@DisplayName("6.4. Testes de Telefone (CRUD + RMM + HATEOAS)")
	class TelefoneTests {

		@Test
		@DisplayName("6.4.1 - Criação de telefone via endpoint")
		void testarCadastroTelefone() throws Exception {
			Telefone tel = new Telefone();
			tel.setDdd("12");
			tel.setNumero("988887777");

			mockMvc.perform(post("/telefone/cadastro")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(tel)))
					.andExpect(status().isCreated());

			List<Telefone> telefones = telefoneRepositorio.findAll();
			assertTrue(telefones.stream().anyMatch(t -> "12".equals(t.getDdd()) && "988887777".equals(t.getNumero())));
		}

		@Test
		@DisplayName("6.4.2 - Listagem e busca por ID de telefone com HATEOAS")
		void testarListagemEBuscaTelefone() throws Exception {
			Telefone tel = new Telefone();
			tel.setDdd("19");
			tel.setNumero("977776666");
			tel = telefoneRepositorio.save(tel);

			mockMvc.perform(get("/telefone/telefones"))
					.andExpect(status().isOk())
					.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
					.andExpect(jsonPath("$[0].links").exists());

			mockMvc.perform(get("/telefone/" + tel.getId()))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.id").value(tel.getId()))
					.andExpect(jsonPath("$.ddd").value("19"))
					.andExpect(jsonPath("$.numero").value("977776666"))
					.andExpect(jsonPath("$._links.self.href").exists())
					.andExpect(jsonPath("$._links.telefones.href").exists());

			mockMvc.perform(get("/telefone/999999"))
					.andExpect(status().isNotFound());
		}

		@Test
		@DisplayName("6.4.3 - Atualização direta de telefone")
		void testarAtualizacaoTelefone() throws Exception {
			Telefone tel = new Telefone();
			tel.setDdd("11");
			tel.setNumero("912341234");
			tel = telefoneRepositorio.save(tel);

			Telefone atualizacao = new Telefone();
			atualizacao.setId(tel.getId());
			atualizacao.setNumero("999991111");

			mockMvc.perform(put("/telefone/atualizar")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(atualizacao)))
					.andExpect(status().isOk());

			Telefone atualizado = telefoneRepositorio.findById(tel.getId()).orElse(null);
			assertNotNull(atualizado);
			assertEquals("999991111", atualizado.getNumero());
			assertEquals("11", atualizado.getDdd());
		}

		@Test
		@DisplayName("6.4.4 - Exclusão direta de telefone")
		void testarExclusaoTelefone() throws Exception {
			Telefone tel = new Telefone();
			tel.setDdd("81");
			tel.setNumero("955554444");
			tel = telefoneRepositorio.save(tel);

			mockMvc.perform(delete("/telefone/excluir/" + tel.getId()))
					.andExpect(status().isOk());

			assertFalse(telefoneRepositorio.findById(tel.getId()).isPresent());
		}
	}

	@Nested
	@DisplayName("6.5. Testes de Documentação Swagger / OpenAPI")
	class SwaggerTests {

		@Test
		@DisplayName("6.5.1 - Verificar disponibilidade da especificação OpenAPI e interface Swagger UI")
		void testarSwaggerDisponivel() throws Exception {
			mockMvc.perform(get("/v3/api-docs"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.info.title").value("AutoBots — API AutoManager (ATVII)"));

			mockMvc.perform(get("/swagger-ui/index.html"))
					.andExpect(status().isOk());
		}
	}
}

