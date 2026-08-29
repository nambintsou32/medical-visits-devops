<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="fr">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>${editing ? 'Modifier un patient' : 'Ajouter un patient'} | Centre médical</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/app.css">
</head>
<body>
  <header class="site-header">
    <nav class="nav" aria-label="Navigation principale">
      <a class="brand" href="${pageContext.request.contextPath}/"><span class="brand-mark" aria-hidden="true">+</span>Centre médical</a>
      <ul class="nav-links">
        <li><a href="${pageContext.request.contextPath}/">Accueil</a></li>
        <li><a href="${pageContext.request.contextPath}/patients" aria-current="page">Patients</a></li>
        <li><a href="${pageContext.request.contextPath}/medecins">Médecins</a></li>
        <li><a href="${pageContext.request.contextPath}/visites">Visites</a></li>
      </ul>
    </nav>
  </header>

  <main class="container narrow">
    <a class="back-link" href="${pageContext.request.contextPath}/patients">← Retour aux patients</a>
    <section class="panel">
      <p class="eyebrow">${editing ? 'Mise à jour du dossier' : 'Nouveau dossier'}</p>
      <h1>${editing ? 'Modifier le patient' : 'Ajouter un patient'}</h1>
      <p class="required-note">Tous les champs sont obligatoires.</p>

      <c:if test="${not empty error}"><p class="alert alert-error" role="alert"><c:out value="${error}"/></p></c:if>
      <c:set var="codeValue" value="${formSubmitted ? param.codePat : patient.codePat}"/>
      <c:set var="nomValue" value="${formSubmitted ? param.nom : patient.nom}"/>
      <c:set var="prenomValue" value="${formSubmitted ? param.prenom : patient.prenom}"/>
      <c:set var="sexeValue" value="${formSubmitted ? param.sexe : patient.sexe}"/>
      <c:set var="adresseValue" value="${formSubmitted ? param.adresse : patient.adresse}"/>

      <form method="post" action="${pageContext.request.contextPath}/patients">
        <input type="hidden" name="action" value="${editing ? 'update' : 'create'}">
        <c:choose>
          <c:when test="${editing}">
            <input type="hidden" name="codePat" value="<c:out value="${codeValue}"/>">
            <div class="field">
              <label>Code patient</label>
              <div class="readonly-value code"><c:out value="${codeValue}"/></div>
            </div>
          </c:when>
          <c:otherwise>
            <div class="field">
              <label for="codePat">Code patient</label>
              <input id="codePat" name="codePat" maxlength="30" value="<c:out value="${codeValue}"/>" required autofocus>
              <small class="field-hint">Identifiant unique, 30 caractères maximum.</small>
            </div>
          </c:otherwise>
        </c:choose>

        <div class="field">
          <label for="nom">Nom</label>
          <input id="nom" name="nom" maxlength="100" value="<c:out value="${nomValue}"/>" required>
        </div>
        <div class="field">
          <label for="prenom">Prénom</label>
          <input id="prenom" name="prenom" maxlength="100" value="<c:out value="${prenomValue}"/>" required>
        </div>
        <div class="field">
          <label for="sexe">Sexe</label>
          <select id="sexe" name="sexe" required>
            <c:forEach items="${sexes}" var="sexe">
              <option value="${sexe}" <c:if test="${sexeValue eq sexe}">selected</c:if>>
                <c:out value="${sexe eq 'FEMININ' ? 'Féminin' : 'Masculin'}"/>
              </option>
            </c:forEach>
          </select>
        </div>
        <div class="field">
          <label for="adresse">Adresse</label>
          <input id="adresse" name="adresse" maxlength="255" value="<c:out value="${adresseValue}"/>" required>
        </div>
        <div class="form-actions">
          <button type="submit">Enregistrer</button>
          <a class="button button-secondary" href="${pageContext.request.contextPath}/patients">Annuler</a>
        </div>
      </form>
    </section>
  </main>
</body>
</html>
