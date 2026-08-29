<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="fr">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Médecins | Centre médical</title>
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

  <main class="container">
    <div class="page-heading">
      <div>
        <p class="eyebrow">Équipe médicale</p>
        <h1>Médecins</h1>
        <p class="lead">Consultez et mettez à jour les membres de l’équipe.</p>
      </div>
      <a class="button button-primary" href="${pageContext.request.contextPath}/medecins/new">+ Ajouter un médecin</a>
    </div>

    <c:if test="${param.message eq 'create'}"><p class="alert alert-success" role="status">Médecin créé avec succès.</p></c:if>
    <c:if test="${param.message eq 'update'}"><p class="alert alert-success" role="status">Médecin modifié avec succès.</p></c:if>
    <c:if test="${param.message eq 'delete'}"><p class="alert alert-success" role="status">Médecin supprimé avec succès.</p></c:if>
    <c:if test="${not empty error}"><p class="alert alert-error" role="alert"><c:out value="${error}"/></p></c:if>

    <c:choose>
      <c:when test="${empty medecins}">
        <section class="empty-state">
          <span class="card-icon" aria-hidden="true">✚</span>
          <h2>Aucun médecin enregistré</h2>
          <p>Ajoutez un médecin pour commencer à planifier des visites.</p>
          <a class="button button-primary" href="${pageContext.request.contextPath}/medecins/new">Ajouter un médecin</a>
        </section>
      </c:when>
      <c:otherwise>
        <div class="table-wrap">
          <table>
            <thead><tr><th>Code</th><th>Nom</th><th>Prénom</th><th>Grade</th><th>Actions</th></tr></thead>
            <tbody>
              <c:forEach items="${medecins}" var="medecin">
                <c:url var="editUrl" value="/medecins/edit"><c:param name="codeMed" value="${medecin.codeMed}"/></c:url>
                <tr>
                  <td class="code"><c:out value="${medecin.codeMed}"/></td>
                  <td><c:out value="${medecin.nom}"/></td>
                  <td><c:out value="${medecin.prenom}"/></td>
                  <td><c:out value="${medecin.grade}"/></td>
                  <td>
                    <div class="actions">
                      <a class="button button-secondary button-small" href="${editUrl}">Modifier</a>
                      <form method="post"
                            action="${pageContext.request.contextPath}/medecins"
                            onsubmit="return confirm('Voulez-vous vraiment supprimer ce médecin ?');">
                        <input type="hidden" name="action" value="delete">
                        <input type="hidden" name="codeMed" value="<c:out value="${medecin.codeMed}"/>">
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
