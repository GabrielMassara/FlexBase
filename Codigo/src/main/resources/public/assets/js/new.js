// Animação, troca de tela e cadastro da aplicação
document.addEventListener('DOMContentLoaded', function () {
    let appData = {};
    const form = document.getElementById('novaAplicacaoForm');
    const btn = form.querySelector('button');
    const btnText = btn.querySelector('.btn-text');
    const spinner = btn.querySelector('.loading-spinner');
    const alertContainer = document.getElementById('alertContainer');
    const authCard = document.querySelector('.auth-card');

    form.addEventListener('submit', function (e) {
        e.preventDefault();
        appData.nome = document.getElementById('nomeAplicacao').value;
        appData.descricao = document.getElementById('descricaoAplicacao').value;

        authCard.classList.add('animate__animated', 'animate__fadeOutLeft');
        setTimeout(() => {
            authCard.innerHTML = `
					<div class='auth-logo text-center mb-4'>
						<img src='../../assets/img/logo.png' alt='FlexBase Logo' width='50'>
						<h2 class='mt-2'>Banco de Dados</h2>
						<p class='text-white-50'>Escolha um nome para o banco de dados da aplicação <strong>${appData.nome}</strong></p>
					</div>
					<form id='dbForm'>
						<div class='form-floating mb-3'>
							<input type='text' class='form-control' id='nomeBanco' placeholder='Nome do banco de dados' required autofocus>
							<label for='nomeBanco'><i class='bi bi-database me-2'></i>Nome do banco de dados</label>
						</div>
						<button type='submit' class='btn btn-success w-100'>
							<span class='btn-text'>Finalizar</span>
							<div class='loading-spinner ms-2' style='display:none;'></div>
						</button>
					</form>
					<div id='alertContainer'></div>
				`;
            authCard.classList.remove('animate__fadeOutLeft');
            authCard.classList.add('animate__fadeInRight');

            const dbForm = document.getElementById('dbForm');
            const dbBtn = dbForm.querySelector('button');
            const dbBtnText = dbBtn.querySelector('.btn-text');
            const dbSpinner = dbBtn.querySelector('.loading-spinner');
            const dbAlert = document.getElementById('alertContainer');
            dbForm.addEventListener('submit', async function (ev) {
                ev.preventDefault();
                appData.banco = document.getElementById('nomeBanco').value;
                dbBtn.disabled = true;
                dbBtnText.textContent = 'Finalizando...';
                dbSpinner.style.display = 'inline-block';
                dbAlert.innerHTML = '';

                // Monta o body para o endpoint
                const body = {
                    nome: appData.nome,
                    readme: appData.descricao,
                    nomeBanco: appData.banco,
                    schemaBanco: { tabelas: [] }
                };
                // Recupera token do localStorage
                const token = localStorage.getItem('token');
                try {
                    const response = await fetch('http://localhost:80/api/aplicacoes', {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json',
                            'Authorization': 'Bearer ' + token
                        },
                        body: JSON.stringify(body)
                    });
                    const result = await response.json();
                    dbBtn.disabled = false;
                    dbBtnText.textContent = 'Finalizar';
                    dbSpinner.style.display = 'none';
                    if (response.ok && result.success) {
                        dbAlert.innerHTML = `<div class='alert alert-success mt-3'>Aplicação <strong>${appData.nome}</strong> criada com sucesso!</div>`;
                        setTimeout(() => {
                            window.location.href = '../home/index.html';
                        }, 1500);
                    } else {
                        dbAlert.innerHTML = `<div class='alert alert-danger mt-3'>Erro ao criar aplicação: ${result.message || 'Verifique os dados e tente novamente.'}</div>`;
                    }
                } catch (err) {
                    dbBtn.disabled = false;
                    dbBtnText.textContent = 'Finalizar';
                    dbSpinner.style.display = 'none';
                    dbAlert.innerHTML = `<div class='alert alert-danger mt-3'>Erro de conexão. Tente novamente.</div>`;
                }
            });
        }, 600);
    });
});