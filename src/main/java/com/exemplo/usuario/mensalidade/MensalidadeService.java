package com.exemplo.usuario.mensalidade;

import com.exemplo.usuario.mensalidade.internal.Mensalidade;
import com.exemplo.usuario.mensalidade.internal.MensalidadeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

// API PUBLICA do modulo mensalidade.
// Unica porta de entrada para os outros modulos: eles conhecem apenas o id do usuario
// e o enum StatusMensalidade. A entidade e o repository ficam escondidos no pacote internal.
@Service
public class MensalidadeService {

    private final MensalidadeRepository repository;

    public MensalidadeService(MensalidadeRepository repository) {
        this.repository = repository;
    }

    // Define o status da mensalidade do usuario.
    // Se o usuario ainda nao tem mensalidade, cria uma nova; senao altera a existente.
    @Transactional
    public void definirStatus(Long usuarioId, StatusMensalidade status) {
        Mensalidade mensalidade = repository.findByUsuarioId(usuarioId)
                .orElseGet(() -> new Mensalidade(usuarioId, status));
        mensalidade.alterarStatus(status);
        repository.save(mensalidade);
    }

    // Usuario sem mensalidade e tratado como "nao paga".
    @Transactional(readOnly = true)
    public boolean estaPaga(Long usuarioId) {
        return repository.findByUsuarioId(usuarioId)
                .map(Mensalidade::isPaga)
                .orElse(false);
    }

    // Devolve o status atual, ou Optional vazio se o usuario nao tem mensalidade.
    @Transactional(readOnly = true)
    public Optional<StatusMensalidade> buscarStatus(Long usuarioId) {
        return repository.findByUsuarioId(usuarioId)
                .map(Mensalidade::getStatus);
    }
}
