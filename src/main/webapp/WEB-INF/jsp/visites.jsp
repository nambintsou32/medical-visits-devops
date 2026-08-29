<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="fr">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Visites | Centre médical</title>
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

  <main class="container">
    <div class="page-heading">
      <div>
        <p class="eyebrow">Planning médical</p>
        <h1>Visites</h1>
        <p class="lead">Planifiez et consultez les rendez-vous du centre.</p>
      </div>
      <a class="button button-primary" href="${pageContext.request.contextPath}/visites/new">+ Ajouter une visite</a>
    </div>

    <c:if test="${param.message eq 'create'}"><p class="alert alert-success" role="status">Visite créée avec succès.</p></c:if>
    <c:if test="${param.message eq 'replace'}"><p class="alert alert-success" role="status">Visite modifiée avec succès.</p></c:if>
    <c:if test="${param.message eq 'delete'}"><p class="alert alert-success" role="status">Visite supprimée avec succès.</p></c:if>
    <c:if test="${not empty error}"><p class="alert alert-error" role="alert"><c:out value="${error}"/></p></c:if>

    <c:choose>
      <c:when test="${empty visites}">
        <section class="empty-state">
          <span class="card-icon" aria-hidden="true">▦</span>
          <h2>Aucune visite planifiée</h2>
          <p>Créez la première visite après avoir enregistré un médecin et un patient.</p>
          <a class="button button-primary" href="${pageContext.request.contextPath}/visites/new">Ajouter une visite</a>
        </section>
      </c:when>
      <c:otherwise>
        <div class="table-wrap">
          <table>
            <thead><tr><th>Date</th><th>Médecin</th><th>Patient</th><th>Actions</th></tr></thead>
            <tbody>
              <c:forEach items="${visites}" var="visite">
                <c:url var="editUrl" value="/visites/edit">
                  <c:param name="codeMed" value="${visite.id.codeMed}"/>
                  <c:param name="codePat" value="${visite.id.codePat}"/>
                  <c:param name="visitDate" value="${visite.id.visitDate}"/>
                </c:url>
                <tr>
                  <td><strong><c:out value="${visite.visitDate}"/></strong></td>
                  <td><span class="code"><c:out value="${visite.medecin.codeMed}"/></span><br><c:out value="${visite.medecin.nom}"/> <c:out value="${visite.medecin.prenom}"/></td>
                  <td><span class="code"><c:out value="${visite.patient.codePat}"/></span><br><c:out value="${visite.patient.nom}"/> <c:out value="${visite.patient.prenom}"/></td>
                  <td>
                    <div class="actions">
                      <a class="button button-secondary button-small" href="${editUrl}">Modifier</a>
                      <form method="post"
                            action="${pageContext.request.contextPath}/visites"
                            onsubmit="return confirm('Voulez-vous vraiment supprimer cette visite ?');">
                        <input type="hidden" name="action" value="delete">
                        <input type="hidden" name="codeMed" value="<c:out value="${visite.id.codeMed}"/>">
                        <input type="hidden" name="codePat" value="<c:out value="${visite.id.codePat}"/>">
                        <input type="hidden" name="visitDate" value="<c:out value="${visite.id.visitDate}"/>">
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
