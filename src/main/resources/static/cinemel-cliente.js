const byId = id => document.getElementById(id);
const movieSelect = byId("movie");
const sessionSelect = byId("session");
const confirmation = byId("confirmation");
const receiptDialog = byId("receipt-dialog");
const review = byId("review");
const confirm = byId("confirm");
const cancel = byId("cancel");
const featuredTitle = "Os Seis Sem Futuro";
const storageKey = "cinemel-reservas";
const dateFormat = new Intl.DateTimeFormat("pt-BR", {
    dateStyle: "short", timeStyle: "short", timeZone: "UTC"
});
const cinemaClock = new Intl.DateTimeFormat("sv-SE", {
    dateStyle: "short", timeStyle: "medium", timeZone: "America/Sao_Paulo"
});
let movies = [];
let sessions = [];
let catalogueLoaded = false;
let filter = "todos";
let activeSeat;
let mapVersion = 0;
let submitting = false;

async function request(path, body) {
    const options = body ? {
        method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify(body)
    } : {};
    const response = await fetch(path, options);
    if (response.ok) return response.json();

    const problem = await response.json().catch(() => null);
    const error = new Error(problem?.detail || "Não foi possível concluir. Tente novamente.");
    error.status = response.status;
    throw error;
}

function dateForDisplay(value) {
    return dateFormat.format(new Date(value + "Z"));
}

function seatName(seat) {
    return seat.fileira + seat.numero;
}

function showPage() {
    const requested = location.hash.slice(1);
    const filmId = /^filme\/(\d+)$/.exec(requested)?.[1];
    const pages = ["inicio", "filmes", "detalhes", "comprar", "perfil"];
    const active = filmId ? "detalhes" : pages.includes(requested) && requested !== "detalhes" ? requested : "inicio";
    for (const id of pages) byId(id).hidden = id !== active;
    const tab = active === "detalhes" ? "filmes" : active;
    for (const link of document.querySelectorAll("nav a")) {
        if (link.hash === "#" + tab) link.setAttribute("aria-current", "page");
        else link.removeAttribute("aria-current");
    }
    if (active === "detalhes") renderFilm(Number(filmId));
    if (active === "comprar") refreshSeats();
    if (active === "perfil") renderReceipts();
}

function setOptions(select, items, placeholder, getLabel, preferred) {
    select.replaceChildren(new Option(placeholder, ""));
    for (const item of items) select.append(new Option(getLabel(item), item.id));
    if (items.some(item => Number(preferred) === item.id)) select.value = preferred;
    else if (items.length === 1) select.value = items[0].id;
    select.disabled = !items.length;
}

function chooseMovie(id, sessionId) {
    if (id) movieSelect.value = id;
    loadSessionsForMovie(sessionId);
    location.hash = "comprar";
}

function allowedPoster(url) {
    if (!url) return null;
    try {
        const parsed = new URL(url, location.origin);
        return parsed.protocol === "http:" || parsed.protocol === "https:" ? parsed.href : null;
    } catch {
        return null;
    }
}

function renderCatalogue() {
    const status = byId("catalogue-status");
    const list = byId("catalogue");
    const visible = movies.filter(movie => filter === "todos" || movie.status === filter);
    list.replaceChildren();
    status.textContent = visible.length ? "" : "Nenhum filme nesta categoria.";

    for (const movie of visible) {
        const row = document.createElement("li");
        row.className = "movie-card";
        const link = document.createElement("a");
        link.href = "#filme/" + movie.id;
        const poster = posterFor(movie);
        if (poster) {
            const image = document.createElement("img");
            image.src = poster;
            image.alt = "";
            image.loading = "lazy";
            image.addEventListener("error", () => image.remove(), { once: true });
            link.append(image);
        }
        const caption = document.createElement("div");
        const title = document.createElement("span");
        const state = document.createElement("small");
        title.textContent = movie.titulo;
        state.textContent = movie.status === "EM_BREVE" ? "Em breve" : "Em cartaz";
        caption.append(title, state);
        link.append(caption);
        row.append(link);
        list.append(row);
    }
}

function posterFor(movie) {
    return movie.titulo === featuredTitle ? "/os-seis-sem-futuro.png" : allowedPoster(movie.posterUrl);
}

