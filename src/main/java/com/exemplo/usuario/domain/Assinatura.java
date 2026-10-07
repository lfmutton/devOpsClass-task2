package com.exemplo.usuario.domain;

import jakarta.persistence.*;

// Camada: DOMINIO.
// Entidade JPA que representa a assinatura do usuario.
// Os saldos ficam em CarteiraAssinatura e o plano/conclusoes em ProgressoAssinatura.
@Entity
@Table(name = "assinaturas")
public class Assinatura {

    private static final int CREDITOS_POR_CONCLUSAO = 3;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private CarteiraAssinatura carteira = new CarteiraAssinatura();

    @Embedded
    private ProgressoAssinatura progresso = new ProgressoAssinatura();

    // Relacionamento 1:1 com Usuario.
    // fetch = LAZY significa que o usuario sera carregado sob demanda.
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    // Construtor vazio exigido pelo JPA.
    protected Assinatura() {
    }

    // Construtor de negocio.
    // Todo novo usuario nasce com assinatura BASICO e zero creditos/moedas.
    public Assinatura(Usuario usuario) {
        this.usuario = usuario;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public CarteiraAssinatura getCarteira() {
        return carteira;
    }

    public ProgressoAssinatura getProgresso() {
        return progresso;
    }

    // Metodo de dominio rico:
    // - soma conclusao com sucesso (e promove para PREMIUM ao atingir 12)
    // - entrega 3 creditos
    public void registrarConclusaoComSucesso() {
        progresso.registrarConclusao();
        carteira.adicionarCreditos(CREDITOS_POR_CONCLUSAO);
    }
}
