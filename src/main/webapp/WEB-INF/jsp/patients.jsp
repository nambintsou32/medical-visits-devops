<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="fr">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Patients | Centre médical</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/app.css">
</head>
<body>
  <header class="site-header">
    <nav class="nav" aria-label="Navigation principale">
      <a class="brand" href="${pageContext.request.contextPath}/">
        <span class="brand-mark" aria-hidden="true">+</span>Centre médical
      </a>
      <ul class="nav-links">
        <li><a href="${pageContext.request.contextPath}/">Accueil</a></li>
        <li><a href="${pageContext.request.contextPath}/patients" aria-current="page">Patients</a></li>
        <li><a href="${pageContext.request.contextPath}/medecins">Médecins</a></li>
        <li><a href="${pageContext.request.contextPath}/visites">Visites</a></li>
      </ul>
    </nav>
  </header>

  <main class="container">
    <div class="page-heading">
      <div>
        <p class="eyebrow">Dossiers</p>
        <h1>Patients</h1>
        <p class="lead">Recherchez et gérez les patients du centre médical.</p>
      </div>
      <a class="button button-primary" href="${pageContext.request.contextPath}/patients/new">
        + Ajouter un patient
      </a>
    </div>

    <c:if test="${param.message eq 'create'}"><p class="alert alert-success" role="status">Patient créé avec succès.</p></c:if>
    <c:if test="${param.message eq 'update'}"><p class="alert alert-success" role="status">Patient modifié avec succès.</p></c:if>
    <c:if test="${param.message eq 'delete'}"><p class="alert alert-success" role="status">Patient supprimé avec succès.</p></c:if>
    <c:if test="${not empty error}"><p class="alert alert-error" role="alert"><c:out value="${error}"/></p></c:if>

    <form class="toolbar panel" method="get" action="${pageContext.request.contextPath}/patients">
      <div class="field">
        <label for="search">Rechercher un patient</label>
        <input id="search" name="search" value="<c:out value="${search}"/>" placeholder="Saisissez un code ou un nom">
      </div>
      <button type="submit">Rechercher</button>
      <a class="button button-secondary" href="${pageContext.request.contextPath}/patients">Réinitialiser</a>
    </form>

    <c:choose>
      <c:when test="${empty patients}">
        <section class="empty-state">
          <span class="card-icon" aria-hidden="true">♙</span>
          <h2>Aucun patient trouvé</h2>
          <p>Modifiez votre recherche ou créez un nouveau dossier patient.</p>
          <a class="button button-primary" href="${pageContext.request.contextPath}/patients/new">Ajouter un patient</a>
        </section>
      </c:when>
      <c:otherwise>
        <div class="table-wrap">
          <table>
            <thead><tr><th>Code</th><th>Nom</th><th>Prénom</th><th>Sexe</th><th>Adresse</th><th>Actions</th></tr></thead>
            <tbody>
              <c:forEach items="${patients}" var="patient">
                <c:url var="editUrl" value="/patients/edit"><c:param name="codePat" value="${patient.codePat}"/></c:url>
                <tr>
                  <td class="code"><c:out value="${patient.codePat}"/></td>
                  <td><c:out value="${patient.nom}"/></td>
                  <td><c:out value="${patient.prenom}"/></td>
                  <td><c:out value="${patient.sexe}"/></td>
                  <td><c:out value="${patient.adresse}"/></td>
                  <td>
                    <div class="actions">
                      <a class="button button-secondary button-small" href="${editUrl}">Modifier</a>
                      <form method="post"
                            action="${pageContext.request.contextPath}/patients"
                            onsubmit="return confirm('Voulez-vous vraiment supprimer ce patient ?');">
                        <input type="hidden" name="action" value="delete">
                        <input type="hidden" name="codePat" value="<c:out value="${patient.codePat}"/>">
                        <button class="button-danger button-small" type="submit">Supprimer</button>
                      </form>
                    </div>
                  </td>
                </tr>
              </c:forEach>
            </tbody>
          </table>
        </div>
      </c:otherwise>
    </c:choose>
  </main>
</body>
</html>
