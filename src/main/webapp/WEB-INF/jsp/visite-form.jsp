<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="fr">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>${editing ? 'Modifier une visite' : 'Ajouter une visite'} | Centre médical</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/app.css">
</head>
<body>
  <header class="site-header">
    <nav class="nav" aria-label="Navigation principale">
      <a class="brand" href="${pageContext.request.contextPath}/"><span class="brand-mark" aria-hidden="true">+</span>Centre médical</a>
      <ul class="nav-links">
        <li><a href="${pageContext.request.contextPath}/">Accueil</a></li>
        <li><a href="${pageContext.request.contextPath}/patients">Patients</a></li>
        <li><a href="${pageContext.request.contextPath}/medecins">Médecins</a></li>
        <li><a href="${pageContext.request.contextPath}/visites" aria-current="page">Visites</a></li>
      </ul>
    </nav>
  </header>

  <main class="container narrow">
    <a class="back-link" href="${pageContext.request.contextPath}/visites">← Retour aux visites</a>
    <section class="panel">
      <p class="eyebrow">${editing ? 'Mise à jour du rendez-vous' : 'Nouveau rendez-vous'}</p>
      <h1>${editing ? 'Modifier la visite' : 'Ajouter une visite'}</h1>
      <p class="required-note">Tous les champs sont obligatoires.</p>
      <c:if test="${not empty error}"><p class="alert alert-error" role="alert"><c:out value="${error}"/></p></c:if>

      <c:choose>
        <c:when test="${empty medecins or empty patients}">
          <div class="alert alert-info">
            Il faut enregistrer au moins un médecin et un patient avant de créer une visite.
          </div>
          <div class="form-actions">
            <c:if test="${empty medecins}"><a class="button button-primary" href="${pageContext.request.contextPath}/medecins/new">Ajouter un médecin</a></c:if>
            <c:if test="${empty patients}"><a class="button button-primary" href="${pageContext.request.contextPath}/patients/new">Ajouter un patient</a></c:if>
          </div>
        </c:when>
        <c:otherwise>
          <c:set var="codeMedValue" value="${formSubmitted ? param.codeMed : visiter.medecin.codeMed}"/>
          <c:set var="codePatValue" value="${formSubmitted ? param.codePat : visiter.patient.codePat}"/>
          <c:set var="dateValue" value="${formSubmitted ? param.visitDate : visiter.visitDate}"/>
          <form method="post" action="${pageContext.request.contextPath}/visites">
            <input type="hidden" name="action" value="${editing ? 'replace' : 'create'}">
            <c:if test="${editing}">
              <input type="hidden" name="currentCodeMed" value="<c:out value="${formSubmitted ? param.currentCodeMed : visiter.id.codeMed}"/>">
              <input type="hidden" name="currentCodePat" value="<c:out value="${formSubmitted ? param.currentCodePat : visiter.id.codePat}"/>">
              <input type="hidden" name="currentVisitDate" value="<c:out value="${formSubmitted ? param.currentVisitDate : visiter.id.visitDate}"/>">
            </c:if>
            <div class="field">
              <label for="codeMed">Médecin</label>
              <select id="codeMed" name="codeMed" required>
                <c:forEach items="${medecins}" var="medecin">
                  <option value="${medecin.codeMed}" <c:if test="${codeMedValue eq medecin.codeMed}">selected</c:if>>
                    <c:out value="${medecin.codeMed}"/> — <c:out value="${medecin.nom}"/> <c:out value="${medecin.prenom}"/>
                  </option>
                </c:forEach>
              </select>
            </div>
            <div class="field">
              <label for="codePat">Patient</label>
              <select id="codePat" name="codePat" required>
                <c:forEach items="${patients}" var="patient">
                  <option value="${patient.codePat}" <c:if test="${codePatValue eq patient.codePat}">selected</c:if>>
                    <c:out value="${patient.codePat}"/> — <c:out value="${patient.nom}"/> <c:out value="${patient.prenom}"/>
                  </option>
                </c:forEach>
              </select>
            </div>
            <div class="field">
              <label for="visitDate">Date de visite</label>
              <input id="visitDate" type="date" name="visitDate" value="<c:out value="${dateValue}"/>" required>
            </div>
            <div class="form-actions">
              <button type="submit">Enregistrer</button>
              <a class="button button-secondary" href="${pageContext.request.contextPath}/visites">Annuler</a>
            </div>
          </form>
        </c:otherwise>
      </c:choose>
    </section>
  </main>
</body>
</html>
