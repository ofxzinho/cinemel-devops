const choice = (value, label) => ({ value, label });
const seatTypes = [
    choice("NORMAL", "Normal"), choice("PCD", "PCD"),
    choice("OBESO", "Obeso"), choice("ACOMPANHANTE", "Acompanhante")
];

const sections = {
    filmes: {
        title: "Filmes", noun: "filme", article: "o",
        intro: "Gerencie o catálogo e acompanhe os títulos em cartaz.",
        fields: [
            { key: "titulo", label: "Título", required: true, maxLength: 150 },
            { key: "status", label: "Situação", options: [
                choice("EM_CARTAZ", "Em cartaz"), choice("EM_BREVE", "Em breve")
            ], required: true },
            { key: "duracaoMinutos", label: "Duração em minutos", type: "number", min: 1 },
            { key: "classificacao", label: "Classificação indicativa", maxLength: 3 },
            { key: "dataEstreia", label: "Data de estreia", type: "date" },
            { key: "sinopse", label: "Sinopse", type: "textarea", maxLength: 2000 },
            { key: "posterUrl", label: "URL do pôster", type: "url" }
        ],
        name: item => item.titulo,
        details: item => [
            item.status === "EM_CARTAZ" ? "Em cartaz" : "Em breve",
            item.duracaoMinutos ? item.duracaoMinutos + " min" : ""
        ]
    },
    salas: {
        title: "Salas", noun: "sala", article: "a",
        intro: "Cadastre as salas e descreva a disposição dos lugares.",
        fields: [
            { key: "nome", label: "Nome da sala", required: true, maxLength: 50 },
            { key: "totalFileiras", label: "Total de fileiras", type: "number", min: 1 },
            { key: "assentosPorFileira", label: "Assentos por fileira", type: "number", min: 1 }
        ],
        name: item => item.nome,
        details: item => [
            item.totalFileiras ? item.totalFileiras + " fileiras" : "",
            item.assentosPorFileira ? item.assentosPorFileira + " assentos por fileira" : ""
        ]
    },
    assentos: {
        title: "Assentos", noun: "assento", article: "o",
        intro: "Consulte e organize os lugares de cada sala.",
        fields: [
            { key: "salaId", label: "Sala", from: "salas", relation: "sala", required: true },
            { key: "fileira", label: "Fileira", required: true, maxLength: 2 },
            { key: "numero", label: "Número", type: "number", min: 1, required: true },
            { key: "tipo", label: "Tipo de assento", options: seatTypes, required: true }
        ],
        name: item => "Assento " + item.fileira + item.numero,
        details: item => [item.sala?.nome, seatTypes.find(type => type.value === item.tipo)?.label]
    },
    sessoes: {
        title: "Sessões", noun: "sessão", article: "a",
        intro: "Programe um filme em uma sala e consulte as sessões cadastradas.",
        fields: [
            { key: "filmeId", label: "Filme", from: "filmes", relation: "filme", required: true },
            { key: "salaId", label: "Sala", from: "salas", relation: "sala", required: true },
            { key: "dataHora", label: "Data e hora", type: "datetime-local", step: 1, required: true },
            { key: "formato", label: "Formato", options: [
                choice("DOIS_D", "2D"), choice("TRES_D", "3D"), choice("IMAX", "IMAX")
            ], required: true },
            { key: "audio", label: "Áudio", options: [
                choice("DUBLADO", "Dublado"), choice("LEGENDADO", "Legendado")
            ], required: true },
            { key: "precoBase", label: "Preço em reais", type: "number", step: .01, min: 0, required: true }
        ],
        name: item => item.filme?.titulo || "Sessão " + item.id,
        details: item => [
            item.sala?.nome,
            item.dataHora ? new Date(item.dataHora).toLocaleString("pt-BR", {
                dateStyle: "short", timeStyle: "short"
            }) : "",
            new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" }).format(item.precoBase)
        ]
    }
};

const byId = id => document.getElementById(id);
const editor = byId("editor");
const confirmation = byId("confirmation");
const form = byId("record-form");
const newButton = byId("new-record");
const saveButton = byId("review-save");
const confirmButton = byId("confirm-action");
const cancelConfirmation = byId("cancel-confirmation");
let active;
let editing;
let pendingAction;
let listVersion = 0;
let editorVersion = 0;
let requestInProgress = false;

