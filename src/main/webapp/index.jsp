<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="fr">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Accueil | Centre médical</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/app.css">
</head>
<body>
  <header class="site-header">
    <nav class="nav" aria-label="Navigation principale">
      <a class="brand" href="${pageContext.request.contextPath}/">
        <span class="brand-mark" aria-hidden="true">+</span>Centre médical
      </a>
      <ul class="nav-links">
        <li><a href="${pageContext.request.contextPath}/" aria-current="page">Accueil</a></li>
        <li><a href="${pageContext.request.contextPath}/patients">Patients</a></li>
        <li><a href="${pageContext.request.contextPath}/medecins">Médecins</a></li>
        <li><a href="${pageContext.request.contextPath}/visites">Visites</a></li>
      </ul>
    </nav>
  </header>

  <main class="container">
    <section class="panel hero">
      <p class="eyebrow">Gestion quotidienne</p>
      <h1>Les visites médicales,<br>simplement organisées.</h1>
      <p class="lead">
        Retrouvez rapidement les patients, les médecins et le planning des visites
        depuis un espace unique.
      </p>
    </section>

    <section class="cards" aria-label="Accès rapides">
      <a class="card" href="${pageContext.request.contextPath}/patients">
        <span class="card-icon" aria-hidden="true">♙</span>
        <h2>Patients</h2>
        <p>Consulter, rechercher et gérer les dossiers patients.</p>
      </a>
      <a class="card" href="${pageContext.request.contextPath}/medecins">
        <span class="card-icon" aria-hidden="true">✚</span>
        <h2>Médecins</h2>
        <p>Administrer l’équipe médicale et ses informations.</p>
      </a>
      <a class="card" href="${pageContext.request.contextPath}/visites">
        <span class="card-icon" aria-hidden="true">▦</span>
        <h2>Visites</h2>
        <p>Planifier et mettre à jour les visites du centre.</p>
      </a>
    </section>
  </main>
</body>
</html>