function renderFilm(id) {
    const movie = movies.find(item => item.id === id);
    const image = byId("film-image");
    const title = byId("film-title");
    const poster = movie && posterFor(movie);
    image.hidden = !poster;
    if (poster) image.src = poster;
    title.textContent = movie?.titulo || (catalogueLoaded ? "Filme indisponível" : "Carregando filme…");
    const featured = movie?.titulo === featuredTitle;
    byId("film-meta").textContent = movie ? [
        movie.status === "EM_BREVE" ? "Em breve" : "Em cartaz",
        featured ? "2026" : null,
        movie.duracaoMinutos ? movie.duracaoMinutos + " min" : null,
        movie.classificacao ? "Classificação " + movie.classificacao : null
    ].filter(Boolean).join(" · ") : "";
    byId("film-description").textContent = featured
        ? "O futuro era promissor. Até eles se conhecerem. Gênero: comédia, tragédia e desemprego."
        : movie?.sinopse || "";
    byId("film-rating").textContent = featured
        ? "IMDb: 9,8/10 — Avaliado pelos próprios protagonistas." : "";
    renderShowtimes("film-showtimes", "film-status", sessions.filter(item => item.filme?.id === id), false);
}

function renderShowtimes(listId, statusId, entries, showFilm) {
    const list = byId(listId);
    list.replaceChildren();
    byId(statusId).textContent = entries.length ? "" : "Nenhuma sessão cadastrada.";
    for (const session of entries) {
        const row = document.createElement("li");
        const button = document.createElement("button");
        const time = document.createElement("strong");
        const subtitle = document.createElement("span");
        time.textContent = dateForDisplay(session.dataHora);
        subtitle.textContent = (showFilm ? session.filme.titulo + " · " : "") + session.sala.nome;
        button.type = "button";
        button.append(time, subtitle);
        button.onclick = () => chooseMovie(session.filme.id, session.id);
        row.append(button);
        list.append(row);
    }
}

function loadSessionsForMovie(preferredSession) {
    const id = Number(movieSelect.value);
    const options = sessions.filter(session => session.filme?.id === id);
    setOptions(sessionSelect, options, "Escolha uma sessão",
        session => dateForDisplay(session.dataHora) + " · " + session.sala?.nome,
        preferredSession || sessionSelect.value);
    refreshSeats();
}

function selectSeat(seat) {
    activeSeat = seat;
    for (const button of byId("seats").querySelectorAll("button")) {
        button.setAttribute("aria-pressed", String(Number(button.dataset.seatId) === seat.id));
    }
    review.disabled = false;
}

async function refreshSeats() {
    const version = ++mapVersion;
    const sessionId = sessionSelect.value;
    const status = byId("booking-status");
    const seats = byId("seats");
    seats.replaceChildren();
    byId("screen").hidden = true;
    activeSeat = undefined;
    review.disabled = true;
    if (!sessionId) {
        status.textContent = movieSelect.value && sessionSelect.disabled
            ? "Este filme está sem sessões na programação."
            : "Escolha uma sessão para ver os lugares.";
        return;
    }
    status.textContent = "Buscando assentos…";
    try {
        const available = await request("/reservas/disponibilidade/" + sessionId);
        if (version !== mapVersion) return;
        if (!available.length) {
            status.textContent = "A sala ainda está sem assentos cadastrados.";
            return;
        }
        byId("screen").hidden = false;
        status.textContent = available.every(seat => seat.reservado)
            ? "Todos os lugares desta sessão estão reservados." : "Toque em um lugar livre para continuar.";
        const rows = new Map();
        for (const seat of available) {
            if (!rows.has(seat.fileira)) {
                const row = document.createElement("div");
                row.className = "seat-row";
                rows.set(seat.fileira, row);
                seats.append(row);
            }
            const button = document.createElement("button");
            button.type = "button";
            button.className = "seat";
            button.dataset.seatId = seat.id;
            button.textContent = seatName(seat);
            button.disabled = seat.reservado;
            button.setAttribute("aria-label", seatName(seat) + (seat.reservado ? " reservado" : ""));
            button.setAttribute("aria-pressed", "false");
            button.onclick = () => selectSeat(seat);
            rows.get(seat.fileira).append(button);
        }
    } catch {
        if (version === mapVersion) status.textContent = "Não foi possível carregar os lugares. Atualize a página.";
    }
}

function receipts() {
    try {
        const saved = JSON.parse(localStorage.getItem(storageKey) || "[]");
        return Array.isArray(saved) ? saved : [];
    } catch {
        return [];
    }
}

