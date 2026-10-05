const AUTH = "Basic " + btoa("admin:123456");

const SECOES = {
    usuarios: { titulo: "Usuários", colunas: ["ID", "Nome", "Plano", "Créditos", "Concluídos", "Moedas", "Mensalidade", "Acesso"] },
    cursos: { titulo: "Cursos", colunas: ["ID", "Título", "Descrição"] },
    matriculas: { titulo: "Matrículas", colunas: ["ID", "Curso", "Status", "Nota", "Bônus"] },
    mensalidade: { titulo: "Mensalidade", colunas: ["ID", "Nome", "Mensalidade", "Acesso"] }
};

let secaoAtual = "usuarios";

function mostrarMensagem(texto, tipo) {
    const el = document.getElementById("mensagem");
    // Cancela qualquer timer de uma mensagem anterior antes de exibir a nova,
    // senao um "sucesso" antigo pode apagar um "erro" mostrado depois dele.
    clearTimeout(mostrarMensagem._timer);
    el.textContent = texto;
    el.className = tipo;
    if (tipo === "sucesso") {
        mostrarMensagem._timer = setTimeout(() => {
            el.className = "";
            el.textContent = "";
        }, 4000);
    }
}

async function api(url, options = {}) {
    const response = await fetch(url, {
        ...options,
        headers: {
            "Content-Type": "application/json",
            "Authorization": AUTH,
            ...(options.headers || {})
        }
    });

    if (response.status === 204) return null;
    const text = await response.text();
    if (!response.ok) {
        let mensagemErro = text || "Erro na requisição";
        try {
            const corpo = JSON.parse(text);
            mensagemErro = corpo.erro || mensagemErro;
        } catch (ignored) {
            // corpo nao era JSON, mantem o texto original
        }
        throw new Error(mensagemErro);
    }
    return text ? JSON.parse(text) : null;
}

async function executar(acao, mensagemSucesso) {
    try {
        const resultado = await acao();
        if (mensagemSucesso) mostrarMensagem(mensagemSucesso, "sucesso");
        return resultado;
    } catch (erro) {
        mostrarMensagem(erro.message, "erro");
        throw erro;
    }
}

function badgeAcesso(usuario) {
    if (usuario.temAcessoAoCurso) {
        return `<span class="badge liberado">acesso liberado</span>`;
    }
    if (usuario.plataformaCongelada) {
        return `<span class="badge bloqueado">plataforma congelada</span>`;
    }
    return `<span class="badge bloqueado">sem acesso</span>`;
}

function badgeMensalidade(usuario) {
    if (!usuario.statusMensalidade) {
        return `<span class="badge neutro">sem mensalidade</span>`;
    }
    const classe = usuario.statusMensalidade === "PAGA" ? "liberado" : "pendente";
    return `<span class="badge ${classe}">${usuario.statusMensalidade.toLowerCase()}</span>`;
}

// ---------- Tabela central ----------

function renderTabela(colunas, linhas) {
    document.getElementById("tabela-thead-row").innerHTML = colunas.map(c => `<th>${c}</th>`).join("");
    document.getElementById("tabela-tbody").innerHTML = linhas.length
        ? linhas.map(linha => `<tr>${linha.map(celula => `<td>${celula}</td>`).join("")}</tr>`).join("")
        : `<tr><td class="vazio" colspan="${colunas.length}">Nenhum registro carregado.</td></tr>`;
}

function selecionarSecao(secao) {
    secaoAtual = secao;

    document.querySelectorAll(".nav-item").forEach(btn => {
        btn.classList.toggle("ativo", btn.dataset.secao === secao);
    });
    document.querySelectorAll(".painel-acao").forEach(el => {
        el.hidden = el.dataset.secaoPainel !== secao;
    });

    document.getElementById("tabela-titulo").textContent = SECOES[secao].titulo;
    renderTabela(SECOES[secao].colunas, []);

    if (secao === "usuarios") listarUsuarios();
    if (secao === "cursos") listarCursos();
    // matriculas e mensalidade dependem de um ID informado pelo usuario,
    // entao a tabela comeca vazia ate uma acao ser executada.
}

