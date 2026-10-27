package com.exemplo.usuario.assinatura;

import com.exemplo.usuario.assinatura.internal.Assinatura;
import com.exemplo.usuario.assinatura.internal.AssinaturaRepository;
import com.exemplo.usuario.shared.RecursoNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Camada: SERVICE (API publica do modulo assinatura).
// Outros modulos so falam com a assinatura por aqui, usando o id do usuario.
@Service
public class AssinaturaService {

    // Constante criada uma vez so (bloco estatico da classe).
    private static final AssinaturaResponseDTO SEM_ASSINATURA =
            new AssinaturaResponseDTO(null, null, null, null);

    private final AssinaturaRepository repository;

    public AssinaturaService(AssinaturaRepository repository) {
        this.repository = repository;
    }

    // Todo novo usuario nasce com assinatura BASICO e zero creditos/conclusoes/moedas.
    @Transactional
    public void criarParaUsuario(Long usuarioId) {
        repository.save(new Assinatura(usuarioId));
    }

    // Usuario sem assinatura (dados antigos) gera todos os campos nulos.
    @Transactional(readOnly = true)
    public AssinaturaResponseDTO resumo(Long usuarioId) {
        return repository.findByUsuarioId(usuarioId)
                .map(Assinatura::paraResumo)
                .orElse(SEM_ASSINATURA);
    }

    // A entidade gerenciada pelo JPA e salva no fim da transacao (dirty checking).
    @Transactional
    public void consumirCredito(Long usuarioId) {
        buscar(usuarioId).getCarteira().consumirCredito();
    }

    @Transactional
    public void registrarConclusaoComSucesso(Long usuarioId) {
        buscar(usuarioId).registrarConclusaoComSucesso();
    }

    private Assinatura buscar(Long usuarioId) {
        return repository.findByUsuarioId(usuarioId)
                .orElseThrow(RecursoNaoEncontradoException.com("Assinatura nao encontrada"));
    }
}
