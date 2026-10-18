package com.autobots.automanager.modelos;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;

import com.autobots.automanager.controles.ClienteControle;
import com.autobots.automanager.entidades.Cliente;

@Component
public class AdicionadorLinkCliente implements AdicionadorLink<Cliente> {

	@Autowired
	private AdicionadorLinkDocumento adicionadorLinkDocumento;

	@Autowired
	private AdicionadorLinkEndereco adicionadorLinkEndereco;

	@Autowired
	private AdicionadorLinkTelefone adicionadorLinkTelefone;

	@Override
	public void adicionarLink(List<Cliente> lista) {
		if (lista == null) {
			return;
		}
		for (Cliente cliente : lista) {
			adicionarLink(cliente);
		}
	}

	@Override
	public void adicionarLink(Cliente objeto) {
		if (objeto == null || objeto.getId() == null) {
			return;
		}
		long id = objeto.getId();
		Link linkProprio = WebMvcLinkBuilder
				.linkTo(WebMvcLinkBuilder
						.methodOn(ClienteControle.class)
						.obterCliente(id))
				.withSelfRel();
		objeto.add(linkProprio);

		Link linkLista = WebMvcLinkBuilder
				.linkTo(WebMvcLinkBuilder
						.methodOn(ClienteControle.class)
						.obterClientes())
				.withRel("clientes");
		objeto.add(linkLista);

		Link linkAtualizar = WebMvcLinkBuilder
				.linkTo(WebMvcLinkBuilder
						.methodOn(ClienteControle.class)
						.atualizarCliente(null))
				.withRel("atualizar");
		objeto.add(linkAtualizar);

		Link linkExcluir = WebMvcLinkBuilder
				.linkTo(WebMvcLinkBuilder
						.methodOn(ClienteControle.class)
						.excluirCliente(id))
				.withRel("excluir");
		objeto.add(linkExcluir);

		if (objeto.getEndereco() != null) {
			adicionadorLinkEndereco.adicionarLink(objeto.getEndereco());
		}
		if (objeto.getDocumentos() != null) {
			adicionadorLinkDocumento.adicionarLink(objeto.getDocumentos());
		}
		if (objeto.getTelefones() != null) {
			adicionadorLinkTelefone.adicionarLink(objeto.getTelefones());
		}
	}
}