function atualizarSecaoAtual() {
    if (secaoAtual === "usuarios") listarUsuarios();
    else if (secaoAtual === "cursos") listarCursos();
    else if (secaoAtual === "matriculas") listarMatriculasUsuario();
    else mostrarMensagem("Informe o usuário e atualize a mensalidade para ver o resultado.", "erro");
}

// ---------- Usuarios ----------

async function criarUsuario() {
    const payload = {
        nome: document.getElementById("nome").value,
        email: document.getElementById("email").value,
        senha: document.getElementById("senha").value
    };
    await executar(
        () => api("/api/usuarios", { method: "POST", body: JSON.stringify(payload) }),
        "Usuário criado com sucesso."
    );
    listarUsuarios();
}

async function listarUsuarios() {
    const usuarios = await executar(() => api("/api/usuarios"));
    renderTabela(SECOES.usuarios.colunas, usuarios.map(u => [
        u.id, u.nome, u.plano, u.creditosCursos, u.cursosConcluidosComSucesso, u.moedas,
        badgeMensalidade(u), badgeAcesso(u)
    ]));
}

// ---------- Cursos ----------

async function criarCurso() {
    const payload = {
        titulo: document.getElementById("tituloCurso").value,
        descricao: document.getElementById("descricaoCurso").value
    };
    await executar(
        () => api("/api/cursos", { method: "POST", body: JSON.stringify(payload) }),
        "Curso criado com sucesso."
    );
    listarCursos();
}

async function listarCursos() {
    const cursos = await executar(() => api("/api/cursos"));
    renderTabela(SECOES.cursos.colunas, cursos.map(c => [c.id, c.titulo, c.descricao ?? "-"]));
}

// ---------- Matriculas ----------

async function matricular() {
    const payload = {
        usuarioId: Number(document.getElementById("matUsuarioId").value),
        cursoId: Number(document.getElementById("matCursoId").value),
        bonus: document.getElementById("matBonus").value === "true"
    };
    const resultado = await executar(
        () => api("/api/matriculas", { method: "POST", body: JSON.stringify(payload) })
    );
    mostrarMensagem(`Matrícula ${resultado.id} criada para ${resultado.usuarioNome}.`, "sucesso");
    document.getElementById("consultaUsuarioId").value = payload.usuarioId;
    listarMatriculasUsuario();
}

async function concluirMatricula() {
    const id = document.getElementById("matriculaId").value;
    const payload = { notaFinal: Number(document.getElementById("notaFinal").value) };
    const resultado = await executar(
        () => api(`/api/matriculas/${id}/concluir`, { method: "PUT", body: JSON.stringify(payload) })
    );
    mostrarMensagem(`Matrícula ${resultado.id} concluída com status ${resultado.status}.`, "sucesso");
    if (document.getElementById("consultaUsuarioId").value) listarMatriculasUsuario();
}

async function listarMatriculasUsuario() {
    const usuarioId = document.getElementById("consultaUsuarioId").value;
    const matriculas = await executar(() => api(`/api/matriculas/usuario/${usuarioId}`));
    renderTabela(SECOES.matriculas.colunas, matriculas.map(m => [
        m.id, m.cursoTitulo, m.status, m.notaFinal ?? "-", m.bonus ? "Sim" : "Não"
    ]));
}

// ---------- Mensalidade ----------

async function atualizarMensalidade() {
    const usuarioId = document.getElementById("mensalidadeUsuarioId").value;
    const status = document.getElementById("mensalidadeStatus").value;
    const usuario = await executar(
        () => api(`/api/usuarios/${usuarioId}/mensalidade`, { method: "PUT", body: JSON.stringify({ status }) }),
        "Mensalidade atualizada."
    );
    renderTabela(SECOES.mensalidade.colunas, [[
        usuario.id, usuario.nome, badgeMensalidade(usuario), badgeAcesso(usuario)
    ]]);
}

// ---------- Inicializacao ----------

selecionarSecao("usuarios");