function receiptSummary(receipt) {
    return receipt.filme + " · " + dateForDisplay(receipt.dataHora)
        + " · " + receipt.sala + " · Assento " + receipt.assento;
}

function renderReceipts() {
    const list = byId("receipts");
    const saved = receipts();
    list.replaceChildren();
    byId("receipts-status").textContent = saved.length ? "" : "Você ainda não fez uma reserva neste navegador.";
    for (const receipt of saved) {
        const row = document.createElement("li");
        const info = document.createElement("p");
        const code = document.createElement("p");
        info.textContent = receiptSummary(receipt);
        code.className = "code";
        code.textContent = receipt.codigo;
        row.append(info, code);
        list.append(row);
    }
}

review.onclick = () => {
    if (!activeSeat) return;
    byId("confirmation-summary").textContent = movieSelect.selectedOptions[0].textContent
        + " · " + sessionSelect.selectedOptions[0].textContent + " · Assento " + seatName(activeSeat);
    byId("confirmation-error").textContent = "";
    confirm.disabled = false;
    confirmation.showModal();
};
cancel.onclick = () => {
    confirmation.close();
    refreshSeats();
};
confirmation.addEventListener("cancel", event => {
    if (submitting) event.preventDefault();
    else refreshSeats();
});
confirm.onclick = async () => {
    if (submitting || !activeSeat) return;
    submitting = true;
    confirm.disabled = true;
    cancel.disabled = true;
    try {
        const receipt = await request("/reservas", {
            sessaoId: Number(sessionSelect.value), assentoId: activeSeat.id
        });
        try {
            localStorage.setItem(storageKey, JSON.stringify([receipt, ...receipts()]));
        } catch {
            byId("receipts-status").textContent = "O navegador bloqueou o histórico local.";
        }
        byId("receipt-summary").textContent = receiptSummary(receipt);
        byId("receipt-code").textContent = receipt.codigo;
        confirmation.close();
        receiptDialog.showModal();
        refreshSeats();
    } catch (error) {
        byId("confirmation-error").textContent = error.status === 409
            ? error.message + " Volte e escolha outro lugar." : error.message;
        if (error.status !== 409) confirm.disabled = false;
    } finally {
        submitting = false;
        cancel.disabled = false;
    }
};
byId("open-profile").onclick = () => {
    receiptDialog.close();
    location.hash = "perfil";
};
byId("feature-book").onclick = () => chooseMovie();
byId("feature-details").onclick = () => { location.hash = "filmes"; };
movieSelect.onchange = () => loadSessionsForMovie();
sessionSelect.onchange = refreshSeats;
window.addEventListener("hashchange", () => {
    showPage();
    window.scrollTo(0, 0);
});
for (const button of document.querySelectorAll(".filters button")) {
    button.onclick = () => {
        filter = button.dataset.filter;
        for (const option of document.querySelectorAll(".filters button")) {
            option.setAttribute("aria-pressed", String(option === button));
        }
        if (catalogueLoaded) renderCatalogue();
    };
}

async function loadCatalogue() {
    try {
        [movies, sessions] = await Promise.all([request("/filmes"), request("/sessoes")]);
        catalogueLoaded = true;
        const now = cinemaClock.format(new Date()).replace(" ", "T");
        sessions = sessions.filter(session => session.dataHora > now);
        sessions.sort((first, second) => first.dataHora.localeCompare(second.dataHora));
        const featured = movies.find(movie => movie.titulo === featuredTitle);
        if (featured) {
            byId("feature-book").onclick = () => chooseMovie(featured.id);
            byId("feature-details").onclick = () => { location.hash = "filme/" + featured.id; };
        }
        setOptions(movieSelect, movies, "Escolha um filme", movie => movie.titulo, featured?.id);
        renderCatalogue();
        renderShowtimes("home-showtimes", "home-status", sessions.slice(0, 4), true);
        loadSessionsForMovie();
        showPage();
    } catch {
        byId("catalogue-status").textContent = "Não foi possível carregar a programação. Atualize a página.";
        byId("home-status").textContent = "Não foi possível carregar os horários.";
    }
}
setOptions(movieSelect, [], "Carregando filmes…", () => "");
setOptions(sessionSelect, [], "Escolha um filme primeiro", () => "");
showPage();
loadCatalogue();
