package com.autobots.automanager.controles;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.autobots.automanager.entidades.Cliente;
import com.autobots.automanager.entidades.Endereco;
import com.autobots.automanager.modelos.AdicionadorLinkEndereco;
import com.autobots.automanager.modelos.EnderecoAtualizador;
import com.autobots.automanager.modelos.EnderecoSelecionador;
import com.autobots.automanager.repositorios.ClienteRepositorio;
import com.autobots.automanager.repositorios.EnderecoRepositorio;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "Endereços", description = "Endpoints para gerenciamento de endereços (RMM Níveis 0 a 3 + HATEOAS)")
public class EnderecoControle {
	@Autowired
	private EnderecoRepositorio repositorio;
	@Autowired
	private EnderecoSelecionador selecionador;
	@Autowired
	private ClienteRepositorio clienteRepositorio;
	@Autowired
	private AdicionadorLinkEndereco adicionadorLink;

	@Operation(summary = "Obter endereço por ID", description = "Busca um endereço específico pelo ID com links HATEOAS")
	@GetMapping("/endereco/{id}")
	public ResponseEntity<Endereco> obterEndereco(@PathVariable long id) {
		List<Endereco> enderecos = repositorio.findAll();
		Endereco endereco = selecionador.selecionar(enderecos, id);
		if (endereco == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		adicionadorLink.adicionarLink(endereco);
		return new ResponseEntity<>(endereco, HttpStatus.OK);
	}

	@Operation(summary = "Listar todos os endereços", description = "Retorna todos os endereços cadastrados com links HATEOAS")
	@GetMapping({ "/endereco/enderecos", "/enderecos" })
	public ResponseEntity<List<Endereco>> obterEnderecos() {
		List<Endereco> enderecos = repositorio.findAll();
		if (enderecos.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		adicionadorLink.adicionarLink(enderecos);
		return new ResponseEntity<>(enderecos, HttpStatus.OK);
	}

	@Operation(summary = "Cadastrar novo endereço", description = "Cadastra um novo endereço no sistema")
	@PostMapping("/endereco/cadastro")
	public ResponseEntity<?> cadastrarEndereco(@RequestBody Endereco endereco) {
		if (endereco == null) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		if (endereco.getId() == null) {
			repositorio.save(endereco);
			return new ResponseEntity<>(HttpStatus.CREATED);
		}
		return new ResponseEntity<>(HttpStatus.CONFLICT);
	}

	@Operation(summary = "Atualizar endereço", description = "Atualiza dados de um endereço existente")
	@PutMapping("/endereco/atualizar")
	public ResponseEntity<?> atualizarEndereco(@RequestBody Endereco atualizacao) {
		if (atualizacao == null || atualizacao.getId() == null) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		Endereco endereco = repositorio.findById(atualizacao.getId()).orElse(null);
		if (endereco != null) {
			EnderecoAtualizador atualizador = new EnderecoAtualizador();
			atualizador.atualizar(endereco, atualizacao);
			repositorio.save(endereco);
			return new ResponseEntity<>(HttpStatus.OK);
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

	@Operation(summary = "Excluir endereço por ID", description = "Remove um endereço pelo ID desvinculando-o de qualquer cliente")
	@DeleteMapping("/endereco/excluir/{id}")
	public ResponseEntity<?> excluirEndereco(@PathVariable long id) {
		Endereco endereco = repositorio.findById(id).orElse(null);
		if (endereco != null) {
			for (Cliente cliente : clienteRepositorio.findAll()) {
				if (cliente.getEndereco() != null && cliente.getEndereco().getId().equals(id)) {
					cliente.setEndereco(null);
					clienteRepositorio.save(cliente);
				}
			}
			if (repositorio.existsById(id)) {
				repositorio.delete(endereco);
			}
			return new ResponseEntity<>(HttpStatus.OK);
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

	@Operation(summary = "Excluir endereço via corpo JSON", description = "Remove um endereço informando o ID no corpo da requisição")
	@DeleteMapping("/endereco/excluir")
	public ResponseEntity<?> excluirEndereco(@RequestBody Endereco exclusao) {
		if (exclusao == null || exclusao.getId() == null) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		return excluirEndereco(exclusao.getId());
	}
}