async function api(path, method = "GET", body) {
    const options = { method };
    if (body !== undefined) {
        options.headers = { "Content-Type": "application/json" };
        options.body = JSON.stringify(body);
    }

    const response = await fetch(path, options);
    if (response.ok) {
        return response.status === 204 ? undefined : response.json();
    }

    const problem = await response.json().catch(() => null);
    const messages = {
        400: "Confira os dados informados.",
        404: "O registro ou vínculo informado não existe mais.",
        409: "Já existe um cadastro com esses dados."
    };
    const error = new Error(problem?.detail || messages[response.status] || "Não foi possível concluir a operação.");
    error.status = response.status;
    throw error;
}

function errorText(error) {
    return error.status ? error.message + " (HTTP " + error.status + ")"
        : "Não foi possível conectar à API. Tente novamente.";
}

function feedback(id, message = "", error = false) {
    const element = byId(id);
    element.textContent = message;
    element.hidden = !message;
    element.toggleAttribute("data-error", error);
}

function actionButton(label, callback, className = "") {
    const button = document.createElement("button");
    button.type = "button";
    button.className = className;
    button.textContent = label;
    button.addEventListener("click", callback);
    return button;
}

function recordRow(section, item) {
    const row = document.createElement("li");
    row.className = "record";
    const info = document.createElement("div");
    info.className = "record-info";
    const title = document.createElement("p");
    title.className = "record-title";
    title.textContent = section.name(item);
    const details = document.createElement("p");
    details.className = "record-details";
    details.textContent = section.details(item).filter(Boolean).join(" · ");
    info.append(title, details);
    const actions = document.createElement("div");
    actions.className = "record-actions";
    actions.append(
        actionButton("Editar", () => openEditor(item)),
        actionButton("Excluir", () => askDelete(item), "delete")
    );
    row.append(info, actions);
    return row;
}

async function loadList() {
    const version = ++listVersion;
    const key = active;
    const section = sections[key];
    const list = byId("entries");
    const status = byId("list-status");
    const retry = byId("retry");
    list.replaceChildren();
    byId("count").textContent = "";
    retry.hidden = true;
    status.textContent = "Carregando cadastros…";

    try {
        const entries = await api("/" + key);
        if (version !== listVersion) return;

        byId("count").textContent = entries.length === 1 ? "1 registro" : entries.length + " registros";
        status.textContent = entries.length ? "" : "Ainda não há registros nesta área.";
        list.replaceChildren(...entries.map(item => recordRow(section, item)));
    } catch (error) {
        if (version !== listVersion) return;
        status.textContent = errorText(error);
        retry.hidden = false;
    }
}

function navigate() {
    const key = location.hash.slice(1);
    active = Object.hasOwn(sections, key) ? key : "assentos";
    const section = sections[active];
    byId("heading").textContent = section.title;
    byId("intro").textContent = section.intro;
    newButton.textContent = "Cadastrar " + section.noun;
    for (const link of document.querySelectorAll("nav a")) {
        if (link.hash === "#" + active) link.setAttribute("aria-current", "page");
        else link.removeAttribute("aria-current");
    }
    feedback("notice");
    if (confirmation.open) confirmation.close();
    if (editor.open) editor.close();
    loadList();
}

function makeField(field, options, item) {
    const label = document.createElement("label");
    label.className = "field";
    const caption = document.createElement("span");
    caption.textContent = field.label;
    label.append(caption);

    let input;
    if (options) {
        input = document.createElement("select");
        input.append(new Option("Selecione", ""));
        for (const option of options) input.append(new Option(option.label, option.value));
    } else if (field.type === "textarea") {
        input = document.createElement("textarea");
        input.rows = 3;
    } else {
        input = document.createElement("input");
        input.type = field.type || "text";
    }

    input.name = field.key;
    input.required = Boolean(field.required);
    if (field.maxLength) input.maxLength = field.maxLength;
    if (field.min !== undefined) input.min = field.min;
    if (field.step !== undefined) input.step = field.step;
    const value = field.relation ? item?.[field.relation]?.id : item?.[field.key];
    input.value = value ?? "";
    label.append(input);
    return label;
}

