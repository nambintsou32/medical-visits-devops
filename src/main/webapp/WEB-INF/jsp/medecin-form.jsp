<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="fr">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>${editing ? 'Modifier un médecin' : 'Ajouter un médecin'} | Centre médical</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/app.css">
</head>
<body>
  <header class="site-header">
    <nav class="nav" aria-label="Navigation principale">
      <a class="brand" href="${pageContext.request.contextPath}/"><span class="brand-mark" aria-hidden="true">+</span>Centre médical</a>
      <ul class="nav-links">
        <li><a href="${pageContext.request.contextPath}/">Accueil</a></li>
        <li><a href="${pageContext.request.contextPath}/patients">Patients</a></li>
        <li><a href="${pageContext.request.contextPath}/medecins" aria-current="page">Médecins</a></li>
        <li><a href="${pageContext.request.contextPath}/visites">Visites</a></li>
      </ul>
    </nav>
  </header>

  <main class="container narrow">
    <a class="back-link" href="${pageContext.request.contextPath}/medecins">← Retour aux médecins</a>
    <section class="panel">
      <p class="eyebrow">${editing ? 'Mise à jour du profil' : 'Nouveau membre'}</p>
      <h1>${editing ? 'Modifier le médecin' : 'Ajouter un médecin'}</h1>
      <p class="required-note">Tous les champs sont obligatoires.</p>
      <c:if test="${not empty error}"><p class="alert alert-error" role="alert"><c:out value="${error}"/></p></c:if>

      <c:set var="codeValue" value="${formSubmitted ? param.codeMed : medecin.codeMed}"/>
      <c:set var="nomValue" value="${formSubmitted ? param.nom : medecin.nom}"/>
      <c:set var="prenomValue" value="${formSubmitted ? param.prenom : medecin.prenom}"/>
      <c:set var="gradeValue" value="${formSubmitted ? param.grade : medecin.grade}"/>
      <form method="post" action="${pageContext.request.contextPath}/medecins">
        <input type="hidden" name="action" value="${editing ? 'update' : 'create'}">
        <c:choose>
          <c:when test="${editing}">
            <input type="hidden" name="codeMed" value="<c:out value="${codeValue}"/>">
            <div class="field"><label>Code médecin</label><div class="readonly-value code"><c:out value="${codeValue}"/></div></div>
          </c:when>
          <c:otherwise>
            <div class="field">
              <label for="codeMed">Code médecin</label>
              <input id="codeMed" name="codeMed" maxlength="30" value="<c:out value="${codeValue}"/>" required autofocus>
              <small class="field-hint">Identifiant unique, 30 caractères maximum.</small>
            </div>
          </c:otherwise>
        </c:choose>
        <div class="field"><label for="nom">Nom</label><input id="nom" name="nom" maxlength="100" value="<c:out value="${nomValue}"/>" required></div>
        <div class="field"><label for="prenom">Prénom</label><input id="prenom" name="prenom" maxlength="100" value="<c:out value="${prenomValue}"/>" required></div>
        <div class="field"><label for="grade">Grade</label><input id="grade" name="grade" maxlength="100" value="<c:out value="${gradeValue}"/>" required></div>
        <div class="form-actions">
          <button type="submit">Enregistrer</button>
          <a class="button button-secondary" href="${pageContext.request.contextPath}/medecins">Annuler</a>
        </div>
      </form>
    </section>
  </main>
</body>
</html>
