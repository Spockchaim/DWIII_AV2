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
import com.autobots.automanager.entidades.Documento;
import com.autobots.automanager.modelos.AdicionadorLinkDocumento;
import com.autobots.automanager.modelos.DocumentoAtualizador;
import com.autobots.automanager.modelos.DocumentoSelecionador;
import com.autobots.automanager.repositorios.ClienteRepositorio;
import com.autobots.automanager.repositorios.DocumentoRepositorio;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "Documentos", description = "Endpoints para gerenciamento de documentos (RMM Níveis 0 a 3 + HATEOAS)")
public class DocumentoControle {
	@Autowired
	private DocumentoRepositorio repositorio;
	@Autowired
	private DocumentoSelecionador selecionador;
	@Autowired
	private ClienteRepositorio clienteRepositorio;
	@Autowired
	private AdicionadorLinkDocumento adicionadorLink;

	@Operation(summary = "Obter documento por ID", description = "Busca um documento específico pelo ID com links HATEOAS")
	@GetMapping("/documento/{id}")
	public ResponseEntity<Documento> obterDocumento(@PathVariable long id) {
		List<Documento> documentos = repositorio.findAll();
		Documento documento = selecionador.selecionar(documentos, id);
		if (documento == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		adicionadorLink.adicionarLink(documento);
		return new ResponseEntity<>(documento, HttpStatus.OK);
	}

	@Operation(summary = "Listar todos os documentos", description = "Retorna todos os documentos cadastrados com links HATEOAS")
	@GetMapping({ "/documento/documentos", "/documentos" })
	public ResponseEntity<List<Documento>> obterDocumentos() {
		List<Documento> documentos = repositorio.findAll();
		if (documentos.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		adicionadorLink.adicionarLink(documentos);
		return new ResponseEntity<>(documentos, HttpStatus.OK);
	}

	@Operation(summary = "Cadastrar novo documento", description = "Cadastra um novo documento no sistema")
	@PostMapping("/documento/cadastro")
	public ResponseEntity<?> cadastrarDocumento(@RequestBody Documento documento) {
		if (documento == null) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		if (documento.getId() == null) {
			repositorio.save(documento);
			return new ResponseEntity<>(HttpStatus.CREATED);
		}
		return new ResponseEntity<>(HttpStatus.CONFLICT);
	}

	@Operation(summary = "Atualizar documento", description = "Atualiza tipo ou número de um documento existente")
	@PutMapping("/documento/atualizar")
	public ResponseEntity<?> atualizarDocumento(@RequestBody Documento atualizacao) {
		if (atualizacao == null || atualizacao.getId() == null) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		Documento documento = repositorio.findById(atualizacao.getId()).orElse(null);
		if (documento != null) {
			DocumentoAtualizador atualizador = new DocumentoAtualizador();
			atualizador.atualizar(documento, atualizacao);
			repositorio.save(documento);
			return new ResponseEntity<>(HttpStatus.OK);
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

	@Operation(summary = "Excluir documento por ID", description = "Remove um documento pelo ID desvinculando-o de qualquer cliente")
	@DeleteMapping("/documento/excluir/{id}")
	public ResponseEntity<?> excluirDocumento(@PathVariable long id) {
		Documento documento = repositorio.findById(id).orElse(null);
		if (documento != null) {
			for (Cliente cliente : clienteRepositorio.findAll()) {
				if (cliente.getDocumentos().removeIf(d -> d.getId().equals(id))) {
					clienteRepositorio.save(cliente);
				}
			}
			if (repositorio.existsById(id)) {
				repositorio.delete(documento);
			}
			return new ResponseEntity<>(HttpStatus.OK);
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

	@Operation(summary = "Excluir documento via corpo JSON", description = "Remove um documento informando o ID no corpo da requisição")
	@DeleteMapping("/documento/excluir")
	public ResponseEntity<?> excluirDocumento(@RequestBody Documento exclusao) {
		if (exclusao == null || exclusao.getId() == null) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		return excluirDocumento(exclusao.getId());
	}
}
