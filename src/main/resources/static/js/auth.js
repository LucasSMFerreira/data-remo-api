// js/auth.js

async function cadastrarAtleta(event) {
  event.preventDefault();

  const nome = document.getElementById('nome').value.trim();
  const idade = Number(document.getElementById('idade').value);
  const altura = Number(document.getElementById('altura').value);
  const clube = document.getElementById('clube').value.trim(); // se não usar, ignore
  const modalidade = document.getElementById('modalidade').value.trim();
  const objetivo = document.getElementById('objetivo').value.trim();
  const email = document.getElementById('email').value.trim();
  const senha = document.getElementById('senha').value;
  const confirmar = document.getElementById('confirmarSenha').value;
  const plano = document.querySelector('input[name="plano"]:checked')?.value || 'FREE';

  const msgErro = document.getElementById('msg-erro-cadastro');

  if (senha !== confirmar) {
    msgErro.textContent = 'As senhas não coincidem.';
    return;
  }

  const payload = {
    nome,
    idade,
    altura,
    modalidade,
    objetivo,
    plano,
    email,
    senha
  };

  try {
    const response = await fetch(`${API}/api/atletas`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });

    if (!response.ok) {
      const texto = await response.text();
      console.error('Erro backend:', texto);
      msgErro.textContent = 'Erro ao criar conta. Verifique se o nome ou email já estão em uso.';
      return;
    }

    const atleta = await response.json();

    // salvar info básica do atleta para usar no dashboard/ranking
    localStorage.setItem('atletaId', atleta.id);
    localStorage.setItem('atletaNome', atleta.nome);
    localStorage.setItem('atletaEmail', atleta.email);

    // redirecionar para o dashboard
    window.location.href = 'dashboard.html';
  } catch (e) {
    console.error(e);
    msgErro.textContent = 'Erro de conexão ao criar conta.';
  }
}

async function loginAtleta(event) {
  event.preventDefault();

  const nome = document.getElementById('loginNome').value.trim();
  const senha = document.getElementById('loginSenha').value;
  const msgErro = document.getElementById('msg-erro-login');

  try {
    const response = await fetch(`${API}/api/atletas/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ nome, senha })
    });

    if (!response.ok) {
      const texto = await response.text();
      console.error('Erro backend login:', texto);
      msgErro.textContent = 'Nome ou senha incorretos.';
      return;
    }

    const atleta = await response.json();
    localStorage.setItem('atletaId', atleta.id);
    localStorage.setItem('atletaNome', atleta.nome);
    localStorage.setItem('atletaEmail', atleta.email);

    window.location.href = 'dashboard.html';
  } catch (e) {
    console.error(e);
    msgErro.textContent = 'Erro de conexão ao entrar.';
  }
}