async function openEditor(item) {
    const version = ++editorVersion;
    const key = active;
    const section = sections[key];
    editing = item;
    byId("editor-title").textContent = (item ? "Editar " : "Cadastrar ") + section.noun;
    feedback("form-error");
    saveButton.disabled = true;
    byId("fields").textContent = "Preparando formulário…";
    editor.showModal();

    try {
        const options = await Promise.all(section.fields.map(async field => {
            if (!field.from) return field.options;
            const records = await api("/" + field.from);
            return records.map(record => choice(record.id, sections[field.from].name(record)));
        }));
        if (version !== editorVersion || !editor.open) return;
        byId("fields").replaceChildren(...section.fields.map((field, i) => makeField(field, options[i], item)));
        const missing = section.fields.find((field, i) => field.from && !options[i].length);
        if (missing) {
            const dependency = sections[missing.from];
            feedback("form-error", "Cadastre " + dependency.article + " " + dependency.noun
                + " na aba " + dependency.title + " antes de continuar.");
        } else {
            saveButton.disabled = false;
            form.querySelector("#fields input, #fields select, #fields textarea")?.focus();
        }
    } catch (error) {
        if (version !== editorVersion || !editor.open) return;
        byId("fields").replaceChildren();
        feedback("form-error", errorText(error));
    }
}

function confirmAction(action) {
    pendingAction = action;
    byId("confirmation-title").textContent = action.title;
    byId("confirmation-text").textContent = action.message;
    confirmButton.textContent = action.label;
    confirmButton.className = action.method === "DELETE" ? "danger" : "primary";
    cancelConfirmation.textContent = action.method === "DELETE" ? "Cancelar" : "Voltar";
    feedback("confirmation-error");
    confirmation.showModal();
}

function askDelete(item) {
    const section = sections[active];
    confirmAction({
        key: active, path: "/" + active + "/" + item.id, method: "DELETE",
        title: "Excluir " + section.noun + "?",
        message: "O registro “" + section.name(item) + "” será removido.",
        label: "Excluir registro", success: "Registro excluído."
    });
}

form.addEventListener("submit", event => {
    event.preventDefault();
    const section = sections[active];
    const body = {};
    for (const field of section.fields) {
        const input = form.elements.namedItem(field.key);
        const value = input.value.trim();
        if (field.required && !value) {
            feedback("form-error", "Preencha o campo " + field.label + ".");
            input.focus();
            return;
        }
        body[field.key] = value === "" ? null : (field.type === "number" || field.from ? Number(value) : value);
    }

    const name = body.titulo || body.nome || section.noun;
    const sala = form.elements.namedItem("salaId")?.selectedOptions[0]?.textContent;
    const message = active === "assentos"
        ? "Assento " + body.fileira + body.numero + " · " + sala
        : active === "sessoes"
            ? form.elements.namedItem("filmeId").selectedOptions[0].textContent + " · " + sala
            : name;

    confirmAction({
        key: active, path: "/" + active + (editing ? "/" + editing.id : ""),
        method: editing ? "PUT" : "POST", body,
        title: editing ? "Salvar alterações?" : "Confirmar cadastro?",
        message, label: "Confirmar e salvar",
        success: editing ? "Alterações salvas." : "Cadastro concluído."
    });
});

confirmButton.addEventListener("click", async () => {
    const action = pendingAction;
    if (!action || requestInProgress) return;
    requestInProgress = true;
    confirmButton.disabled = true;
    cancelConfirmation.disabled = true;
    feedback("confirmation-error");

    try {
        await api(action.path, action.method, action.body);
        if (active !== action.key) return;
        confirmation.close();
        if (editor.open) editor.close();
        feedback("notice", action.success);
        loadList();
    } catch (error) {
        if (active !== action.key) return;
        if (editor.open) {
            confirmation.close();
            feedback("form-error", errorText(error));
            byId("form-error").focus();
        } else {
            feedback("confirmation-error", errorText(error));
        }
    } finally {
        requestInProgress = false;
        confirmButton.disabled = false;
        cancelConfirmation.disabled = false;
    }
});

confirmation.addEventListener("cancel", event => {
    if (requestInProgress) event.preventDefault();
});
cancelConfirmation.addEventListener("click", () => confirmation.close());
byId("close-editor").addEventListener("click", () => editor.close());
byId("cancel-editor").addEventListener("click", () => editor.close());
byId("retry").addEventListener("click", loadList);
newButton.addEventListener("click", () => openEditor());
window.addEventListener("hashchange", navigate);
navigate();
