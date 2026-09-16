const AUTH = "Basic " + btoa("admin:123456");

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
    return `<span class="badge ${classe}">mensalidade ${usuario.statusMensalidade.toLowerCase()}</span>`;
}

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
    document.getElementById("usuarios").innerHTML = usuarios.map(u => `
        <li class="item">
            <span>${u.id} - ${u.nome} | plano: ${u.plano} | créditos: ${u.creditosCursos} | concluídos: ${u.cursosConcluidosComSucesso} | moedas: ${u.moedas}</span>
            <span>${badgeMensalidade(u)} ${badgeAcesso(u)}</span>
        </li>
    `).join("");
}

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
    document.getElementById("cursos").innerHTML = cursos.map(c =>
        `<li class="item"><span>${c.id} - ${c.titulo}</span></li>`
    ).join("");
}

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
}

async function concluirMatricula() {
    const id = document.getElementById("matriculaId").value;
    const payload = { notaFinal: Number(document.getElementById("notaFinal").value) };
    const resultado = await executar(
        () => api(`/api/matriculas/${id}/concluir`, { method: "PUT", body: JSON.stringify(payload) })
    );
    mostrarMensagem(`Matrícula ${resultado.id} concluída com status ${resultado.status}.`, "sucesso");
}

async function listarMatriculasUsuario() {
    const usuarioId = document.getElementById("consultaUsuarioId").value;
    const matriculas = await executar(() => api(`/api/matriculas/usuario/${usuarioId}`));
    document.getElementById("matriculas").innerHTML = matriculas.map(m =>
        `<li class="item"><span>${m.id} - ${m.cursoTitulo} | status: ${m.status} | nota: ${m.notaFinal ?? '-'} | bônus: ${m.bonus}</span></li>`
    ).join("");
}

async function atualizarMensalidade() {
    const usuarioId = document.getElementById("mensalidadeUsuarioId").value;
    const status = document.getElementById("mensalidadeStatus").value;
    const usuario = await executar(
        () => api(`/api/usuarios/${usuarioId}/mensalidade`, { method: "PUT", body: JSON.stringify({ status }) }),
        "Mensalidade atualizada."
    );
    document.getElementById("mensalidadeResultado").innerHTML = `
        <li class="item">
            <span>${usuario.id} - ${usuario.nome}</span>
            <span>${badgeMensalidade(usuario)} ${badgeAcesso(usuario)}</span>
        </li>
    `;
}
